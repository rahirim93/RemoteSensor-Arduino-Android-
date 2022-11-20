package com.example.remotesensor

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class ConnectedThread (mmSocket: BluetoothSocket?, myHandler: Handler) : Thread() {
        private val mmInStream: InputStream? = mmSocket?.inputStream
        private val mmOutStream: OutputStream? = mmSocket?.outputStream
        private var counter = true
        var list = ArrayList<String>()
        private var handler = myHandler
        var sum2 = 0



    override fun run() {
            val counterOf = Calendar.getInstance().timeInMillis
            val stringBuilder = StringBuilder()
            var mode = 1231
            // 3212 - прием тми
            // 1231 - считывание команд
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                /** Понять что соединине разорвано можно только отправив сообщение на другую сторону.
                 * В случае возникновения ошибки отправки, функция выбрасывает исключение, по которому
                 мы понимаем что соединение разоравно.
                 Поэтому периодически отправляем пустое сообщение, для проверки возможности отправки
                 Если передача неуспешна, то программа переходит к блоку исключения функции write
                * @see sendMessage */
                if (Calendar.getInstance().timeInMillis - counterOf > 100) {
                    sendMessage("")
                }


                // Read from the InputStream.
                // Режим считывания команд
                if (mode == 1231) {
                    if (counter) {
                        handler.sendMessageDelayed(handler.obtainMessage(1, "Ожидание команды"), 2000)
                        Log.d("myLog", "Режим считывания команд: $mode")
                        counter = false
                        handler.sendMessage(handler.obtainMessage(4))
                        list.forEach {
                            Log.d("myLog", it)
                        }
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
                            } else if(str1.equals("3021")) {
                                sum2 += 1
                                handler.sendMessage(handler.obtainMessage(3, "Получено $sum2"))
                            }
                            val str2 = stringBuilder.substring(ind + 1, stringBuilder.length)
                            Log.d("myLog", "Строка1: $str1")
                            Log.d("myLog", "Строка1: ${Calendar.getInstance().timeInMillis}")
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

                // Режим получения данных с ардуино
                /** Во время режима считывания информации от арудино
                 * в бесконечном цикле, в случае есть арудино ничего не отправляет,
                 * т.е. на стороне ардуино инфа не передается, сделать таймер который
                 * через определенное время выключит этот режим, чтобы программа не зависла */
                if (mode == 4532) {
                    var sum = 0
                    var counter2 = 0
                    // Выполняется один раз при срабатывании команды
                    if (counter) {
                        // Отправка имени файла для сброса
                        val calendar = Calendar.getInstance()
                        val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
                        Log.d("myLog", sdf.format(calendar.time))
                        sendMessage(sdf.format(calendar.time))

                        handler.sendMessage(handler.obtainMessage(1, "Ожидание передачи"))
                        Log.d("myLog", "Режим сброса данных: $mode")
                        counter = false
                        list.clear()
                    }
                    while (mode == 4532) {
                        /** Работа алгоритма следующая:
                         *      1. Алгоритм считывает какие-то байты. Переводит их в строку. Строку добавляет в стрингдилдер.
                         *
                         *      2. Если в стрингбилдере нет символа окончания строки "\n", то алгоритм повторяем пункт 1
                         *
                         *      3. Если в строке есть символ окончания строки "\n", то алгоритм переходит к пункту 4
                         *
                         *      4. В строке могут быть несколько символов "\n", т.е. в стрингдилдер могут быть считаны несколько
                         *  строк данных. IndexOf находит только первый встречающийся символ "\n". Поэтому далее алгоритм зацикливается
                         *  циклом while и данные извлекаются, пока в стрингдилдере не окажется символа окончания строки,
                         *  следующим образом: из стрингбилдера считывается строка с первым встреченным символом окончания строки и
                         *  записывается в массив. Далее цикл while опять ищет символ окончания строки и если его нет, то переходит к шагу 1.
                         *
                         *      5. Завершение режима считывания. При окончании передачи ардуино отправляет слово "DONE".
                         *      При считывании слова "DONE", режим работы сменяется, ветка режима получения данных прерывается
                         *      цикл while Прерывается.
                         *
                         */
                        try {
                            var bytes = mmInStream!!.available()
                            if (bytes == 0) {
                                sleep(10)
                                continue
                            }
                            val buffer = ByteArray(bytes) // buffer store for the stream

                            // Read from the InputStream
                            bytes = mmInStream.read(buffer)                                  // Получаем кол-во байт и само собщение в байтовый массив "buffer"
                            val str = String(buffer)                                         // Формируем строку из буфера
                            sum += bytes                                                     // Прибавляем количество байтов
                            handler.sendMessage(handler.obtainMessage(2, "$sum"))   // Отправляем количество байтов для вывода в поле
                            stringBuilder.append(str)                                        // Прибавляем сформированную строку к стрингбилдеру

                            while (stringBuilder.indexOf("\n") > 0) {                        // Пока в стринбилдере есть конец строки
                                val index = stringBuilder.indexOf("\n")                      // Находим индекс символа конца строки
                                val str1 = stringBuilder.substring(0, index)                 // Выделяем строку 1 с концам строки
                                if (str1.equals("DONE")) {                                   // В случае получения слова "DONE"(признак окончания передачи)
                                    mode = 1231                                              // Переключить режим на режим приема команд
                                    Log.d("myLog", "Закончено")
                                    handler.sendMessage(handler.obtainMessage(1, "Передача закончена"))
                                    counter = true
                                    break
                                }
                                list.add(str1)                                                      // Добавляем в массив строку 1
                                val str2 = stringBuilder.substring(index + 1, stringBuilder.length) // Остаток после выделения строки выделяем в строку 2
                                stringBuilder.delete(0, stringBuilder.length)                       // Очищаем стрингбилдер
                                stringBuilder.append(str2)                                          // Добавляем остаток для дальнейшей обработки
                            }
                        } catch (e: IOException) {
                            handler.sendMessage(handler.obtainMessage(6))
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
            /** Если отправка вызывает блок исключения, значит соединения разорвано и программа
             * уведомлет об этом handler
             * @see MainActivity.handler */
            handler.sendMessage(handler.obtainMessage(7))
        }
    }
    }
