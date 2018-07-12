package ch.fdlo.audiobookhelperforspotify

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.player_state_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

class PlayerStateRecyclerViewAdapter(private val playerStatesBackend: PlayerStateBackend, private val albumArtCache: AlbumArtCache) : RecyclerView.Adapter<PlayerStateRecyclerViewAdapter.ViewHolder>() {

    companion object {
        // TODO Enhance this function to show minutes and seconds
        fun formatTime(millis: Long): Int {
            return Math.floorDiv(millis, 1000.toLong()).toInt()
        }
    }

    init {
        playerStatesBackend.setOnChangeListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_state_item, parent, false)
        val viewHolder = ViewHolder(view)

        with(view) {
            btn_store.setOnClickListener {
                async(UI) {
                    Log.d("ABHfS", Integer.toString(viewHolder.adapterPosition))
                    playerStatesBackend.storePlayerState(viewHolder.adapterPosition)
                }
            }
            btn_load.setOnClickListener {
                Log.d("ABHfS", Integer.toString(viewHolder.adapterPosition))
                playerStatesBackend.restorePlayerState(viewHolder.adapterPosition)
            }
            btn_delete.setOnClickListener {
                Log.d("ABHfS", Integer.toString(viewHolder.adapterPosition))
                playerStatesBackend.delete(viewHolder.adapterPosition)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playerState = playerStatesBackend[position]

        with (holder.view) {
            tv_album.text = playerState.album
            tv_track.text = playerState.trackName
            tv_position.text = "${formatTime(playerState.position)} / ${formatTime(playerState.duration)}"
            img_cover.tag = playerState.coverURI

            async(UI) {
                val albumArtBitmap = albumArtCache.fetchCoverForAlbum(playerState.coverURI).await()

                // Check whether a new PlayerState has been assigned to the (recyclable) view since we started fetching this album art.
                // Due to race conditions (differing download times etc.) this could otherwise lead to wrongly assigned cover images.
                // This check only assures the same imageURI, but as this one is unique it points to the same cover art (in the worst case an image
                // would get replaced by an identical copy)
                // If the assigned album changed we simply do nothing and wait for the right image to be fetched (or leave it unchanged in
                // case it already loaded)
                synchronized(this@PlayerStateRecyclerViewAdapter) {
                    if (playerState.coverURI == img_cover.tag) {
                        img_cover.setImageBitmap(albumArtBitmap)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = playerStatesBackend.size()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}
