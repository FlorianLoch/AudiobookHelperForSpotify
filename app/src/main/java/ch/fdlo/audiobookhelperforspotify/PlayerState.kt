package ch.fdlo.audiobookhelperforspotify

class PlayerState(val trackURI: String,
                  val trackName: String,
                  val albumURI: String,
                  val album: String,
                  val artist: String,
                  val coverURI: String,
                  val position: Long,
                  val duration: Long) {
    companion object {
        fun fromSpotifyPlayerState(spotifyPlayerState: com.spotify.protocol.types.PlayerState): PlayerState {
            val position = spotifyPlayerState.playbackPosition

            with(spotifyPlayerState.track) {
                return PlayerState(uri, name, album.uri, album.name, artist.name, imageUri.raw, position, duration)
            }
        }
    }
}