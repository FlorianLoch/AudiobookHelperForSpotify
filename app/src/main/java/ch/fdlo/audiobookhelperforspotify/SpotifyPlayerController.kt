package ch.fdlo.audiobookhelperforspotify

import android.util.Log
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

class SpotifyPlayerController(private val spotifyRemote: SpotifyAppRemote) : PlayerController {
    override fun suspendPlayerAndGetState(): Deferred<PlayerState> {
        val promise = CompletableDeferred<PlayerState>()

        with(spotifyRemote.playerApi.playerState) {
            setResultCallback {
                if (it == null) {
                    promise.completeExceptionally(Throwable("PlayerState could not be retrieved from Spotify!"))
                    return@setResultCallback
                }

                val state = PlayerState.fromSpotifyPlayerState(it)

                it.track.imageUri

                promise.complete(state)
            }
            setErrorCallback {
                promise.completeExceptionally(it)
            }
        }

        return promise
    }

//    override suspend fun resumePlayerAtState(state: PlayerState) {
//        jumpStepwiseToTrack(state)
//    }

    override suspend fun resumePlayerAtState(state: PlayerState) {
        val promise = CompletableDeferred<Unit>()

        with (spotifyRemote.playerApi) {
            // TODO Perhaps we should start the album first to make sure the track is played in
            // the right context (would be necessary in case a track is included in multiple albums - if this can actually happen,
            // check whether trackid is unique and cannot be used in different albums
            with (play(state.albumURI)) {
                setResultCallback {
                    with (play(state.trackURI)) {
                        setResultCallback {
                            with (seekTo(state.position)) {
                                setResultCallback {
                                    promise.complete(Unit)
                                }
                                setErrorCallback {
                                    promise.completeExceptionally(Throwable("Could not restore state:\n$state\nCan not seek to last stored position!"))
                                }
                            }
                        }
                        setErrorCallback {
                            promise.completeExceptionally(Throwable("Could not restore state:\n$state\nCan not play this track!"))
                        }
                    }
                }
                setErrorCallback {
                    promise.completeExceptionally(Throwable("Could not restore state:\n$state\nCan not queue the album!"))
                }
            }
        }

        promise.await()
    }

    private suspend fun jumpStepwiseToTrack(targetPlayerState: PlayerState) {
        playAlbum(targetPlayerState.albumURI).await()

        var currentPlayerState = suspendPlayerAndGetState().await()
        while (currentPlayerState.trackURI != targetPlayerState.trackURI) {
            Log.d("ABHfS", "At track: ${currentPlayerState.trackURI}, skip")
            skipNext().await()
            currentPlayerState = suspendPlayerAndGetState().await()
        }

        seekTo(targetPlayerState.position).await()
    }

    private fun playAlbum(albumURI: String): Deferred<Unit> {
        val promise = CompletableDeferred<Unit>()

        with (spotifyRemote.playerApi) {
            // TODO Perhaps we should start the album first to make sure the track is played in
            // the right context (would be necessary in case a track is included in multiple albums - if this can actually happen,
            // check whether trackid is unique and cannot be used in different albums
            with (play(albumURI)) {
                setResultCallback {
                    promise.complete(Unit)
                }
                setErrorCallback {
                    promise.completeExceptionally(Throwable("Could not play album ($albumURI)!"))
                }
            }
        }

        return promise
    }

    private fun seekTo(position: Long): CompletableDeferred<Unit> {
        val promise = CompletableDeferred<Unit>()

        with (spotifyRemote.playerApi.seekTo(position)) {
            setResultCallback {
                promise.complete(Unit)
            }
            setErrorCallback {
                promise.completeExceptionally(Throwable("Could not seek to position ($position)!"))
            }
        }

        return promise
    }

    private fun skipNext(): CompletableDeferred<Unit> {
        val promise = CompletableDeferred<Unit>()

        with (spotifyRemote.playerApi.skipNext()) {
            setResultCallback {
                promise.complete(Unit)
            }
            setErrorCallback {
                promise.completeExceptionally(it)
            }
        }

        return promise
    }
}
