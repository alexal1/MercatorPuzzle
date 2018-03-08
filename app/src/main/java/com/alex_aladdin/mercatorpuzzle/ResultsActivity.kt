package com.alex_aladdin.mercatorpuzzle

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_results.*

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        setToolbar()
        setRecycler()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setRecycler() {
        MercatorApp.gameController.loadAllGames { items ->
            recyclerResults.adapter = ResultsAdapter(items)
            recyclerResults.layoutManager = LinearLayoutManager(this@ResultsActivity, LinearLayoutManager.VERTICAL, false)
            recyclerResults.setHasFixedSize(true)
        }
    }

}