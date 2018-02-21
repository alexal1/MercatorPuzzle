package com.alex_aladdin.mercatorpuzzle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

/**
 * Create a copy of this Bitmap with the given alpha.
 */
fun Bitmap.alpha(alpha: Float): Bitmap {
    val originalBitmap = this@alpha
    val newBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(newBitmap)
    val alphaPaint = Paint()
    alphaPaint.alpha = (255 * alpha).toInt()
    canvas.drawBitmap(originalBitmap, 0f, 0f, alphaPaint)
    return newBitmap
}

/**
 * Create a Bitmap from drawable by given resource ID.
 */
fun createBitmapFrom(resId: Int): Bitmap
        = BitmapFactory.decodeResource(MercatorApp.applicationContext.resources, resId)