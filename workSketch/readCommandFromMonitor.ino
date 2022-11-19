// Функция чтения команд из порта
void readCommand() {
  if (Serial.available() > 0) {

    int COMMAND_NUMBER = Serial.parseInt();  // Считываем с порта число команды

    // Первая команда (при числе команды "1"). Выводит список файлов.
    if (COMMAND_NUMBER == 1) {
      Serial.println(F("Команда вывода списка файлов"));
      root2 = SD.open("/");
      printDirectory(root2, 0);
      root2.close();
      listCommands();
    }

    // Вторая команда (при числе команды "2"). Создает файл с заданным именем.
    // Рабочая. Просто закоментирована для освобождения места
    //    if (COMMAND_NUMBER == 2) {
    //      Serial.println(F("Команда создания файла"));
    //      Serial.println(F("Введите имя нового файла"));
    //      Serial.println(F("В найстройках порта выберете Нет конца строки"));
    //
    //      // Очистка порта
    //      while (Serial.available()) Serial.read();
    //
    //      // Строка для хранения имени файла
    //      String name = "";
    //
    //      // Ожидаем ввода имени нового файла
    //      while (true) {
    //        if (Serial.available() > 0) {
    //          name = Serial.readString();  // Считываем имя нового файла
    //          name += ".txt";              // Добавляем тип файла
    //          Serial.println("Имя нового файла: " + name);
    //          // Проверяем есть ли уже такой файл
    //          if (!SD.exists(name)) {
    //            Serial.println(F("Такого файла нет"));
    //          } else {
    //            Serial.println(F("Файл с таким именем уже существует"));
    //            break;
    //          }
    //
    //          Serial.println(F("Создание файла"));
    //          myFile = SD.open(name, FILE_WRITE);
    //          if (myFile) Serial.println(F("Файл создан"));
    //          myFile.close();
    //          break;
    //        }
    //      }
    //      listCommands();
    //    }

    // Третья команда (при числе команды "3"). Удаляет файл с заданным именем.
    if (COMMAND_NUMBER == 3) {
      Serial.println(F("Команда удаления файла"));
      Serial.println(F("Введите имя удаляемого файла"));
      Serial.println(F("В найстройках порта выберете Нет конца строки"));

      // Очистка порта
      while (Serial.available()) Serial.read();

      // Строка для хранения имени удаляемого файла
      String name = "";

      // Ожидаем ввода имени удаляемого файла
      while (true) {
        if (Serial.available() > 0) {
          name = Serial.readString();  // Считываем имя удаляемого файла
          name += ".txt";              // Добавляем тип файла
          Serial.println("Имя удаляемого файла: " + name);
          // Проверяем есть ли такой файл
          if (!SD.exists(name)) {
            Serial.println(F("Такого файла нет"));
            break;
          } else {
            Serial.println(F("Файл с таким именем существует"));
          }

          Serial.println(F("Удаление файла"));
          if (SD.remove(name)) {
            Serial.println(F("Файл удален"));
          } else {
            Serial.println(F("Файл не удален"));
          }
          break;
        }
      }
      listCommands();
    }

    // Команда вывода содержимого файла в монитор порта
    if (COMMAND_NUMBER == 4) {
      Serial.println(F("Команда вывода содержимого файла"));
      Serial.println(F("Введите имя выводимого файла"));
      Serial.println(F("В найстройках порта выберете Нет конца строки"));
      // Очистка порта
      while (Serial.available()) Serial.read();
      // Строка для хранения имени удаляемого файла
      String name = "";
      // Ожидаем ввода имени удаляемого файла
      while (true) {
        if (Serial.available() > 0) {
          name = Serial.readString();  // Считываем имя удаляемого файла
          name += ".txt";              // Добавляем тип файла
          Serial.print(F("Имя выводимого файла: "));
          Serial.println(name);
          // Проверяем есть ли такой файл
          if (!SD.exists(name)) {
            Serial.println(F("Такого файла нет"));
            break;
          } else {
            Serial.println(F("Файл с таким именем существует"));
          }

          Serial.println(F("Вывод содержимого файла"));

          myFile = SD.open(name);
          if (myFile) {
            while (myFile.available()) {
              Serial.write(myFile.read());
            }
            myFile.close();
            listCommands();
            break;
          } else {
            // if the file didn't open, print an error:
            Serial.println(F("error opening test.txt"));
            listCommands();
            break;
          }
        }
      }
    }

    // // Команда отправки на телефон
    // if (COMMAND_NUMBER == 5) {
    //   //Serial.println(F("Команда отправки содержимого файла на телефон"));
    //   //Serial.println(F("Введите имя отправляемого файла"));
    //   //Serial.println(F("В найстройках порта выберете Нет конца строки"));
    //   // Очистка порта
    //   while (Serial.available()) Serial.read();
    //   // Строка для хранения имени отправляемого файла
    //   String name = "";
    //   // Ожидаем ввода имени отправляемого файла
    //   while (true) {
    //     if (Serial.available() > 0) {
    //       name = Serial.readString();  // Считываем имя отправляемого файла
    //       name += ".txt";              // Добавляем тип файла
    //       //Serial.print(F("Имя отправляемого файла: "));
    //       //Serial.println(name);
    //       // Проверяем есть ли такой файл
    //       if (!SD.exists(name)) {
    //         //Serial.println(F("Такого файла нет"));
    //         break;
    //       } else {
    //         //Serial.println(F("Файл с таким именем существует"));
    //       }
    //       //Serial.println(F("Отправка содержимого файла"));
    //       myFile = SD.open(name);
    //       if (myFile) {
    //         while (myFile.available()) {
    //           mySerial.write(myFile.read());
    //         }
    //         myFile.close();
    //         //listCommands();
    //         break;
    //       } else {
    //         // if the file didn't open, print an error:
    //         //Serial.println(F("error opening test.txt"));
    //         //listCommands();
    //         break;
    //       }
    //     }
    //   }
    // }

    // Команда вывода времени в монитор порта
    if (COMMAND_NUMBER == 6) {
      Serial.println(time.gettime("d-m-Y, H:i:s, D"));  // выводим время
      listCommands();
    }

    // Команда режима настройки времени
    // if (COMMAND_NUMBER == 7) {
    //   Serial.println(F("Отправьте время с телефона"));
    //   while (1) {
    //     if (funSetTimeInRTC() == true) break;
    //   }
    //   Serial.println(F("Настройка времени завершена"));
    //   Serial.print(F("Время: "));
    //   Serial.println(watch.gettime("d-m-Y, H:i:s, D"));
    //   listCommands();
    // }

    if (COMMAND_NUMBER == 8) {
      isWrite = !isWrite;
      listCommands();
    }

    if (COMMAND_NUMBER == 11) {
      // Очистка порта
      long counter;
      while (Serial.available()) Serial.read();
      while (true) {
        if (millis() - counter > 1000) {
          counter = millis();
          Serial.print(".");
        }
        if (Serial.available() > 0) {
          while (Serial.available()) {
            mySerial.write(Serial.read());
          }
          Serial.println(F("Sent"));
          break;
        }
      }
    }
  }
}