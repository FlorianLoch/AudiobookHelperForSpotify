package ch.fdlo.audiobookhelperforspotify

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

fun fadeInView(view: View) {
    animateView(view, View.VISIBLE)
}

fun fadeOutView(view: View) {
    animateView(view, View.GONE)
}

// Taken from https://stackoverflow.com/a/29542951 and slightly modified
fun animateView(view: View, toVisibility: Int, duration: Long = 50, toAlpha: Float = 0.4f) {
    view.bringToFront()

    val show = toVisibility == View.VISIBLE
    if (show) {
        view.alpha = 0f
    }
    view.visibility = View.VISIBLE
    with (view.animate()) {
        setDuration(duration)
        alpha(if (show) toAlpha else 0f)
        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = toVisibility
            }
        })
    }
}