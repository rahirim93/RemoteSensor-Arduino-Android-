package com.example.remotesensor

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class ConnectedThread (mmSocket: BluetoothSocket?, myHandler: Handler) : Thread() {
        private val mmInStream: InputStream? = mmSocket?.inputStream
        private val mmOutStream: OutputStream? = mmSocket?.outputStream
        private val mmBuffer: ByteArray = ByteArray(3000) // mmBuffer store for the stream
        private var handler = myHandler

        override fun run() {
            var numBytes: Int // bytes returned from read()
            var stringBuilder = StringBuilder()
            lateinit var stringTemperature: String
            lateinit var stringHumidity: String
            lateinit var stringToPrint: String
            var sumBytes = 0

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                try {
                    var bytes = mmInStream!!.available()
                    if (bytes == 0) {
                        sleep(10)
                        continue
                    }
                    val buffer = ByteArray(bytes) // buffer store for the stream

                    // Read from the InputStream
                    bytes = mmInStream.read(buffer) // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                    sumBytes += bytes
                    //Log.d("myLog", "Количество байт: $summBytes")
                    //Log.d("myLog", "Buffer =" + buffer.contentToString())
                    var str = String(buffer)
                    stringBuilder.append(str)
                    var indexOfN = stringBuilder.indexOf("\n")
                    //Log.d("myLog", "Строка: $str")
                    //Log.d("myLog", "Количество символов: ${str.length}")
                    //Log.d("myLog", "Наличие перевода: $indexOfN")
                    //Log.d("myLog", "................")
                    /** Нужно отлавливать конец строки. Если его нет, то сохранять промежуточную строку
                     * но не выводить. Если в следующей строки он будет. То прибавлять к первой то что было
                     * до конца и выводить. остаток надо сохранять для формирования следующей строки о при нахождении
                     * нового конца
                     *
                     */
                    if (indexOfN > 0) {
                        var ind = stringBuilder.indexOf("\n")
                        var str1 = stringBuilder.substring(0, ind)
                        var str2 = stringBuilder.substring(ind + 1, stringBuilder.length)
                        Log.d("myLog", "Строка1: $str1")
                        stringBuilder.delete(0, stringBuilder.length)
                        stringBuilder.append(str2)
                    }

                    ///////////////////////
//                    numBytes = mmInStream?.read(mmBuffer)!!
//                    //Log.d("MyLog", "Количество байтов: $numBytes")
//                    summBytes += numBytes
//                    Log.d("MyLog", "Количество байтов: $summBytes")
//
//                    var stringIncome = String(mmBuffer,0, numBytes)
//                    stringBuilder.append(stringIncome)
//
//
//                    //var endLineIndex1 = stringBuilder.indexOf("A")
//                    var endLineIndex = stringBuilder.indexOf("\r\n")
//                   // var endLineIndex = stringBuilder.indexOf("A")
//                    //var endLineIndex = stringBuilder.indexOf("\n")
//                   //var endLineIndex = stringBuilder.indexOf("\r")
//                    if (endLineIndex > 0) {
//                        Log.d("MyLog", endLineIndex.toString())
//
//                        //stringTemperature = stringBuilder.substring(0, endLineIndex1)
//                        //stringHumidity = stringBuilder.substring(endLineIndex1 + 1, endLineIndex)
//                        stringToPrint = stringBuilder.substring(0, endLineIndex)
//                        //Log.d("myLog", "Temp $stringTemperature")
//                        //Log.d("myLog", "Hum $stringHumidity")
//                        Log.d("MyLog", stringToPrint)
//                        stringBuilder.delete(0, stringBuilder.length)
//
//                        //var msg = handler.obtainMessage(1, stringTemperature)
//                        //var msg2 = handler.obtainMessage(2, stringHumidity)
//
//                        //handler.sendMessage(msg)
//                        //handler.sendMessage(msg2)
//                    }
                } catch (e: IOException) {
                    Log.d("MyLog", "Input stream was disconnected", e)
                    break
                }
            }
        }

//        fun write(bytes: ByteArray) {
//            try {
//                mmOutStream?.write(bytes)
//            } catch (e: Exception) {
//
//            }
//        }
    fun sendMessage(message: String) {
        try {
            val messageBuffer = message.toByteArray()
            mmOutStream?.write(messageBuffer)
        } catch (e: Exception) {

        }
    }
    }
