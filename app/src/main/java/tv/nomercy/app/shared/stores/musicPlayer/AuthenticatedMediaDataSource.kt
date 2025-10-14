package tv.nomercy.app.shared.stores.musicPlayer

import android.media.MediaDataSource
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class AuthenticatedMediaDataSource(
    private val url: String,
    private val authToken: String?
) : MediaDataSource() {

    private companion object {
        const val TAG = "AuthMediaDataSource"

        const val INITIAL_CHUNK_SIZE = 256 * 1024
        const val MIN_CHUNK_SIZE = 64 * 1024
        const val MAX_CHUNK_SIZE = 512 * 1024
        const val MAX_CACHED_CHUNKS = 8
        const val TARGET_BUFFER_SECONDS = 3
        const val PLAYBACK_THRESHOLD_READS = 5
        const val DEFAULT_BITRATE = 320000
        const val ASSUMED_SONG_DURATION_SEC = 180
        const val TIMEOUT_SECONDS = 30L
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var contentLength: Long = -1

    @Volatile
    private var initialized = false

    private var readCount: Int = 0

    private data class CachedChunk(
        val data: ByteArray,
        val start: Long,
        val end: Long,
        var lastAccessed: Long = System.currentTimeMillis()
    )

    private val cachedChunks = mutableListOf<CachedChunk>()
    private var estimatedBitrate: Int = DEFAULT_BITRATE

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (!initialized) {
            initializeContentInfo()
        }

        if (position >= contentLength && contentLength > 0) {
            return -1
        }

        val cachedData = readFromCache(position, size)
        if (cachedData != null) {
            System.arraycopy(cachedData, 0, buffer, offset, cachedData.size)
            return cachedData.size
        }

        readCount++
        return fetchChunk(position, buffer, offset, size)
    }

    override fun getSize(): Long {
        if (!initialized) {
            initializeContentInfo()
        }
        return contentLength
    }

    override fun close() {
        synchronized(cachedChunks) {
            cachedChunks.clear()
        }
        Log.d(TAG, "Data source closed")
    }

    private fun readFromCache(position: Long, size: Int): ByteArray? {
        synchronized(cachedChunks) {
            val chunk = cachedChunks.find { position >= it.start && position < it.end } ?: return null

            chunk.lastAccessed = System.currentTimeMillis()

            val cacheOffset = (position - chunk.start).toInt()
            val bytesToCopy = minOf(size, (chunk.end - position).toInt())

            return ByteArray(bytesToCopy).apply {
                System.arraycopy(chunk.data, cacheOffset, this, 0, bytesToCopy)
            }
        }
    }

    private fun storeInCache(position: Long, data: ByteArray) {
        synchronized(cachedChunks) {
            val newChunk = CachedChunk(
                data = data,
                start = position,
                end = position + data.size
            )

            cachedChunks.add(newChunk)

            if (cachedChunks.size > MAX_CACHED_CHUNKS) {
                val lruChunk = cachedChunks.minByOrNull { it.lastAccessed }
                if (lruChunk != null) {
                    cachedChunks.remove(lruChunk)
                    Log.v(TAG, "Evicted LRU chunk at position ${lruChunk.start / 1024}KB")
                }
            }

            Log.d(TAG, "Cached chunk at ${position / 1024}KB (${cachedChunks.size}/${MAX_CACHED_CHUNKS} chunks)")
        }
    }

    private fun initializeContentInfo() {
        if (initialized) return

        try {
            val request = buildRangeRequest(0, 0)

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 206) {
                    contentLength = extractContentLength(response.header("Content-Range"))
                        ?: response.body?.contentLength()
                        ?: -1

                    Log.d(TAG, "Initialized - Content-Length: ${contentLength / 1024}KB")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get content info: ${e.message}")
        } finally {
            initialized = true
        }
    }

    private fun fetchChunk(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        try {
            val chunkSize = calculateChunkSize()
            val endPosition = calculateEndPosition(position, chunkSize)

            val request = buildRangeRequest(position, endPosition)
            val fetchStartTime = System.currentTimeMillis()

            Log.d(TAG, "Fetching chunk: bytes $position-$endPosition (${(endPosition - position + 1) / 1024}KB, read #$readCount)")

            return httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    Log.e(TAG, "HTTP error: ${response.code}")
                    return@use -1
                }

                processResponse(response, position, buffer, offset, size, fetchStartTime)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Read error at position $position: ${e.message}", e)
            return -1
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error at position $position: ${e.message}", e)
            return -1
        }
    }

    private fun processResponse(
        response: okhttp3.Response,
        position: Long,
        buffer: ByteArray,
        offset: Int,
        size: Int,
        fetchStartTime: Long
    ): Int {
        updateContentLengthFromResponse(response)

        val chunkData = response.body?.byteStream()?.readBytes()
            ?: throw IOException("Empty response body")

        val fetchDuration = System.currentTimeMillis() - fetchStartTime
        updateBitrateEstimation(chunkData.size.toLong(), fetchDuration)

        storeInCache(position, chunkData)

        val bytesToCopy = minOf(size, chunkData.size)
        System.arraycopy(chunkData, 0, buffer, offset, bytesToCopy)

        Log.d(TAG, "Fetched ${chunkData.size / 1024}KB in ${fetchDuration}ms, returned $bytesToCopy bytes")

        return if (bytesToCopy == 0) -1 else bytesToCopy
    }

    private fun calculateChunkSize(): Long {
        return if (readCount < PLAYBACK_THRESHOLD_READS) {
            INITIAL_CHUNK_SIZE.toLong()
        } else {
            (estimatedBitrate / 8 * TARGET_BUFFER_SECONDS)
                .coerceIn(MIN_CHUNK_SIZE, MAX_CHUNK_SIZE)
                .toLong()
        }
    }

    private fun calculateEndPosition(position: Long, chunkSize: Long): Long {
        return if (contentLength > 0) {
            minOf(position + chunkSize - 1, contentLength - 1)
        } else {
            position + chunkSize - 1
        }
    }

    private fun updateBitrateEstimation(fetchSize: Long, fetchDuration: Long) {
        if (fetchDuration <= 0 || contentLength <= 0 || readCount < PLAYBACK_THRESHOLD_READS) {
            return
        }

        val calculatedBitrate = (contentLength * 8 * 1000 / ASSUMED_SONG_DURATION_SEC).toInt()
        estimatedBitrate = (estimatedBitrate * 0.7 + calculatedBitrate * 0.3).toInt()

        if (readCount == PLAYBACK_THRESHOLD_READS) {
            Log.d(TAG, "Playback started - estimated bitrate: ${estimatedBitrate / 1000}kbps")
        }
    }

    private fun buildRangeRequest(start: Long, end: Long): Request {
        return Request.Builder()
            .url(url)
            .apply {
                authToken?.let { addHeader("Authorization", "Bearer $it") }
                addHeader("Range", "bytes=$start-$end")
            }
            .build()
    }

    private fun extractContentLength(contentRange: String?): Long? {
        if (contentRange == null) return null

        val parts = contentRange.substringAfter("bytes ").split("/")
        return if (parts.size == 2) {
            parts[1].toLongOrNull()
        } else {
            null
        }
    }

    private fun updateContentLengthFromResponse(response: okhttp3.Response) {
        if (contentLength >= 0) return

        val newLength = extractContentLength(response.header("Content-Range"))
        if (newLength != null && newLength > 0) {
            contentLength = newLength
            Log.d(TAG, "Content-Length from Content-Range: ${contentLength / 1024}KB")
        }
    }
}
