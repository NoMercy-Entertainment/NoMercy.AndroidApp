package tv.nomercy.app.shared.stores.musicPlayer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.nomercy.app.shared.models.PlaylistItem

/**
 * Manages the music player queue, backlog, current song, shuffle, and repeat state.
 */
class MusicPlayerQueue {
    // region: Queue
    private val _queue = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val queue: StateFlow<List<PlaylistItem>> = _queue.asStateFlow()

    /** Returns a copy of the current queue. */
    fun getQueue(): List<PlaylistItem> = _queue.value

    /** Sets the queue to a copy of the given items. */
    fun setQueue(items: List<PlaylistItem>) {
        _queue.value = items.map { it.copy() }
    }

    /** Adds an item to the end of the queue. */
    fun addToQueue(item: PlaylistItem) {
        _queue.value = _queue.value + item.copy()
    }

    /** Adds an item to the front of the queue. */
    fun addToQueueNext(item: PlaylistItem) {
        _queue.value = listOf(item.copy()) + _queue.value
    }

    /** Adds multiple items to the end of the queue. */
    fun pushToQueue(items: List<PlaylistItem>) {
        _queue.value = _queue.value + items.map { it.copy() }
    }

    /** Removes an item from the queue by id. */
    fun removeFromQueue(item: PlaylistItem) {
        _queue.value = _queue.value.filterNot { it.id == item.id }
    }

    /** Clears the queue. */
    fun clearQueue() {
        _queue.value = emptyList()
    }
    // endregion

    // region: Backlog
    private val _backlog = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val backlog: StateFlow<List<PlaylistItem>> = _backlog.asStateFlow()

    /** Returns a copy of the current backlog. */
    fun getBacklog(): List<PlaylistItem> = _backlog.value

    /** Sets the backlog to a copy of the given items. */
    fun setBacklog(items: List<PlaylistItem>) {
        _backlog.value = items.map { it.copy() }
    }

    /** Adds an item to the end of the backlog. */
    fun addToBacklog(item: PlaylistItem?) {
        if (item == null) return
        _backlog.value = _backlog.value + item.copy()
    }

    /** Adds multiple items to the end of the backlog. */
    fun pushToBacklog(items: List<PlaylistItem>) {
        _backlog.value = _backlog.value + items.map { it.copy() }
    }

    /** Removes an item from the backlog by id. */
    fun removeFromBacklog(item: PlaylistItem) {
        _backlog.value = _backlog.value.filterNot { it.id == item.id }
    }

    /** Clears the backlog. */
    fun clearBacklog() {
        _backlog.value = emptyList()
    }
    // endregion

    // region: Current Song
    private val _currentSong = MutableStateFlow<PlaylistItem?>(null)
    val currentSong: StateFlow<PlaylistItem?> = _currentSong.asStateFlow()

    /** Sets the current song. */
    fun setCurrentSong(item: PlaylistItem?) {
        _currentSong.value = item
    }

    /** Returns the current song. */
    fun getCurrentSong(): PlaylistItem? = _currentSong.value
    // endregion

    // region: Shuffle & Repeat
    private val _isShuffling = MutableStateFlow(false)
    val isShuffling: StateFlow<Boolean> = _isShuffling.asStateFlow()

    private val _repeatState = MutableStateFlow(RepeatState.OFF)
    val repeatState: StateFlow<RepeatState> = _repeatState.asStateFlow()

    /** Enables or disables shuffle mode. */
    fun setShuffle(shuffle: Boolean) {
        _isShuffling.value = shuffle
    }

    /** Sets the repeat state. */
    fun setRepeat(repeat: RepeatState) {
        _repeatState.value = repeat
    }
    // endregion

    // region: Navigation
    /** Returns the next song in the queue, considering shuffle. */
    fun getNextSong(): PlaylistItem? {
        val queue = _queue.value
        if (queue.isEmpty()) return null
        return if (_isShuffling.value) {
            queue.randomOrNull()
        } else {
            queue.firstOrNull()
        }
    }

    /** Returns the previous song from the backlog. */
    fun getPreviousSong(): PlaylistItem? {
        return _backlog.value.lastOrNull()
    }

    /**
     * Plays the given track and optionally sets the queue to the tracks after it.
     * @param track The track to play.
     * @param tracks The full list of tracks (optional).
     */
    fun playTrack(track: PlaylistItem, tracks: List<PlaylistItem>? = null) {
        setCurrentSong(track)
        tracks?.let {
            val index = it.indexOfFirst { t -> t.id == track.id }
            if (index != -1) {
                val afterIndex = it.subList(index + 1, it.size)
                val beforeIndex = it.subList(0, index)
                setQueue(afterIndex + beforeIndex)
            }
        }
    }

    /** Moves to the next song, updating backlog and queue as needed. */
    fun moveToNext(): PlaylistItem? {
        val current = _currentSong.value
        current?.let { addToBacklog(it) }

        val queue = _queue.value
        if (queue.isEmpty()) {
            // If queue is empty and repeat all, restore from backlog
            if (_repeatState.value == RepeatState.ALL && _backlog.value.isNotEmpty()) {
                setQueue(_backlog.value)
                setBacklog(emptyList())
            } else {
                setCurrentSong(null)
                return null
            }
        }

        val nextItem = getNextSong()
        nextItem?.let {
            setCurrentSong(it)
            removeFromQueue(it)
        }
        return nextItem
    }

    /** Moves to the previous song, updating backlog and queue as needed. */
    fun moveToPrevious(): PlaylistItem? {
        val previousSong = getPreviousSong()
        return if (previousSong != null) {
            val current = _currentSong.value
            current?.let { addToQueueNext(it) }
            setCurrentSong(previousSong)
            removeFromBacklog(previousSong)
            previousSong
        } else {
            null
        }
    }
    // endregion
}
