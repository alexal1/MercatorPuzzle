package com.alex_aladdin.mercatorpuzzle.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.data.Continents

class ContinentDialogFragment : DialogFragment() {

    companion object {

        const val TAG = "MercatorCDFragment"

        fun instance(continent: Continents): ContinentDialogFragment {
            val fragment = ContinentDialogFragment()
            fragment.continentName = MercatorApp.applicationContext.getString(continent.stringId)
            return fragment
        }

    }

    private var continentName: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contextNotNull = context ?: return super.onCreateDialog(savedInstanceState)
        val title = continentName +
                contextNotNull.getString(R.string.continent_dialog_not_available)
        val messageText = contextNotNull.getString(R.string.continent_dialog_message)
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(messageText, Html.FROM_HTML_MODE_LEGACY)
        }
        else {
            @Suppress("DEPRECATION")
            Html.fromHtml(messageText)
        }
        return AlertDialog.Builder(contextNotNull)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
    }

}