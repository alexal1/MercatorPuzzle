package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.AttributeSet
import android.util.Log
import android.view.ViewOutlineProvider
import android.widget.ImageView
import com.alex_aladdin.mercatorpuzzle.country.Country
import com.alex_aladdin.mercatorpuzzle.helpers.dp
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class FlagView : ImageView, PropertyChangeListener {

    companion object {

        val TAG = "MercatorFlagView"

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var currentCountry: Country? = null
        set(value) {
            if (value == field) return

            field = value

            if (value != null) {
                val resId = getFlagResById(value.id)
                Handler(Looper.getMainLooper()).post {
                    if (resId != null) {
                        val drawable = getRoundedDrawable(resId)
                        this@FlagView.background = drawable
                    }
                    else {
                        this@FlagView.background = null
                    }
                }
            }
            else {
                Handler(Looper.getMainLooper()).post {
                    this@FlagView.background = null
                }
            }
        }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this@FlagView.outlineProvider = ViewOutlineProvider.BACKGROUND
            this@FlagView.translationZ = 8f.dp
        }
    }

    override fun propertyChange(pce: PropertyChangeEvent?) {
        pce ?: return

        if (pce.propertyName == Country.PROPERTY_CURRENT_CENTER) {
            currentCountry = pce.source as Country
        }
    }

    private fun getFlagResById(id: String): Int? {
        val name = id.toLowerCase().replace('-', '_')
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (resId != 0) {
            resId
        }
        else {
            Log.e(TAG, "No such flag: $name")
            null
        }
    }

    private fun getRoundedDrawable(resId: Int): Drawable {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val drawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        drawable.cornerRadius = 8f.dp
        return drawable
    }

}