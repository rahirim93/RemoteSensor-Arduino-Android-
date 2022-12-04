package com.example.remotesensor

import java.util.*

class RestartConnectionThread(private val bluetoothHelper: BluetoothHelper): Thread() {

    private val restartTimeOut = 2000
    private val startTime = Calendar.getInstance().timeInMillis

    override fun run() {
        while (!isInterrupted) {
            if (Calendar.getInstance().timeInMillis - startTime > restartTimeOut) {
                bluetoothHelper.connect()
                interrupt()
            }
        }
    }
}