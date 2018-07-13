package ch.fdlo.audiobookhelperforspotify

import kotlinx.coroutines.experimental.Deferred

interface PlayerController {
    fun suspendPlayerAndGetState(): Deferred<PlayerState>
    suspend fun resumePlayerAtState(state: PlayerState)
}
