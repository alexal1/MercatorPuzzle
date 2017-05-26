package com.alex_aladdin.mercatorpuzzle

import android.graphics.*
import android.view.SurfaceHolder

/**
 * Thread for drawing on MySurfaceView.
 */
class DrawThread(val surfaceHolder: SurfaceHolder) : Thread() {

    var runFlag: Boolean = false    // DrawThread is running at the moment
    var touchPoint: PointF? = null  // Point on the screen where user touches it

    override fun run() {
        var canvas: Canvas?

        // Start drawing
        while (runFlag) {
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas(null)
                synchronized (surfaceHolder) {
                    // Clear canvas
                    canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)

                    // Draw rectangle
                    val paint = Paint()
                    paint.color = Color.RED
                    touchPoint?.apply {
                        canvas!!.drawRect(x - 100, y - 100, x + 100, y + 100, paint)
                    }
                }
            }
            finally {
                canvas?.let {
                    surfaceHolder.unlockCanvasAndPost(it)
                }
            }
        }

        // Finally clear canvas
        clearCanvas()
    }

    /**
     * Just clear canvas.
     */
    private fun clearCanvas() {
        var canvas: Canvas? = null
        try {
            canvas = surfaceHolder.lockCanvas(null)
            synchronized (surfaceHolder) {
                canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
            }
        }
        finally {
            canvas?.let {
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }
    }

}