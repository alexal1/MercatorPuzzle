package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.graphics.*
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.helpers.copyInto


class FlagView : ImageView {

    companion object {

        private const val TAG = "MercatorFlagView"
        const val MAX_BLUR_RADIUS = 25f

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var countryId: String? = null
        set(value) {
            field = value
            flagBitmap = getBitmapById(value)?.roundedCorners()
            blurredBitmap = flagBitmap?.let { Bitmap.createBitmap(it) }
            setImageBitmap(blurredBitmap)
        }

    var blurRadius = 0f
        set(value) {
            field = value
            if (blurredBitmap != null && flagBitmap != null) {
                blurBitmap(flagBitmap!!, blurredBitmap!!, value)
            }
            invalidate()
        }

    private var flagBitmap: Bitmap? = null
    private var blurredBitmap: Bitmap? = null
    private var renderScript: RenderScript? = null
    private val cornersRadius = resources.getDimension(R.dimen.flag_view_corners_radius)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        renderScript = RenderScript.create(context)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        RenderScript.releaseAllContexts()
        renderScript = null
    }

    private fun getBitmapById(id: String?): Bitmap? {
        id ?: return null
        val name = id.toLowerCase().replace('-', '_')
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (resId != 0) {
            BitmapFactory.decodeResource(context.resources, resId)
        }
        else {
            Log.e(TAG, "No such flag: $name")
            BitmapFactory.decodeResource(context.resources, R.drawable.unknown)
        }
    }

    private fun Bitmap.roundedCorners(): Bitmap {
        val bitmapInput = this@roundedCorners
        val bitmapWidth = bitmapInput.width + MAX_BLUR_RADIUS.toInt() * 2
        val bitmapHeight = bitmapInput.height + MAX_BLUR_RADIUS.toInt() * 2
        val bitmapOutput = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapOutput)
        val clipPath = Path()
        val padding = MAX_BLUR_RADIUS
        val rect = RectF(
                padding,
                padding,
                bitmapInput.width.toFloat() + padding,
                bitmapInput.height.toFloat() + padding
        )
        clipPath.addRoundRect(rect, cornersRadius, cornersRadius, Path.Direction.CW)
        canvas.clipPath(clipPath)
        canvas.drawBitmap(bitmapInput, padding, padding, Paint())
        bitmapInput.recycle()
        return bitmapOutput
    }

    private fun blurBitmap(bitmapInput: Bitmap, bitmapOutput: Bitmap, blurRadius: Float) {
        if (blurRadius > 0f) {
            val rs = renderScript ?: return
            val input = Allocation.createFromBitmap(rs, bitmapInput)
            val output = Allocation.createTyped(rs, input.type)
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            script.setRadius(blurRadius)
            script.setInput(input)
            script.forEach(output)
            output.copyTo(bitmapOutput)
        }
        else {
            renderScript?.finish()
            bitmapInput.copyInto(bitmapOutput)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Preserve flag's ratio when measuring this View
        flagBitmap
                ?.let { bitmap ->
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val h = MeasureSpec.getSize(heightMeasureSpec)
                    val w = h * ratio
                    val widthMeasureSpec2 = MeasureSpec.makeMeasureSpec(w.toInt(), MeasureSpec.AT_MOST)
                    super.onMeasure(widthMeasureSpec2, heightMeasureSpec)
                }
                ?: let {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                }
    }

}