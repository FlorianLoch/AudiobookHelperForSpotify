package ch.fdlo.audiobookhelperforspotify

data class PlayerState(val trackURI: String, val position: Long, val album: String, val trackName: String, val duration: Long)