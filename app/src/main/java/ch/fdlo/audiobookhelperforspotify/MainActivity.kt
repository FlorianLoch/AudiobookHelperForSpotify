package ch.fdlo.audiobookhelperforspotify

import android.content.Context
import android.graphics.ColorSpace
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

import com.spotify.protocol.types.PlayerState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


class MainActivity : AppCompatActivity() {
    private val REDIRECT_URI = "ch.fdlo.audiobookhelperforspotify://spotify_callback"

    var backend: PlayerStateBackend? = null // TODO Check what lateinit does resp. if it would improve things here
    lateinit var persistence: PlayerStatePersistence


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_position_list.isEnabled = false

        persistence = PlayerStatePersistence.load(this)

        fab_add_current_state.setOnClickListener() {
            async(UI) {
                // TODO Add a spinner or some other animation
                if (backend != null) {
                    backend!!.addCurrentPlayerState()
                    return@async
                }

                Toast.makeText(applicationContext, "Not yet connected with the spotify app!", Toast.LENGTH_LONG).show()
            }
        }

        val connectionParams = ConnectionParams.Builder("b68c9b6c29464768bed65249bd3204b0")
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build()

        Log.d("ABHfS", "Trying to connect to the Spotify app...")

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    Log.d("ABHfS", "Connected! Yay!")

                    try {
                        val spotifyController = SpotifyPlayerController(spotifyAppRemote)
                        backend = PlayerStateBackend(spotifyController, persistence)
                        val listAdapter = PlayerStateRecyclerViewAdapter(backend!!)
                        backend!!.setOnChangeListener(listAdapter)


                        rv_position_list.adapter = listAdapter
                    } catch (e: Throwable) {
                        Toast.makeText(applicationContext, "Could not initialize backend!", Toast.LENGTH_LONG).show()
                        Log.e("ABHfS", "Could not initialize backend!", e)
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("ABHfS", "Could not connect to Spotify!", throwable)

                    Toast.makeText(applicationContext, "Could not connect to Spotify!", Toast.LENGTH_LONG).show()

                    // TODO Better error handling
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()

        persistence.save(this)
    }
}
