package ch.fdlo.audiobookhelperforspotify

class PlayerStateBackend(private val playerControl: PlayerController, persistence: PlayerStatePersistence) {
    private val list: MutableList<PlayerState>
    private lateinit var playerStateRecyclerViewAdapter: PlayerStateRecyclerViewAdapter

    init {
        list = persistence.playerStateList!!
    }

    fun setOnChangeListener(playerStateRecyclerViewAdapter: PlayerStateRecyclerViewAdapter) {
        this.playerStateRecyclerViewAdapter = playerStateRecyclerViewAdapter
    }

    suspend fun storePlayerState(index: Int) {
        list[index] = playerControl.suspendPlayerAndGetState().await()
        playerStateRecyclerViewAdapter.notifyItemChanged(index)
    }

    fun restorePlayerState(index: Int) {
        playerControl.resumePlayerAtState(list[index])
    }

    fun delete(index: Int) {
        list.removeAt(index)

        // TODO Add asserts (also in all other methods)

        playerStateRecyclerViewAdapter.notifyItemRemoved(index)
    }

    suspend fun addCurrentPlayerState() {
        list.add(playerControl.suspendPlayerAndGetState().await())
        playerStateRecyclerViewAdapter.notifyItemRangeInserted(list.size - 1, 1)
    }

    fun size(): Int {
        return list.size
    }

    operator fun get(index: Int): PlayerState {
        return list[index]
    }
}