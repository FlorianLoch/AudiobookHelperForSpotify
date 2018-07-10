package ch.fdlo.audiobookhelperforspotify

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.PlayerState;
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val REDIRECT_URI = "ch.fdlo.audiobookhelperforspotify://spotify_callback"

    private lateinit var spotifyRemote: SpotifyAppRemote
    lateinit var currentPlayerState : PlayerState
    lateinit var btnPlayPause : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spotifyController = SpotifyPlayerController.getInstance()
        val gsonBackend = GsonBackend.getInstance()
        val backend = PlayerStateBackend(spotifyController, gsonBackend, loadConfigFromDisk())
        val listAdapter = PlayerStateRecyclerViewAdapter(backend)
        backend.setOnChangeListener(listAdapter)

        fab_add_current_state.setOnClickListener() {
            backend.addCurrentPlayerState()
        }

        rv_position_list.adapter = listAdapter

        val connectionParams = ConnectionParams.Builder(getString(R.string.spotify_client_id))
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build()

        Log.d("ABHfS", "Trying to connec to the Spotify app...")

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    spotifyRemote = spotifyAppRemote
                    Log.d("ABHfS", "Connected! Yay!")

                    spotifyRemote.playerApi.playerState.setResultCallback(this@MainActivity::handlePlayerState)

                    spotifyRemote.playerApi.subscribeToPlayerState().setEventCallback(this@MainActivity::handlePlayerState)
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("ABHfS", "Could not connect to Spotify!", throwable)

                    btnPlayPause.isEnabled = false

                    Toast.makeText(applicationContext, "Could not connect to Spotify!", Toast.LENGTH_LONG)

                    // Add better error handling, seems like it happends sometime that we cannot connect to the spotify app and restarting it helps...
                }
            }
        )

        btnPlayPause.setOnClickListener(this::handlePausePlayButtonClick)
    }

    private fun handlePlayerState(state: PlayerState) {
        Log.e("ABHfS", "PlayerState changed!")

        btnPlayPause.text = resources.getString(
            when (state.isPaused) {
                true -> R.string.play
                false -> R.string.pause
        })

        currentPlayerState = state

        btnPlayPause.isEnabled = true
    }

    private fun handlePausePlayButtonClick(view: View) {
        with (spotifyRemote.playerApi) {
            playerState.setResultCallback {
                if (it.isPaused) {
                    val trackInformation = loadPosition()

                    with (this) {
                        play(trackInformation.trackURI).setResultCallback {
                            seekTo(trackInformation.position)
                        }
                    }

                    Log.d("ABHfS", "Try to continue track ${trackInformation.trackURI} at ${trackInformation.position}")
                }
                else {
                    this.pause() // TODO handle result of this call

                    storePosition(TrackInformation(it.track.uri, it.playbackPosition, ))

                    Log.d("ABHfS", "Stopped track ${it.track.uri} at ${it.playbackPosition}")
                }
            }
        }
    }

    private fun storePosition(playerState: PlayerState) {
        with (getPreferences(Context.MODE_PRIVATE).edit()) {
            putString("trackURI", playerState.trackURI)
            putLong("trackPosition", playerState.position)
            apply()
        }
    }

    private fun loadPosition(): PlayerState {
        with (getPreferences(Context.MODE_PRIVATE)) {
            if (contains("trackURI").not()) {
                throw Throwable("No position stored yet!")
            }
            return PlayerState(this.getString("trackURI", ""), getLong("trackPosition", 0), )
        }
    }
}
