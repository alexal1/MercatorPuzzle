package com.alex_aladdin.mercatorpuzzle.custom_views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.alex_aladdin.mercatorpuzzle.country.Country
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
                    this@FlagView.setImageResource(resId)
                }
            }
            else {
                Handler(Looper.getMainLooper()).post {
                    this@FlagView.setImageResource(0)
                }
            }
        }

    override fun propertyChange(pce: PropertyChangeEvent?) {
        pce ?: return

        if (pce.propertyName == Country.PROPERTY_CURRENT_CENTER) {
            currentCountry = pce.source as Country
        }
    }

    private fun getFlagResById(id: String): Int {
        val name = id.toLowerCase().replace('-', '_')
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (resId == 0) {
            Log.e(TAG, "No such flag: $name")
        }
        return resId
    }

}