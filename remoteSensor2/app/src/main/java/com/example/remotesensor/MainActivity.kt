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
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

    private fun init() {
        textView = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)

        buttonConnect = findViewById(R.id.buttonConnect)
        buttonConnect.isEnabled = false // Блокировка кпопки

        //Handler
        handler = object: Handler(Looper.myLooper()!!){
            override fun handleMessage(msg: Message) {
                if (msg.what == 1) {
                    textView.text = "Teмпература: ${msg.obj}"
                }

                if (msg.what == 2) {
                    textView.append("\n Влажность: ${msg.obj}")
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
            connectionThread = ConnectThread(myDevice, handler)
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
        // Нужно отрпавить текущее время
        var calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        var hours = calendar.get(Calendar.HOUR_OF_DAY)
        var minutes = calendar.get(Calendar.MINUTE)
        var seconds = calendar.get(Calendar.SECOND)
        var sdf = SimpleDateFormat("Ddd.MM.yyyy", Locale.getDefault())
        var a = sdf.format(calendar.time)
        connectionThread.connectedThread.sendMessage("$year\n$month\n$day\n$hours\n$minutes\n$seconds\n")
    }
}