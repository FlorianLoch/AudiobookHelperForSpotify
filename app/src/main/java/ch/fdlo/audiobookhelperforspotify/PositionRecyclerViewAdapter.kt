package ch.fdlo.audiobookhelperforspotify

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import ch.fdlo.audiobookhelperforspotify.PositionListFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_position.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class PositionRecyclerViewAdapter(
        private val positions: TrackInformationStore)
    : RecyclerView.Adapter<PositionRecyclerViewAdapter.ViewHolder>() {

    private val storeClickListener: View.OnClickListener
    private val loadClickListener: View.OnClickListener
    private val deleteClickListener: View.OnClickListener


    init {
        notifyDataSetChanged()

        positions.setOnChangeListener(this)

        storeClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            positions.store(position)
        }
        loadClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            positions.load(position)
        }
        deleteClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            positions.delete(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_position, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)

        with(holder.view) {
            tag = position
        }
    }

    override fun getItemCount(): Int = positions.size()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun setContent(position: Int) {
            val trackInfo = positions[position]

            with (view) {
                tag = position

                tv_album.text = trackInfo.album
                tv_track.text = trackInfo.trackName
                tv_position.text = "${trackInfo.position} / ${trackInfo.duration}"

                btn_store.setOnClickListener(storeClickListener)
                btn_load.setOnClickListener(storeClickListener)
                btn_delete.setOnClickListener(deleteClickListener)
            }
        }
    }
}
