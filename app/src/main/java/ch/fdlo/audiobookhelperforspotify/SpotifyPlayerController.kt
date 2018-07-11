package ch.fdlo.audiobookhelperforspotify

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

                promise.complete(state)
            }
            setErrorCallback {
                promise.completeExceptionally(it)
            }
        }

        return promise
    }

    override fun resumePlayerAtState(state: PlayerState): Deferred<Unit> {
        val promise = CompletableDeferred<Unit>()

        with (spotifyRemote.playerApi) {
            // TODO Perhaps we should start the album first to make sure the track is played in
            // the right context (would be necessary in case a track is included in multiple albums - if this can actually happen,
            // check whether trackid is unique and cannot be used in different albums

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

        return promise
    }
}
