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

class MainActivity : AppCompatActivity() {
    // TextView для выбранного времени
    private lateinit var textView2: TextView
    // Handler для передачи сообщений между потоками
    lateinit var handler: Handler
    // Инициализация втроенного Bluetooth устройства
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // Искомый, заранее известные, адрес устройства
    val bluetoothAddress = "98:D3:31:F9:8D:32"
    //val bluetoothAddress = "00:21:13:00:06:3B"
    //Переменная для хранения найденного устройства
    private var myDevice: BluetoothDevice? = null

    var founded = false // Признак того что устройство найдено. После установки true, broadcastreceiver не будет ничего делать если найдены другие устройства

    var connectionThread: ConnectThread? = null

    private lateinit var buttonGetData: Button
    private lateinit var buttonSyncTime: Button
    private lateinit var buttonDiscovery: Button
    private lateinit var buttonTest2: Button



    private lateinit var anyChartView: AnyChartView
    private lateinit var chart: com.anychart.charts.Cartesian


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        myDevice = bluetoothAdapter?.getRemoteDevice(bluetoothAddress)

        tryConnect()
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
        buttonSyncTime = findViewById(R.id.buttonSyncTime)
        buttonSyncTime.setOnClickListener {
            connectionThread?.connectedThread?.sendMessage("2812")
        }

        buttonDiscovery = findViewById(R.id.buttonDiscovery)
        buttonDiscovery.setOnClickListener {
            tryConnect()
        }

        buttonGetData = findViewById(R.id.buttonGetData)
        buttonGetData.setOnClickListener {
            if (connectionThread != null) {
                if(connectionThread?.connectedThread != null) {
                    if (connectionThread?.connectedThread?.isAlive == true) {
                        connectionThread!!.connectedThread?.sendMessage("3421")
                    } else {
                        tryConnect()
                    }
                } else {
                    tryConnect()
                }
            } else {
                tryConnect()
            }
        }

        buttonTest2 = findViewById(R.id.buttonTest2)
        buttonTest2.setOnClickListener {
            //connectionThread?.connectedThread?.start()
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
                    draw(connectionThread!!, chart)
                }

                if (msg.what == 6) {
                    textView2.text = "Соединение разорвано ${msg.what}"
                    connectionThread?.connectedThread?.interrupt()
                    connectionThread?.cancel()
                    tryConnect()
                }

                if (msg.what == 7) {
                    textView2.text = "Соединение разорвано ${msg.what}"
                    connectionThread?.connectedThread?.interrupt()
                    connectionThread?.cancel()
                    //tryConnect()
                }

                if (msg.what == 8) {
                    textView2.text = msg.obj.toString()
                    //tryConnect()
                }

                if (msg.what == 9) {
                    textView2.text = msg.obj.toString()
                }
            }
        }
    }

    /**
     * Попробовть просто в онрезме проверять живы ли ветви и есть нет то возобновлять
     */

    //Создать поток соединения и запускить
    private fun tryConnect() {
        if (myDevice != null) {
            if (connectionThread != null) {
                if (connectionThread!!.isAlive) {
                    connectionThread = null
                }
            }
            connectionThread = ConnectThread(myDevice!!, handler, this)
            connectionThread!!.start()
        } else {
            Toast.makeText(this, "Device = null", Toast.LENGTH_SHORT).show()
        }
    }

    fun interr(view: View) {
        connectionThread?.connectedThread?.interrupt()
        //finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionThread?.cancel()
        connectionThread?.connectedThread?.interrupt()
        connectionThread = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
    }
}