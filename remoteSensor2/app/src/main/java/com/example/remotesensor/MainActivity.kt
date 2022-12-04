package com.example.remotesensor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.enums.ScaleTypes

const val CONNECTING_SUCCESSFUL = 10
const val CONNECTING_TRY = 11
const val CONNECTING_FAILED = 12


class MainActivity : AppCompatActivity() {
    // Все, что касается Bluetooth
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() // Инициализация втроенного Bluetooth устройства
    private val bluetoothAddress = "98:D3:31:F9:8D:32" // Искомый, заранее известные, адрес устройства
    //val bluetoothAddress = "00:21:13:00:06:3B" // Искомый, заранее известные, адрес устройства
    private var myDevice: BluetoothDevice? = null //Переменная для хранения найденного устройства
    private lateinit var bluetoothHelper: BluetoothHelper // Переменная класса BluetoothHelper
    private var counterConnecting = 0 // Счетчик для подсчета количества попыток соединений

    // TextView для выбранного времени
    private lateinit var textView2: TextView

    // Handler для передачи сообщений между потоками
    lateinit var handler: Handler

    // Кнопки
    private lateinit var buttonGetData: Button
    private lateinit var buttonSyncTime: Button
    private lateinit var buttonConnect: Button
    private lateinit var buttonTest1: Button
    private lateinit var buttonTest2: Button
    private lateinit var buttonTest3: Button

    // График
    private lateinit var anyChartView: AnyChartView
    private lateinit var chart: com.anychart.charts.Cartesian

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        myDevice = bluetoothAdapter?.getRemoteDevice(bluetoothAddress)


        bluetoothHelper = BluetoothHelper(this, handler)

        bluetoothHelper.connect()
    }

    private fun init() {
        initChart()

        initButtons()

        initHandler()

        textView2 = findViewById(R.id.textView2)
        textView2.textSize = 20.0f
    }

    private fun initChart() {
        // Инициализация и настройка графика
        anyChartView = findViewById(R.id.any_chart_view)
        chart = AnyChart.line()

        chart.tooltip().titleFormat("function() {\n" +
                "var hours = Math.trunc(this.x);\n" +
                "var minutes = Math.trunc((this.x - hours) * 60);\n" +
                "var seconds = Math.trunc((((this.x - hours)*60) - minutes) * 60);\n" +
                "return 'Время: ' + hours + ':' + minutes + ':' + seconds;" +
                "\n" +
                "}")

        chart.xScale(ScaleTypes.LINEAR)
        chart.xScale("continuous")
        //chart.xScroller(true)
        //chart.yScroller(true)
        // Добавление линии нуля сетки графика
        chart.lineMarker(0).value(0).stroke("0.1 grey")
        anyChartView.setZoomEnabled(true)
        anyChartView.setChart(chart)

        chart.setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event?) {
                if (event != null) {
                    //var point = event.po
                    val a = event.data["x"]
                    val b = event.data["value"]
                    Toast.makeText(this@MainActivity, a, Toast.LENGTH_SHORT).show()
                    Log.d("myLog", "Индекс: $a")
                    Log.d("myLog", "Значение: $b")

                } else {
                    Log.d("myLog", "Исключение")
                }
            }
        })
    }

    private fun initButtons() {
        buttonConnect = findViewById(R.id.buttonConnect)
        buttonConnect.setOnClickListener {
            counterConnecting = 0 // Сброс счетчика попыток соединений
            bluetoothHelper.connect()
        }

        buttonSyncTime = findViewById(R.id.buttonSyncTime)
        buttonSyncTime.setOnClickListener {
            //connectionThread?.connectedThread?.sendMessage("2812")
        }

        buttonGetData = findViewById(R.id.buttonGetData)
        buttonGetData.setOnClickListener {
            bluetoothHelper.getData()
        }

        buttonTest1 = findViewById(R.id.buttonTest1)
        buttonTest1.setOnClickListener {

        }

        buttonTest2 = findViewById(R.id.buttonTest2)
        buttonTest2.setOnClickListener {

        }

        buttonTest3 = findViewById(R.id.buttonTest3)
        buttonTest3.setOnClickListener {

        }
    }

    private fun initHandler() {
        handler = object: Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                if (msg.what == 1) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == 2) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == 3) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == 4) {
                    draw(bluetoothHelper.connectThread!!, chart)
                }

                if (msg.what == 6) {
                    textView2.text = "Соединение разорвано ${msg.what}"
                    bluetoothHelper.connectThread?.connectedThread?.interrupt()
                    //connectionThread?.cancel()
                    bluetoothHelper.connect()
                }

                if (msg.what == 7) {
                    textView2.text = "Соединение разорвано ${msg.what}"
                    //connectionThread?.connectedThread?.interrupt()
                    //connectionThread?.cancel()
                    //tryConnect()
                }

                if (msg.what == 9) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == CONNECTING_TRY) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == CONNECTING_SUCCESSFUL) {
                    textView2.text = msg.obj.toString()
                }

                if (msg.what == CONNECTING_FAILED) {
                    // Счетчик попыток соединений
                    counterConnecting += 1
                    if (counterConnecting > 5) { // Если было совершено более 5 попыток, то подключение не возобновляем
                        textView2.text = "Нажмите кнопку соединение"
                    } else {
                        textView2.text = msg.obj.toString()
                        val restartConnectionThread = RestartConnectionThread(bluetoothHelper)
                        restartConnectionThread.name = "BlueRestart"
                        restartConnectionThread.start()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothHelper.stop()
        log("onDestroy")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}