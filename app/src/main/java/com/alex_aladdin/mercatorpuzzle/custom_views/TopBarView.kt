package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.Fade.IN
import android.support.transition.Fade.OUT
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.country.Country
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.top_bar_view.view.*

class TopBarView : RelativeLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private enum class BarType { FLAG, FLAG_AND_NAME }

    private val compositeDisposable = CompositeDisposable()
    private var currentType = BarType.FLAG

    init {
        inflate(context, R.layout.top_bar_view, this@TopBarView)
        setBarType(BarType.FLAG)
        setOnClickListener {
            if (currentType == BarType.FLAG) {
                setBarType(BarType.FLAG_AND_NAME)
            }
            else {
                setBarType(BarType.FLAG)
            }
        }
    }

    var currentCountry: Country? = null
        set(value) {
            field = value

            if (value == null) {
                setBarType(BarType.FLAG)
            }

            flagView.currentCountry = value
            textName.text = value?.name ?: ""
        }

    fun subscribeOn(observable: Observable<Country>) {
        val disposable: Disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { country ->
                    currentCountry = country
                }

        compositeDisposable.add(disposable)
    }

    private fun setBarType(type: BarType) {
        currentType = type

        // Prepare transitions

        val transitionTopBar = ChangeBounds()
        transitionTopBar.addTarget(this@TopBarView)

        val transitionFlag = ChangeBounds()
        transitionFlag.addTarget(flagView)

        val transitionName = if (type == BarType.FLAG) Fade(OUT) else Fade(IN)
        transitionName.addTarget(textName)

        val transitionBackground = if (type == BarType.FLAG) Fade(OUT) else Fade(IN)
        transitionBackground.addTarget(imageBackground)

        val transitionSet = TransitionSet()
                .addTransition(transitionTopBar)
                .addTransition(transitionFlag)
                .addTransition(transitionName)
                .addTransition(transitionBackground)
                .setOrdering(TransitionSet.ORDERING_TOGETHER)

        TransitionManager.beginDelayedTransition(this@TopBarView, transitionSet)

        // Perform layout changes

        when (type) {
            BarType.FLAG -> {
                this@TopBarView.layoutParams = RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                )
                flagView.layoutParams = RelativeLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                )
                (flagView.layoutParams as RelativeLayout.LayoutParams).addRule(CENTER_HORIZONTAL)
                textName.visibility = View.GONE
                imageBackground.visibility = View.GONE
            }

            BarType.FLAG_AND_NAME -> {
                this@TopBarView.layoutParams = RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        resources.getDimension(R.dimen.top_bar_view_height).toInt()
                )
                flagView.layoutParams = RelativeLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT
                )
                textName.visibility = View.VISIBLE
                imageBackground.visibility = View.VISIBLE
            }
        }
        val margin = resources.getDimension(R.dimen.top_bar_view_inner_margin).toInt()
        (flagView.layoutParams as MarginLayoutParams).setMargins(margin, margin, 0, margin)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        compositeDisposable.clear()
    }

}