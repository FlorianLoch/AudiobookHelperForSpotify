package ch.fdlo.audiobookhelperforspotify

class PlayerState(val trackURI: String, val album: String, val albumURI: String, val trackName: String,  val position: Long, val duration: Long) {
    companion object {
        fun fromSpotifyPlayerState(spotifyPlayerState: com.spotify.protocol.types.PlayerState): PlayerState {
            val position = spotifyPlayerState.playbackPosition

            with(spotifyPlayerState.track) {
                return PlayerState(uri, album.name, album.uri, name, position, duration)
            }
        }
    }
}