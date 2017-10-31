package com.alex_aladdin.mercatorpuzzle.animators

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.PointF
import android.view.animation.OvershootInterpolator
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.draw_threads.ScaleDrawThread

class ScaleCountriesAnimator(scaleDrawThread: ScaleDrawThread) : CountriesAnimator(scaleDrawThread) {

    companion object {

        const val DURATION: Long = 1000
        const val OFFSET: Long = 500

    }

    private val animatorSet = AnimatorSet()

    override fun animate(onFinish: (() -> Unit)?) {
        (drawThread as ScaleDrawThread).apply {
            val animators = ArrayList<Animator>(scales.size)

            for ((count, country) in scales.keys.withIndex()) {
                val animator = ValueAnimator.ofFloat(0f, 1f)
                animator.addUpdateListener { animation: ValueAnimator? ->
                    val value: Float = animation?.animatedValue as? Float ?: 0f

                    scales[country] = { point ->
                        val w = MercatorApp.screen.x
                        val h = MercatorApp.screen.y
                        val w1 = w * value
                        val h1 = h * value
                        val (x0, y0) = centers[country]!!.let { Pair(it.x, it.y) }

                        PointF(x0 * (1f - value) + point.x / w * w1, y0 * (1f - value) + point.y / h * h1)
                    }
                }
                animator.startDelay = OFFSET * count

                animators.add(animator)
            }

            animatorSet.playTogether(animators)
            animatorSet.duration = DURATION
            animatorSet.interpolator = OvershootInterpolator(5f)

            animatorSet.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    scales.keys.forEach { scales[it] = { point -> point } }
                    isInProgress = false
                    onFinish?.invoke()
                }

            })

            animatorSet.start()
            isInProgress = true
        }
    }

    override fun cancel() {
        animatorSet.cancel()
    }

}