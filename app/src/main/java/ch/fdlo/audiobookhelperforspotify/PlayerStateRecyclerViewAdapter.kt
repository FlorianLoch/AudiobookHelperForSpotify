package ch.fdlo.audiobookhelperforspotify

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.player_state_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class PlayerStateRecyclerViewAdapter(private val playerStates: PlayerStateBackend) : RecyclerView.Adapter<PlayerStateRecyclerViewAdapter.ViewHolder>() {

    private val storeClickListener: View.OnClickListener
    private val loadClickListener: View.OnClickListener
    private val deleteClickListener: View.OnClickListener

    companion object {
        // TODO Enhance this function to show minutes and seconds
        fun formatTime(millis: Long): Int {
            return Math.floorDiv(millis, 1000.toLong()).toInt()
        }
    }

    init {
        notifyDataSetChanged()

        playerStates.setOnChangeListener(this)

        storeClickListener = View.OnClickListener { v ->
            val index = (v.parent as View).tag as Int
            async(UI) {
                // TODO Add spinner etc.
                playerStates.storePlayerState(index)
            }
        }
        loadClickListener = View.OnClickListener { v ->
            val index = (v.parent as View).tag as Int
            playerStates.restorePlayerState(index)
        }
        deleteClickListener = View.OnClickListener { v ->
            val index = (v.parent as View).tag as Int
            playerStates.delete(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_state_item, parent, false)

        with(view) {
            btn_store.setOnClickListener(storeClickListener)
            btn_load.setOnClickListener(loadClickListener)
            btn_delete.setOnClickListener(deleteClickListener)
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trackInfo = playerStates[position]

        with (holder.view) {
            tag = position

            tv_album.text = trackInfo.album
            tv_track.text = trackInfo.trackName
            tv_position.text = "${formatTime(trackInfo.position)} / ${formatTime(trackInfo.duration)}"
        }
    }

    override fun getItemCount(): Int = playerStates.size()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}
