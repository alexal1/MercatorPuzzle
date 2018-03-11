package com.alex_aladdin.mercatorpuzzle.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import com.alex_aladdin.mercatorpuzzle.activities.FeedbackActivity

class FeedbackDialogFragment : DialogFragment() {

    companion object {

        const val TAG = "MercatorFDFragment"

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contextNotNull = context ?: return super.onCreateDialog(savedInstanceState)
        val title = contextNotNull.getString(R.string.feedback_dialog_title)
        val message = contextNotNull.getString(R.string.feedback_dialog_message)
        return AlertDialog.Builder(contextNotNull)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    val i = Intent(contextNotNull, FeedbackActivity::class.java)
                    contextNotNull.startActivity(i)
                }
                .setNegativeButton(contextNotNull.getString(R.string.feedback_dialog_no)) { dialog, _ ->
                    dialog.cancel()
                }
                .setNeutralButton(contextNotNull.getString(R.string.feedback_dialog_do_not_show)) { _, _ ->
                    MercatorApp.flagDoNotShowFeedbackDialog = true
                }
                .create()
    }

}