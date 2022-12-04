package com.example.remotesensor

import android.util.Log
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import java.text.SimpleDateFormat
import java.util.*

fun draw(connectionThread: ConnectThread, chart: com.anychart.charts.Cartesian) {
    chart.removeAllSeries()
    val dataHumidity = arrayListOf<DataEntry>()
    val dataTemperature = arrayListOf<DataEntry>()
    val list = connectionThread.connectedThread?.list
    if (list!!.size > 0) {
        list.take(list.size - 0).forEach {
            val stringBuilder2 = StringBuilder()
            stringBuilder2.append(it)
            val indexTime = it.indexOf("T")
            val indexHumidity = it.indexOf("H")
            val indexTemperature = it.indexOf("t")
            val stringTime = stringBuilder2.substring(0, indexTime)
            val stringHumidity = stringBuilder2.substring(indexTime + 1, indexHumidity)
            val stringTemperature = stringBuilder2.substring(indexHumidity + 1, indexTemperature)
            stringBuilder2.delete(0, stringBuilder2.length)
            stringBuilder2.append(stringTime)
            val hours = stringBuilder2.substring(0, 2).toInt()
            val minutes = stringBuilder2.substring(3, 5).toInt()
            val seconds = stringBuilder2.substring(6, 8).toInt()
            val sumHours = hours + (minutes/60.0) + (seconds / 60.0 / 60.0)
            val humidity = stringHumidity.toFloat()
            val temperature = stringTemperature.toFloat()
            dataHumidity.add(ValueDataEntry(sumHours, humidity))
            dataTemperature.add(ValueDataEntry(sumHours, temperature))
        }
        chart.run {
            line(dataHumidity).stroke("0.2 black").name("Влажность")
            line(dataTemperature).stroke("0.2 red").name("Температура")
        }
    }
}

fun sendDate(connectionThread: ConnectThread) {
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    Log.d("myLog", sdf.format(calendar.time))
    connectionThread.connectedThread?.sendMessage(sdf.format(calendar.time))
}

fun log (string: String) {
    Log.d("myLog", string)
}