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
class PlayerStateRecyclerViewAdapter(private val playerStates: PlayerStateBackend, private val albumArtCache: AlbumArtCache) : RecyclerView.Adapter<PlayerStateRecyclerViewAdapter.ViewHolder>() {

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
        val playerState = playerStates[position]

        with (holder.view) {
            tag = position

            tv_album.text = playerState.album
            tv_track.text = playerState.trackName
            tv_position.text = "${formatTime(playerState.position)} / ${formatTime(playerState.duration)}"
            img_cover.tag = playerState.coverURI

            async(UI) {
                val albumArtBitmap = albumArtCache.fetchCoverForAlbum(playerState.coverURI).await()

                // Check whether a new PlayerState has been assigned to the (recycable) view since we started fetching this album art
                // Due to race conditions (differing download times etc.) this could otherwise lead to wrongly assigned cover images
                // This check only assures the same albumURI, but as this one is unique it should regard to the same cover art so the
                // mistake would not become visible.
                // If the assigned album changed we simply do nothing and wait for the right image to be fetched (or leave it unchanged in
                // case it already loaded)
                // TODO Update this text
                synchronized(this@PlayerStateRecyclerViewAdapter) {
                    if (playerState.coverURI == img_cover.tag) {
                        img_cover.setImageBitmap(albumArtBitmap)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = playerStates.size()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}
