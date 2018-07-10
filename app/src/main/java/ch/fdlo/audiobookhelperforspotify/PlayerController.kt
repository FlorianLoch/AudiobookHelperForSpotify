package ch.fdlo.audiobookhelperforspotify

interface PlayerController {
    fun suspendPlayerAndGetState(): PlayerState
    fun resumePlayerAtState(state: PlayerState)
}
