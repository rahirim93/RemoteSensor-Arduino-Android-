package com.example.remotesensor

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList


class ConnectedThread (mmSocket: BluetoothSocket?, myHandler: Handler) : Thread() {
        private val mmInStream: InputStream? = mmSocket?.inputStream
        private val mmOutStream: OutputStream? = mmSocket?.outputStream
        private var counter = true
        var list = ArrayList<String>()
    private var handler = myHandler


    override fun run() {
            val stringBuilder = StringBuilder()
            var mode = 1231
            // 3212 - прием тми
            // 1231 - считывание команд
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                // Режим считывания команд
                if (mode == 1231) {
                    if (counter) {
                        handler.sendMessage(handler.obtainMessage(1, "Ожидание команды"))
                        Log.d("myLog", "Режим считывания команд: $mode")
                        counter = false
//                        if (list.size > 0) {
//                            list.take(list.size - 2).forEach {
//                                val a = it.indexOf("T")
//                                val stringBuilder2 = StringBuilder()
//                                stringBuilder2.append(it)
//                                //Log.d("myLog", stringBuilder2.substring(0, a))
//                                val time = stringBuilder2.substring(0, a)
//                                Log.d("myLog", "Время: $time")
//                            }
//                            //list.clear()
//                        }
                    }
                    try {
                        var bytes = mmInStream!!.available()
                        if (bytes == 0) {
                            sleep(10)
                            continue
                        }
                        val buffer = ByteArray(bytes) // buffer store for the stream

                        // Read from the InputStream
                        bytes = mmInStream.read(buffer) // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                        val str = String(buffer)
                        //Log.d("myLog", "Строка1: $str")
                        stringBuilder.append(str)
                        val indexOfN = stringBuilder.indexOf("\n")
                        /** 1 Отправить команду на арудино
                         * 2 Принять команду от ардуино и включить соответствующий режим приема
                         * 3 Отправить информацию с ардуино. В конце поставить знак окончания передачи
                         * и при его приеме переключить режим обратно
                         */
                        if (indexOfN > 0) {
                            val ind = stringBuilder.indexOf("\n")
                            val str1 = stringBuilder.substring(0, ind)
                            if (str1.equals("3212")) {
                                mode = str1.toInt()
                                counter = true
                            } else if(str1.equals("4532")) {
                                mode = str1.toInt()
                                counter = true
                            }
                            val str2 = stringBuilder.substring(ind + 1, stringBuilder.length)
                            Log.d("myLog", "Строка1: $str1")
                            stringBuilder.delete(0, stringBuilder.length)
                            stringBuilder.append(str2)
                        }
                    } catch (e: IOException) {
                        Log.d("MyLog", "Input stream was disconnected", e)
                        break
                    }
                }
                // Режим вывода полученного в консоль
                if (mode == 3212) {
                    if (counter) {
                        Log.d("myLog", "Режим: $mode")
                        counter = false
                    }
                    while (mode == 3212) {
                        try {
                            var bytes = mmInStream!!.available()
                            if (bytes == 0) {
                                sleep(10)
                                continue
                            }
                            val buffer = ByteArray(bytes) // buffer store for the stream

                            // Read from the InputStream
                            bytes = mmInStream.read(buffer) // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                            val str = String(buffer)
                            //Log.d("myLog", "Строка1: $str")
                            stringBuilder.append(str)
                            val indexOfN = stringBuilder.indexOf("\n")
                            /** 1 Отправить команду на арудино
                             * 2 Принять команду от ардуино и включить соответствующий режим приема
                             * 3 Отправить информацию с ардуино. В конце поставить знак окончания передачи
                             * и при его приеме переключить режим обратно
                             */
                            if (indexOfN > 0) {
                                val ind = stringBuilder.indexOf("\n")
                                val str1 = stringBuilder.substring(0, ind)
                                if (str1.equals("DONE")) {
                                    mode = 1231
                                    Log.d("myLog", "Закончено")
                                    counter = true
                                }
                                val str2 = stringBuilder.substring(ind + 1, stringBuilder.length)
                                Log.d("myLog", "Строка1: $str1")
                                stringBuilder.delete(0, stringBuilder.length)
                                stringBuilder.append(str2)
                                Log.d("myLog", "Строка2: $str2")
                            }
                        } catch (e: IOException) {
                            Log.d("MyLog", "Input stream was disconnected", e)
                            break
                        }
                    }
                }

                // Экспериментальный. С обработкой файлов
                if (mode == 4532) {
                    var counter2 = 0
                    if (counter) {
                        handler.sendMessage(handler.obtainMessage(1, "Ожидание передачи"))
                        Log.d("myLog", "Режим сброса данных: $mode")
                        counter = false
                        list.clear()
                    }
                    while (mode == 4532) {
                        try {
                            if (counter2 == 0) {
                                handler.sendMessage(handler.obtainMessage(2, "..."))
                                counter2 += 1
                            } else if (counter2 == 1) {
                                handler.sendMessage(handler.obtainMessage(2, "...."))
                                counter2 += 1
                            } else if (counter2 == 2) {
                                handler.sendMessage(handler.obtainMessage(2, "....."))
                                counter2 = 0
                            }
                            var bytes = mmInStream!!.available()
                            if (bytes == 0) {
                                sleep(10)
                                continue
                            }
                            val buffer = ByteArray(bytes) // buffer store for the stream

                            // Read from the InputStream
                            bytes = mmInStream.read(buffer) // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                            val str = String(buffer)
                            //Log.d("myLog", "Строка0: $str")
                            stringBuilder.append(str)
                            val indexOfN = stringBuilder.indexOf("\n")
                            /** 1 Отправить команду на арудино
                             * 2 Принять команду от ардуино и включить соответствующий режим приема
                             * 3 Отправить информацию с ардуино. В конце поставить знак окончания передачи
                             * и при его приеме переключить режим обратно
                             */
                            while (stringBuilder.indexOf("\n") > 0) {
                                val index = stringBuilder.indexOf("\n")
                                val str1 = stringBuilder.substring(0, index)
                                list.add(str1)
                                //Log.d("myLog", "Строка1: $str1")
                                if (str1.equals("DONE")) {
                                    mode = 1231
                                    Log.d("myLog", "Закончено")
                                    handler.sendMessage(handler.obtainMessage(1, "Передача закончена"))
                                    counter = true
                                    break
                                }
                                val str2 = stringBuilder.substring(index + 1, stringBuilder.length)
                                //Log.d("myLog", "Строка2: $str2")
                                stringBuilder.delete(0, stringBuilder.length)
                                stringBuilder.append(str2)
                            }
                        } catch (e: IOException) {
                            Log.d("MyLog", "Input stream was disconnected", e)
                            break
                        }
                    }
                }

            }
        }

    fun sendMessage(message: String) {
        try {
            val messageBuffer = message.toByteArray()
            mmOutStream?.write(messageBuffer)
        } catch (e: Exception) {

        }
    }
    }
