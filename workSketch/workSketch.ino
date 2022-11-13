// Для работы с модулем времени
#include <iarduino_RTC.h>
iarduino_RTC time(RTC_DS3231);
// SDA - A4, SCL - A5
// Для работы с модулем времени

// DHT11 (датчик температуры и влажности)/////////////////////
#include "DHT.h"
#define DHTPIN 8  // Тот самый номер пина, о котором упоминалось выше
DHT dht(DHTPIN, DHT11);
// DHT11 (датчик температуры и влажности)/////////////////////

#include <SoftwareSerial.h>

SoftwareSerial mySerial(2, 3);  // указываем пины rx и tx соответственно

long counter;

// Для работы с SD
#include <SPI.h>
#include <SD.h>
Sd2Card card;
SdVolume volume;
SdFile root;
const int chipSelect = 4;
File root2;
File myFile;
// Для работы с SD

boolean isWrite = true;

void setup() {
  // Найстройка датчика DHT11
  dht.begin();

  time.begin();
  pinMode(2, INPUT);
  pinMode(3, OUTPUT);
  mySerial.begin(9600);
  Serial.begin(9600);
  initCard();
  //listCommands();
}

void loop() {
  readCommandFromAndroid();

  readCommand();

  if (isWrite) {
    if (millis() - counter > 300000) {
      counter = millis();
      test2222();
    }
  }
}

void test2222() {
  //Serial.println(time.gettime("d.m.Y"));
  String a = time.gettime("dmY");
  a.concat(".txt");
  //Serial.println(a);
  myFile = SD.open(a, FILE_WRITE);
  if (myFile) {
    myFile.print(stringOfData());
    myFile.close();
  }
}

// Возвращает втроку для записи в файл
String stringOfData() {
  String curentTime = time.gettime("H:i:s");  // Текущее время

  float h = dht.readHumidity();     //Измеряем влажность
  float t = dht.readTemperature();  //Измеряем температуру
  if (isnan(h) || isnan(t)) {       // Проверка. Если не удается считать показания, выводится «Ошибка считывания», и программа завершает работу
    Serial.println(F("Ошибка считывания"));
    return;
  }

  curentTime.concat("T");
  curentTime.concat('\t');
  curentTime.concat(h);
  curentTime.concat("H");
  curentTime.concat('\t');
  curentTime.concat(t);
  curentTime.concat("t");
  curentTime.concat('\n');



  return curentTime;
}

boolean funSetTimeInRTC() {
  // Очистка порта
  //while (mySerial.available()) mySerial.read();
  //Протокол взаимодействия
  // C андроида нужно отправить строку вида: ГОД+Y+МЕСЯЦ+M+ДЕНЬ+D+ЧАСЫ+H+МИНУТЫ+m+СЕКУНДЫ+S
  if (mySerial.available() > 0) {
    String a = "";
    a.concat(mySerial.readString());  // Получаем строку с телефона
    // Находим индексы разделителей
    int yearIndex = a.indexOf("Y");     // Индекс года
    int monthIndex = a.indexOf("M");    // Индекс месяца
    int dayIndex = a.indexOf("D");      // Индекс дня
    int hoursIndex = a.indexOf("H");    // Индекс часов
    int minutesIndex = a.indexOf("m");  // Индекс минут
    int secondsIndex = a.indexOf("S");  // Индекс секунд
    // Извлекаем данные по найденным индексам
    // При настройке времени используются только два последних символа года.
    // Нарпимер, при годе 2022 это 22. Поэтому вытаскиваем только две последние цифры.
    int year = (a.substring(2, yearIndex)).toInt();
    // В андроиде месяцы от 0 до 11, в ардуино от 1 до 12. Поэтому при настройке прибалвяем 1.
    int month = (a.substring(yearIndex + 1, monthIndex)).toInt() + 1;
    int day = (a.substring(monthIndex + 1, dayIndex)).toInt();
    int hours = (a.substring(dayIndex + 1, hoursIndex)).toInt();
    int minutes = (a.substring(hoursIndex + 1, minutesIndex)).toInt();
    int seconds = (a.substring(minutesIndex + 1, secondsIndex)).toInt() + 2;
    // Настраиваем время
    time.settime(seconds, minutes, hours, day, month, year);
    return 1;
  } else return false;
}

void readCommandFromAndroid() {
  if (mySerial.available() > 0) {
    int COMMAND_NUMBER = mySerial.readString().toInt();
    // Команда включения/выключения записи ТМИ
    if (COMMAND_NUMBER == 1563) {
      isWrite = !isWrite;
    }

    // Команда настройки времени
    if (COMMAND_NUMBER == 2812) {
      mySerial.write("Setting time");
      mySerial.write("\n");
      while (1) {
        if (funSetTimeInRTC() == true) break;
      }
      mySerial.write(time.gettime("d-m-Y, H:i:s, D"));
      mySerial.write("\n");
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
    if (COMMAND_NUMBER == 2) {
      Serial.println(F("Команда создания файла"));
      Serial.println(F("Введите имя нового файла"));
      Serial.println(F("В найстройках порта выберете Нет конца строки"));

      // Очистка порта
      while (Serial.available()) Serial.read();

      // Строка для хранения имени файла
      String name = "";

      // Ожидаем ввода имени нового файла
      while (true) {
        if (Serial.available() > 0) {
          name = Serial.readString();  // Считываем имя нового файла
          name += ".txt";              // Добавляем тип файла
          Serial.println("Имя нового файла: " + name);
          // Проверяем есть ли уже такой файл
          if (!SD.exists(name)) {
            Serial.println(F("Такого файла нет"));
          } else {
            Serial.println(F("Файл с таким именем уже существует"));
            break;
          }

          Serial.println(F("Создание файла"));
          myFile = SD.open(name, FILE_WRITE);
          if (myFile) Serial.println(F("Файл создан"));
          myFile.close();
          break;
        }
      }
      listCommands();
    }

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

void printDirectory(File dir, int numTabs) {
  while (true) {
    File entry = dir.openNextFile();
    if (!entry) {
      // файлов больше нет
      break;
    }
    for (uint8_t i = 0; i < numTabs; i++) {
      Serial.print('\t');
    }
    Serial.print(entry.name());
    if (entry.isDirectory()) {
      Serial.println("/");
      printDirectory(entry, numTabs + 1);
    } else {
      // у файлов есть размеры, у каталогов - нет
      Serial.print("\t\t");
      Serial.println(entry.size(), DEC);
    }
    entry.close();
  }
}

// Функция вывода списка команд
void listCommands() {
  Serial.println(F("Список команд:"));
  Serial.println(F("1 - Вывести список файлов"));
  Serial.println(F("2 - Создать файл"));
  Serial.println(F("3 - Удалить файл"));
  Serial.println(F("4 - Вывод содержимого файла в монитор порта"));
  Serial.println(F("5 - Отправка содержимого файла на телефон"));
  Serial.println(F("6 - Вывод времени"));
  Serial.println(F("8 - Включить/выключить режим записи"));
}

void initCard() {
  Serial.print(F("Initializing SD card..."));
  if (!SD.begin(4)) {
    Serial.println(F("initialization failed!"));
    while (1)
      ;
  }
  Serial.println(F("initialization done."));
}