void readCommandFromAndroid() {
  if (mySerial.available() > 0) {
    int COMMAND_NUMBER = mySerial.readString().toInt();
    // Команда включения/выключения записи ТМИ
    if (COMMAND_NUMBER == 1563) {
      isWrite = !isWrite;
    }

    // Команда настройки времени
    if (COMMAND_NUMBER == 2812) {
      mySerial.write("3842\n");
      while (1) {
        if (funSetTimeInRTCNew() == true) break;
      }
      mySerial.write(time.gettime("d-m-Y, H:i:s, D"));
      mySerial.write("\n");
      delay(1000);
      mySerial.write("8432\n");
    }

    // Команда сброса данных на телефон
    if (COMMAND_NUMBER == 3421) {
      while (mySerial.available()) mySerial.read();
      mySerial.write("4532\n");
      // Строка для хранения имени отправляемого файла
      String name = "";
      // Ожидаем ввода имени отправляемого файла
      while (true) {
        if (mySerial.available() > 0) {
          name = mySerial.readString();  // Считываем имя отправляемого файла
          name += ".txt";                // Добавляем тип файла
          if (!SD.exists(name)) {
            //Serial.println(F("Такого файла нет"));
            break;
          } else {
            //Serial.println(F("Файл с таким именем существует"));
          }
          //Serial.println(F("Отправка содержимого файла"));
          myFile = SD.open(name);
          if (myFile) {
            Serial.println("Start");
            while (myFile.available()) {
              //Serial.println(myFile.available());
              mySerial.write(myFile.read());
              //Serial.println(".");
              //delay();
            }
            Serial.println(F("Finished"));
            myFile.close();
            delay(1000);
            mySerial.write("DONE");
            mySerial.write("\n");
            break;
          } else {
            // if the file didn't open, print an error:
            //Serial.println(F("error opening test.txt"));
            //listCommands();
            break;
          }
        }
      }
    }
  }
}
