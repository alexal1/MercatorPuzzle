package com.alex_aladdin.mercatorpuzzle.animators

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.PointF
import com.alex_aladdin.mercatorpuzzle.draw_threads.MoveDrawThread

class MoveCountriesAnimator(moveDrawThread: MoveDrawThread) : CountriesAnimator(moveDrawThread) {

    companion object {

        const val DURATION: Long = 300

    }

    private val animator = ValueAnimator.ofFloat(0f, 1f)

    override fun animate(onFinish: (() -> Unit)?) {
        (drawThread as MoveDrawThread).apply {
            val startPoint: PointF = projection.toScreenLocation(country.currentCenter)
            val endPoint: PointF = projection.toScreenLocation(country.targetCenter)

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
                    touchPoint = null
                    synchronized(country) {
                        country.currentCenter = country.targetCenter
                    }
                    isInProgress = false
                    onFinish?.invoke()
                }

            })

            animator.duration = DURATION
            animator.start()
            isInProgress = true
        }
    }

    override fun cancel() {
        animator.cancel()
    }

}