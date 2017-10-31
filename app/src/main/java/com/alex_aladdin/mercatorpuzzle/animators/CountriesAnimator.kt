package com.alex_aladdin.mercatorpuzzle.animators

import com.alex_aladdin.mercatorpuzzle.draw_threads.DrawThread

abstract class CountriesAnimator(protected val drawThread: DrawThread) {

    var isInProgress = false
        protected set

    abstract fun animate(onFinish: (() -> Unit)? = null)

    abstract fun cancel()

}