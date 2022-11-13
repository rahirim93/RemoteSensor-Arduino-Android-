package com.example.remotesensor

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.ScaleTypes
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    // Переменная для хранения соединенного потока
    private lateinit var connectedThread: ConnectedThread
    // Переменная для хранения времени будильника
    private lateinit var alarmTimeCalendar: Calendar
    // TextView для отображения принятого сообщения
    private lateinit var textView: TextView
    // TextView для выбранного времени
    private lateinit var textView2: TextView
    // Handler для передачи сообщений между потоками
    private lateinit var handler: Handler
    // Кнопка соединения
    private lateinit var buttonConnect: Button
    // Инициализация втроенного Bluetooth устройства
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // Искомый, заранее известные, адрес устройства
    val bluetoothAddress = "98:D3:31:F9:8D:32"
    //val bluetoothAddress = "00:21:13:00:06:3B"
    //Переменная для хранения найденного устройства
    private lateinit var myDevice: BluetoothDevice
    var founded = false

    private lateinit var connectionThread: ConnectThread

    private lateinit var buttonPrintLog: Button

    private lateinit var anyChartView: AnyChartView
    private lateinit var chart: com.anychart.charts.Cartesian



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        //Запрос разрешений
        requestPermissions()

        //Регистрация ресивера
        registerMyReceiver()

        bluetoothAdapter?.startDiscovery()
    }

    private fun initChart() {
        // Инициализация и настройка графика
        anyChartView = findViewById(R.id.any_chart_view)
        chart = AnyChart.line()
        chart.xScale(ScaleTypes.LINEAR)
        // Добавление линии нуля сетки графика
        val zeroLine = chart.lineMarker(0).value(0).stroke("0.1 grey")
        anyChartView.setZoomEnabled(true)
        anyChartView.setChart(chart)
    }

    private fun init() {
        initChart()

        textView = findViewById(R.id.textView)
        textView.textSize = 1.0f
        textView2 = findViewById(R.id.textView2)
        textView2.textSize = 20.0f

        buttonConnect = findViewById(R.id.buttonConnect)
        buttonConnect.isEnabled = false // Блокировка кпопки

        buttonPrintLog = findViewById(R.id.buttonPrintLog)
        buttonPrintLog.setOnClickListener {
            val dataHumidity = arrayListOf<DataEntry>()
            val dataTemperature = arrayListOf<DataEntry>()
            val list = connectionThread.connectedThread.list
            if (list.size > 0) {
                list.take(list.size - 2).forEach {
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
                    //Log.d("myLog", "$sumHours")

                    val humidity = stringHumidity.toFloat()
                    val temperature = stringTemperature.toFloat()

                    dataHumidity.add(ValueDataEntry(sumHours, humidity))
                    dataTemperature.add(ValueDataEntry(sumHours, temperature))



                    //textView2.append("$stringTime $stringHumidity $stringTemperature\n")
                }
                chart.run {
                    line(dataHumidity).stroke("0.2 black").name("Влажность")
                    line(dataTemperature).stroke("0.2 red").name("Температура")
                }
                //list.clear()
            }
        }

        //Handler
        handler = object: Handler(Looper.myLooper()!!){
            override fun handleMessage(msg: Message) {
                if (msg.what == 1) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == 2) {
                    textView2.text = msg.obj.toString()
                }
                //textView.text = "Температура: ${msg.obj} \n Влажность: "
            }
        }
    }

    private fun requestPermissions() {
        // Запрос разрешения на использование геолокации
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN),
            1 )
    }

    private fun registerMyReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    //Инициализация широковещательного приемника
    private val receiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address
                    println("Name: $deviceName. Address: $deviceHardwareAddress")
                    if (device?.address.equals(bluetoothAddress) && !founded) {
                        if (device != null) {
                            myDevice = device
                            founded = true
                            buttonConnect.isEnabled = true //Если найдено искомое устройство - разблокировать устройство
                            Toast.makeText(context, "Устройство найдено", Toast.LENGTH_SHORT).show()
                            tryConnect()
                        }
                    }
                }
            }
        }

    }

    //Кнопка начала поиска устройств
    fun click(view: android.view.View) {
        bluetoothAdapter?.startDiscovery()
    }

    //Кнопка запуска соединения
    fun connect(view: android.view.View) {
        //Создать поток соединения и запускить
        tryConnect()
    }

    //Создать поток соединения и запускить
    private fun tryConnect() {
        if (myDevice != null) {
            connectionThread = ConnectThread(myDevice, handler, this)
            connectionThread.start()
            Toast.makeText(this, "Попытка соединения", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Device = null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //Отказ от регистрации приемника при закрытии приложения
        unregisterReceiver(receiver)
    }

    fun sendMessage(view: View) {
        //connectionThread.connectedThread.sendMessage("1563")
        //setTime()
    }
    fun setTime() {
        // Нужно отрпавить текущее время
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val sdf = SimpleDateFormat("Ddd.MM.yyyy", Locale.getDefault())
        //var a = sdf.format(calendar.time)
        connectionThread
            .connectedThread
            .sendMessage("${year}Y${month}M${day}D${hours}H${minutes}m${seconds}S")
    }

    fun buttonOn(view: View) {
        connectionThread.connectedThread.sendMessage("2812")
    }
    fun buttonSendTime(view: View) {
        setTime()
    }

    fun buttonGetData(view: View) {
        connectionThread.connectedThread.sendMessage("3421")
    }

    fun buttonNameFile(view: View) {
        //var a = sdf.format(calendar.time)

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        Log.d("myLog", sdf.format(calendar.time))
        connectionThread.connectedThread.sendMessage(sdf.format(calendar.time))


    }
}