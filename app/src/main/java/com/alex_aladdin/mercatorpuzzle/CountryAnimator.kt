package com.alex_aladdin.mercatorpuzzle

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.PointF

/**
 * Animator that moves Country to its target location.
 */
class CountryAnimator(private val drawThread: DrawThread) {

    companion object {

        const val DURATION: Long = 300

    }

    /**
     * Flag that shows if animation is in progress now.
     */
    var isInProgress = false
        private set

    /**
     * Move Country that's held by DrawThread to its target location.
     */
    fun animate(onFinish: (() -> Unit)? = null) {
        drawThread.apply {
            val startPoint: PointF = projection.toScreenLocation(country.currentCenter)
            val endPoint: PointF = projection.toScreenLocation(country.targetCenter)

            val animator = ValueAnimator.ofFloat(0f, 1f)

            animator.addUpdateListener { animation: ValueAnimator? ->
                val value: Float = animation?.animatedValue as? Float ?: 0f
                val point = PointF(
                        startPoint.x + (endPoint.x - startPoint.x) * value,
                        startPoint.y + (endPoint.y - startPoint.y) * value
                )
                touchPoint = point
            }

            animator.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                    country.currentCenter = country.targetCenter
                    isInProgress = false
                    onFinish?.invoke()
                }

            })

            animator.duration = DURATION
            animator.start()
            isInProgress = true
        }
    }

}