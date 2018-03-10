package com.alex_aladdin.mercatorpuzzle.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.alex_aladdin.mercatorpuzzle.activities.MapActivity
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import kotlinx.android.synthetic.main.fragment_lap.*
import kotlin.math.ceil

class LapFragment : Fragment() {

    companion object {

        const val TAG = "MercatorLapFragment"
        private const val COUNTER_ANIMATION_DURATION = 1600L
        private const val FINAL_ANIMATION_DURATION = 400L
        private const val FINAL_PAUSE = 200L

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contextNotNull = context ?: return

        // Set Toggle Aspect Ratio Constraint according to the screen orientation
        val constraintSet = ConstraintSet()
        constraintSet.clone(layoutFragmentLap)
        when (contextNotNull.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                constraintSet.setDimensionRatio(R.id.layoutCoins, "h,1:1")
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                constraintSet.setDimensionRatio(R.id.layoutCoins, "w,1:1")
            }
        }
        constraintSet.applyTo(layoutFragmentLap)

        setListeners()
        startAnimation()
    }

    fun onBackPressed() {
        ready()
    }

    private fun setListeners() {
        buttonMenu.setOnClickListener((activity as? MapActivity)?.onMenuClickCallback)
        buttonSkip.setOnClickListener { ready() }

        val alphaRespond = { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.alpha = 0.5f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 1.0f
            }
            false
        }
        buttonMenu.setOnTouchListener(alphaRespond)
        buttonSkip.setOnTouchListener(alphaRespond)
    }

    private fun startAnimation() {
        val contextNotNull = context ?: return
        val coins = MercatorApp.gameController.getLapIncome()
        val counterDelay = (COUNTER_ANIMATION_DURATION.toDouble() / coins).toLong()

        var currentValue = 0
        Thread({
            // Run counter animation
            while (currentValue < coins) {
                currentValue++

                textCoins?.post {
                    textCoins?.text = currentValue.toString()
                }
                progressCoins?.post {
                    progressCoins?.progress = ceil(currentValue.toFloat() / coins * 100f).toInt()
                }

                Thread.sleep(counterDelay)
            }

            // Run final animation in the main thread
            Handler(Looper.getMainLooper()).post {
                layoutCoins?.background = ContextCompat.getDrawable(contextNotNull, R.drawable.fragment_lap_circle)

                layoutCoins?.pivotX = layoutCoins.width / 2f
                layoutCoins?.pivotY = layoutCoins.height / 2f

                val animator1 = ValueAnimator.ofFloat(1.0f, 1.2f)
                animator1.addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    layoutCoins?.scaleX = value
                    layoutCoins?.scaleY = value
                }

                val animator2 = ValueAnimator.ofFloat(1.2f, 1.0f)
                animator2.addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    layoutCoins?.scaleX = value
                    layoutCoins?.scaleY = value
                }

                val animatorSet = AnimatorSet()
                animatorSet.playSequentially(animator1, animator2)
                animatorSet.duration = FINAL_ANIMATION_DURATION
                animatorSet.addListener(object : Animator.AnimatorListener {

                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        Handler().postDelayed({
                            ready()
                        }, FINAL_PAUSE)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                })
                animatorSet.start()
            }
        }, "LapFragmentThread").start()
    }

    private fun ready() {
        (activity as? MapActivity)?.onLapFragmentReadyCallback?.invoke()
    }

}