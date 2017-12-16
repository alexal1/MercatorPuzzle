package com.alex_aladdin.mercatorpuzzle.helpers

import android.util.TypedValue
import com.alex_aladdin.mercatorpuzzle.MercatorApp

val Float.dp: Float get() {
    val resources = MercatorApp.applicationContext.resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this@dp, resources.displayMetrics)
}

val Float.mm: Float get() {
    val resources = MercatorApp.applicationContext.resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, this@mm, resources.displayMetrics)
}