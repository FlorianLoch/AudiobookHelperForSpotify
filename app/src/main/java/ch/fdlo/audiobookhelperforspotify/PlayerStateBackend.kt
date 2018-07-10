package ch.fdlo.audiobookhelperforspotify

import com.google.gson.Gson

class PlayerStateBackend(private val playerControl: PlayerController, private val serializerDeserializer: SerializerDeserializer) {
    private val list = mutableListOf<PlayerState>()
    private lateinit var positionRecyclerViewAdapter: PositionRecyclerViewAdapter

    fun setOnChangeListener(positionRecyclerViewAdapter: PositionRecyclerViewAdapter) {
        this.positionRecyclerViewAdapter = positionRecyclerViewAdapter
    }

    fun storePlayerState(index: Int) {
        list[index] = getCurrentPlayerState()
    }

    fun restorePlayerState(index: Int) {

    }

    fun delete(index: Int) {
        list.removeAt(index)

        // TODO Add asserts (also in all other methods)

        positionRecyclerViewAdapter.notifyItemRemoved(index)
    }

    fun addCurrentPlayerState() {
        list.add(playerControl.suspendPlayerAndGetState())
        positionRecyclerViewAdapter.notifyItemRangeInserted(list.size - 1, 1)
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

    fun deserialize(s: String) {
        list = serializerDeserializer.deserialize(s, ArrayList<PlayerState>.javaClass)
    }
}