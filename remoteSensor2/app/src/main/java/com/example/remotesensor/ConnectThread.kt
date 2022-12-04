package com.example.remotesensor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import java.io.IOException
import java.util.*

class ConnectThread (device: BluetoothDevice, handler: Handler, var context: Context) : Thread() {

    private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var myHandler = handler
    var connectedThread: ConnectedThread? = null
     private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(myUUID)
    }

    override fun run() {
        // Cancel discovery because it otherwise slows down the connection.
        if (bluetoothAdapter?.isDiscovering == true) bluetoothAdapter?.cancelDiscovery()


        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            myHandler.sendMessageDelayed(myHandler.obtainMessage(CONNECTING_TRY, "Попытка соединения"), 500)
            val startConnect = Calendar.getInstance().timeInMillis
            mmSocket?.connect()
            val endConnect = Calendar.getInstance().timeInMillis
            val timeOfConnecting = (endConnect - startConnect) / 1000
            log("Время соединения: $timeOfConnecting сек")
            myHandler.sendMessageDelayed(myHandler.obtainMessage(CONNECTING_SUCCESSFUL, "Соединение успешно"), 500)
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = ConnectedThread(mmSocket, myHandler)
            connectedThread!!.name = "BlueConnectedThread"
            connectedThread!!.start()


        } catch (e: Exception) {
            cancel()
            myHandler.sendMessageDelayed(myHandler.obtainMessage(CONNECTING_FAILED, "Попытка соединения не удалась"), 500)
        }
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
        }
    }
}