package tv.nomercy.app.shared.stores.musicPlayer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.nomercy.app.shared.models.PlaylistItem

class MusicPlayerQueue {
    private val _queue = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val queue: StateFlow<List<PlaylistItem>> = _queue.asStateFlow()

    private val _backlog = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val backlog: StateFlow<List<PlaylistItem>> = _backlog.asStateFlow()

    private val _currentSong = MutableStateFlow<PlaylistItem?>(null)
    val currentSong: StateFlow<PlaylistItem?> = _currentSong.asStateFlow()

    private val _isShuffling = MutableStateFlow(false)
    val isShuffling: StateFlow<Boolean> = _isShuffling.asStateFlow()

    private val _repeatState = MutableStateFlow(RepeatState.OFF)
    val repeatState: StateFlow<RepeatState> = _repeatState.asStateFlow()

    fun getQueue(): List<PlaylistItem> = _queue.value

    fun setQueue(items: List<PlaylistItem>) {
        _queue.value = items.map { it.copy() }
    }

    fun addToQueue(item: PlaylistItem) {
        _queue.value = _queue.value + item.copy()
    }

    fun addToQueueNext(item: PlaylistItem) {
        _queue.value = listOf(item.copy()) + _queue.value
    }

    fun pushToQueue(items: List<PlaylistItem>) {
        _queue.value = _queue.value + items.map { it.copy() }
    }

    fun removeFromQueue(item: PlaylistItem) {
        _queue.value = _queue.value.filterNot { it.id == item.id }
    }

    fun clearQueue() {
        _queue.value = emptyList()
    }

    fun getBacklog(): List<PlaylistItem> = _backlog.value

    fun setBacklog(items: List<PlaylistItem>) {
        _backlog.value = items.map { it.copy() }
    }

    fun addToBacklog(item: PlaylistItem?) {
        if (item == null) return
        _backlog.value = _backlog.value + item.copy()
    }

    fun pushToBacklog(items: List<PlaylistItem>) {
        _backlog.value = _backlog.value + items.map { it.copy() }
    }

    fun removeFromBacklog(item: PlaylistItem) {
        _backlog.value = _backlog.value.filterNot { it.id == item.id }
    }

    fun clearBacklog() {
        _backlog.value = emptyList()
    }

    fun setCurrentSong(item: PlaylistItem?) {
        _currentSong.value = item
    }

    fun getCurrentSong(): PlaylistItem? = _currentSong.value

    fun getNextSong(): PlaylistItem? {
        val queue = _queue.value
        if (queue.isEmpty()) return null

        return if (_isShuffling.value) {
            queue.randomOrNull()
        } else {
            queue.firstOrNull()
        }
    }

    fun getPreviousSong(): PlaylistItem? {
        return _backlog.value.lastOrNull()
    }

    fun setShuffle(shuffle: Boolean) {
        _isShuffling.value = shuffle
    }

    fun setRepeat(repeat: RepeatState) {
        _repeatState.value = repeat
    }

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
}
