package com.alex_aladdin.mercatorpuzzle.fragments

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.view.*
import com.alex_aladdin.mercatorpuzzle.MapActivity
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import kotlinx.android.synthetic.main.fragment_finish.*
import kotlin.math.max

class FinishFragment : Fragment() {

    companion object {

        const val TAG = "MercatorFinishFragment"
        private const val INITIAL_PAUSE = 1000L
        private const val ANIMATION_DURATION = 1000L
        var topMargin = 0

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_finish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAppearance()
        setData()
        setListeners()
        startAnimation()
    }

    fun onBackPressed() {
        ready()
    }

    private fun setAppearance() {
        var titleMargin = context?.resources?.getDimension(R.dimen.fragment_finish_top_margin) ?: return
        titleMargin += topMargin

        val constraintSet = ConstraintSet()
        constraintSet.clone(layoutFragmentFinish)
        constraintSet.setMargin(R.id.textTitle, ConstraintSet.TOP, titleMargin.toInt())
        constraintSet.applyTo(layoutFragmentFinish)
    }

    private fun setData() {
        val contextNotNull = context ?: return
        val gameData = MercatorApp.gameData ?: return

        val title = contextNotNull.getString(gameData.continent.stringId) +
                contextNotNull.getString(R.string.finish_fragment_title_done)
        val timestampFinish = gameData.timestampFinish ?: gameData.timestampStart
        var minutes = (timestampFinish - gameData.timestampStart) / 60_000L
        val seconds = (timestampFinish - gameData.timestampStart) % 60_000L / 1000L
        if (seconds >= 30) {
            minutes += 1
        }

        textTitle?.text = title
        textCountriesCounter?.text = gameData.continent.count.toString()
        textMinutesCounter?.text = minutes.toString()
        textCoinsCounter?.text = gameData.coins.toString()
    }

    private fun setListeners() {
        buttonMenu.setOnClickListener((activity as? MapActivity)?.onMenuClickCallback)

        val alphaRespond = { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.alpha = 0.5f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 1.0f
            }
            false
        }
        buttonMenu.setOnTouchListener(alphaRespond)
        buttonResults.setOnTouchListener(alphaRespond)
    }

    private fun startAnimation() {
        // Start animation only after view tree is built
        fun animation() {
            textCountriesCounter.pivotX = textCountriesCounter.width / 2f
            textCountriesCounter.pivotY = textCountriesCounter.height / 2f
            textMinutesCounter.pivotX = textMinutesCounter.width / 2f
            textMinutesCounter.pivotY = textMinutesCounter.height / 2f
            textCoinsCounter.pivotX = textCoinsCounter.width / 2f
            textCoinsCounter.pivotY = textCoinsCounter.height / 2f

            val animator1 = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f)
            animator1.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                val alphaValue = value / 0.2f - 1 / 0.2f
                textCountriesCounter?.apply {
                    scaleX = value
                    scaleY = value
                    alpha = max(alpha, alphaValue)
                }
                textCountriesCaption?.apply {
                    alpha = max(alpha, alphaValue)
                }
            }
            animator1.startDelay = 0

            val animator2 = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f)
            animator2.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                val alphaValue = value / 0.2f - 1 / 0.2f
                textMinutesCounter?.apply {
                    scaleX = value
                    scaleY = value
                    alpha = max(alpha, alphaValue)
                }
                textMinutesCaption?.apply {
                    alpha = max(alpha, alphaValue)
                }
            }
            animator2.startDelay = ANIMATION_DURATION / 2

            val animator3 = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f)
            animator3.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                val alphaValue = value / 0.2f - 1 / 0.2f
                textCoinsCounter?.apply {
                    scaleX = value
                    scaleY = value
                    alpha = max(alpha, alphaValue)
                }
                textCoinsCaption?.apply {
                    alpha = max(alpha, alphaValue)
                }
            }
            animator3.startDelay = ANIMATION_DURATION

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animator1, animator2, animator3)
            animatorSet.duration = ANIMATION_DURATION
            animatorSet.startDelay = INITIAL_PAUSE
            animatorSet.start()
        }

        layoutFragmentFinish
                .viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        layoutFragmentFinish.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        animation()
                    }
                })
    }

    private fun ready() {
        (activity as? MapActivity)?.onFinishFragmentReadyCallback?.invoke()
    }

}