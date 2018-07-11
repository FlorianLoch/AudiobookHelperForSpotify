package ch.fdlo.audiobookhelperforspotify

import kotlinx.coroutines.experimental.Deferred

interface PlayerController {
    fun suspendPlayerAndGetState(): Deferred<PlayerState>
    fun resumePlayerAtState(state: PlayerState): Deferred<Unit>
}
