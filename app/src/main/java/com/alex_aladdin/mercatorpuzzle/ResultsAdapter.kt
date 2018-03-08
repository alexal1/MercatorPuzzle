package com.alex_aladdin.mercatorpuzzle

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alex_aladdin.mercatorpuzzle.data.GameData
import java.text.DateFormat
import java.util.*

class ResultsAdapter(private val items: List<GameData>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.results_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gameData = items[position]
        holder.apply {
            textContinent.text = gameData.continent.name
            textCoins.text = gameData.coins.toString()

            val date = DateFormat
                    .getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                    .format(Date(gameData.timestampStart))
            val time = DateFormat
                    .getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                    .format(Date(gameData.timestampStart))
            val totalDate = "$date $time"
            textDate.text = totalDate

            val progress = "${gameData.progress}/${gameData.continent.count}"
            textProgress.text = progress

            val timestampFinish = gameData.timestampFinish ?: return
            val minutes = (timestampFinish - gameData.timestampStart) / 60_000L
            val seconds = (timestampFinish - gameData.timestampStart) % 60_000L / 1000L
            val totalTime = minutes.toString() + " " +
                    MercatorApp.applicationContext.getString(R.string.results_activity_minutes) +
                    " " + seconds.toString() + " " +
                    MercatorApp.applicationContext.getString(R.string.results_activity_seconds)
            textTime.text = totalTime
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textContinent: TextView = itemView.findViewById(R.id.textContinent)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textProgress: TextView = itemView.findViewById(R.id.textProgress)
        val textCoins: TextView = itemView.findViewById(R.id.textCoins)
        val textTime: TextView = itemView.findViewById(R.id.textTime)

    }

}