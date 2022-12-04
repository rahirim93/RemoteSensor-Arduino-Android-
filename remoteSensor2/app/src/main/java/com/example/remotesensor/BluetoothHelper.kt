package com.example.remotesensor

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.widget.Toast

class BluetoothHelper(var context: Context,
                      var handler: Handler) {

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var connectThread: ConnectThread? = null
    var connectedThread: ConnectedThread? = null
    private val bluetoothAddress = "98:D3:31:F9:8D:32"
    private val bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothAddress)
    //val bluetoothAddress = "00:21:13:00:06:3B"
    private var flagDestroy = false



    fun start() {

    }

    fun getData() {
        if (connectThread != null) {
            if(connectThread?.connectedThread != null) {
                if (connectThread?.connectedThread?.isAlive == true) {
                    connectThread!!.connectedThread?.sendMessage("3421")
                } else {
                    connect()
                }
            } else {
                connect()
            }
        } else {
            connect()
        }
    }

    fun connect() {
        if (!flagDestroy) {
            if (bluetoothDevice != null) {
                if (connectThread != null) {
                    if (connectThread!!.isAlive) {
                        connectThread = null
                    }
                }
                connectThread = ConnectThread(bluetoothDevice, handler, context)
                connectThread!!.name = "BlueConnectingThread"
                connectThread!!.start()
            } else {
                Toast.makeText(context, "Device = null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stop() {
        flagDestroy = true
        connectThread?.cancel()
        connectThread?.connectedThread?.interrupt()
        connectThread = null
    }

    fun status () {

    }
}