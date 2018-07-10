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

    fun storePlayerState(index: Int) {
        list[index] = playerControl.suspendPlayerAndGetState()
    }

    fun restorePlayerState(index: Int) {
        playerControl.resumePlayerAtState(list[index])
    }

    fun delete(index: Int) {
        list.removeAt(index)

        // TODO Add asserts (also in all other methods)

        playerStateRecyclerViewAdapter.notifyItemRemoved(index)
    }

    fun addCurrentPlayerState() {
        list.add(playerControl.suspendPlayerAndGetState())
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
        var playerStateList: MutableList<PlayerState>? = null
    }
}