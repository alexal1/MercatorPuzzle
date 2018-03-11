package com.alex_aladdin.mercatorpuzzle.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebViewClient
import com.alex_aladdin.mercatorpuzzle.MercatorApp
import com.alex_aladdin.mercatorpuzzle.R
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        getSharedPreferences(MercatorApp.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(MercatorApp.SHARED_PREFERENCES_FEEDBACK_GIVEN, true)
                .apply()

        setToolbar()
        setWebView()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(getString(R.string.feedback_activity_url))
    }

}