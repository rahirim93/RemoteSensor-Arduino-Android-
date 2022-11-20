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
    lateinit var connectedThread: ConnectedThread
     val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(myUUID)
    }

    override fun run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter?.cancelDiscovery()

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            myHandler.sendMessage(myHandler.obtainMessage(8, "Попытка соединения"))
            mmSocket?.connect()
            myHandler.sendMessage(myHandler.obtainMessage(8, "Соединение успешно"))
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = ConnectedThread(mmSocket, myHandler)
            connectedThread.start()

        } catch (e: Exception) {
            myHandler.sendMessage(myHandler.obtainMessage(8, "Попытка соединения не удалась"))
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