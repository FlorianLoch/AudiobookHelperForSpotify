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

    private val CONFIG_KEY = "ch.fdlo.audiobookhelperforspotify"

    private lateinit var spotifyRemote: SpotifyAppRemote
    lateinit var currentPlayerState : PlayerState
    lateinit var btnPlayPause : Button
    var backend: PlayerStateBackend? = null // TODO Check what lateinit does resp. if it would improve things here


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_position_list.isEnabled = false
        fab_add_current_state.isEnabled = false

        val connectionParams = ConnectionParams.Builder(getString(R.string.spotify_client_id))
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build()

        Log.d("ABHfS", "Trying to connect to the Spotify app...")

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    Log.d("ABHfS", "Connected! Yay!")

                    val spotifyController = SpotifyPlayerController(spotifyAppRemote)
                    val gsonBackend = GsonBackend.getInstance()
                    backend = PlayerStateBackend(spotifyController, gsonBackend, loadConfigFromDisk())
                    val listAdapter = PlayerStateRecyclerViewAdapter(backend!!)
                    backend!!.setOnChangeListener(listAdapter)

                    fab_add_current_state.isEnabled = true
                    fab_add_current_state.setOnClickListener() {
                        async(UI) {
                            // TODO Add a spinner or some other animation
                            backend!!.addCurrentPlayerState()
                        }
                    }

                    rv_position_list.adapter = listAdapter
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("ABHfS", "Could not connect to Spotify!", throwable)

                    btnPlayPause.isEnabled = false

                    Toast.makeText(applicationContext, "Could not connect to Spotify!", Toast.LENGTH_LONG).show()

                    // Add better error handling, seems like it happends sometime that we cannot connect to the spotify app and restarting it helps...
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()

        if (backend == null) {
            return
        }

        writeConfigToDisk(backend!!.serialize())
    }

    private fun handlePlayerState(state: PlayerState) {
        Log.d("ABHfS", "PlayerState changed!")

        btnPlayPause.text = resources.getString(
            when (state.isPaused) {
                true -> R.string.play
                false -> R.string.pause
        })

        currentPlayerState = state

        btnPlayPause.isEnabled = true
    }

    private fun writeConfigToDisk(serializedState: String) {
        with (getPreferences(Context.MODE_PRIVATE).edit()) {
            putString(CONFIG_KEY, serializedState)
            apply()
        }
    }

    private fun loadConfigFromDisk(): String {
        with (getPreferences(Context.MODE_PRIVATE)) {
            if (contains(CONFIG_KEY).not()) {
                Log.d("ABHfS", "No configuration stored yet! First run of the app?")
            }

            return getString(CONFIG_KEY, "")
        }
    }
}
