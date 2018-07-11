package ch.fdlo.audiobookhelperforspotify

class PlayerStateBackend(private val playerControl: PlayerController, private val serializerDeserializer: SerializerDeserializer, serializedData: String) {
    private val list: MutableList<PlayerState>
    private lateinit var playerStateRecyclerViewAdapter: PlayerStateRecyclerViewAdapter

    init {
        val container = serializerDeserializer.deserialize(serializedData, PlayerStateSerializationContainer::class.java)

        // Improve this, do some asserts or do not use gson?

        list = container.playerStateList!!
    }

    fun setOnChangeListener(playerStateRecyclerViewAdapter: PlayerStateRecyclerViewAdapter) {
        this.playerStateRecyclerViewAdapter = playerStateRecyclerViewAdapter
    }

    suspend fun storePlayerState(index: Int) {
        list[index] = playerControl.suspendPlayerAndGetState().await()
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

    fun serialize(): String {
        return serializerDeserializer.serialize(list)
    }



    class PlayerStateSerializationContainer {
        var configVersion: Int? = null
        var playerStateList: ArrayList<PlayerState>? = null
    }
}