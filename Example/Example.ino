

// Модуль времени конфликтует с модулем SD.
// Поэтому пока убираем модуль времени. Будем отсчитывать по БШВ.
// Второй вариант пока отложенный, как то их срастить вместе
// Сделать периодическое тестирование в то случае если флаг ошибки поднят
// Есть подозрение что серво залипает. Добавить положение серво в строку вывода
// Добавить блютуз модуль
// Настройку времени задавать самому по телефону. В программе написать код который время будет сам отсчитывать


// Time //////////////////////////////////////////////////////
//#include <iarduino_RTC.h>
// RST 7
// CLK 5
// DAT 6
// Порядок ввода RST, CLK, DAT
//iarduino_RTC time(RTC_DS1302, 7, 5, 6);
// Time //////////////////////////////////////////////////////


// PVMServo
#include <PWMServo.h>
PWMServo myservo;

// Servo /////////////////////////////////////////////////////
#include <Servo.h>
//Servo myservo;  // create servo object to control a servo
long counterWaker; //Счетчик для таймера будильника повербанка
int wakerDelay = 6000; // Время задержки работы серво-будильника повербанка
boolean flagReverse; // Флаг для обратного направления движения сервопривода
// Servo /////////////////////////////////////////////////////



// SD ////////////////////////////////////////////////////////
// Распиновка SD модуля. Начитается с квадратика на модуле
// 1. 3,3 V (любой питающий пин)
// 2. CS (4 цифровой пин)
// 3. MOSI (11 цифровой пин)
// 4. CLK (13 цифровой пин)
// 5. MISO (12 цифровой пин)
// 6. GND (любой заземляющий пин)
#include <SPI.h>
#include <SD.h>
Sd2Card card;
SdVolume volume;
SdFile root;
const int chipSelect = 4;
long counterSD; //Счетчик для работы с SD
long sdDelay = 300000; // Время задержки работы SD
// SD ////////////////////////////////////////////////////////



// DHT11 (датчик температуры и влажности)/////////////////////
#include "DHT.h"
#define DHTPIN 5 // Тот самый номер пина, о котором упоминалось выше
DHT dht(DHTPIN, DHT11);
// DHT11 (датчик температуры и влажности)/////////////////////



// HC05 (Bluetooth модуль)//./////////////////////////////////
#include <SoftwareSerial.h>
// указываем пины rx и tx соответственно. Это на плате. 
//Ответные пины на модуле наоборот (rx на tx, tx на rx)
SoftwareSerial mySerial(2, 3); 
long counterBluetooth;
// HC05 (Bluetooth модуль)///////////////////////////////////



// Счётчик времени (типа БШВ)
long secondsSinceStart;
long counterTime;



// Переменные для сигнализатора ошибок
long counterError;
boolean errorFlag;

// Счетчик для таймера для тестов
long counterTest;

void setup() {
  // Настройка bluetooth модуля
  pinMode(2, INPUT);
  pinMode(3, OUTPUT);
  mySerial.begin(9600);

  // Открытие порта для вывода
  Serial.begin(9600);

  // Настройка модуля времени
  //time.begin();
  //time.period(20);
  // Тест и установка времнеи в тестовой функции
  // Подумать о переносе инициализации туда же

  // Найстройка сервопривода
  myservo.attach(SERVO_PIN_A);  // attaches the servo on pin 9 to the servo object

  // Найстройка датчика DHT11
  dht.begin();

  testSystems();
}

void loop(void) {

  servoWaker();

  timeCounter();

  check();

  errorAlarm();

  blueTest();


  // Таймер с задержкой в 1 сек для тестов (не удалять)
  if (millis() - counterTest > 10000) {
    counterTest = millis();
    //Serial.println("Loop is working");

    //Serial.println(time.gettime("d-m-Y, H:i:s, D"));
  }
}

void blueTest() {
  //counterBluetooth
  //mySerial
  if (millis() - counterBluetooth > 1000) {
    counterBluetooth = millis();

    //mySerial.println("Position servo: " + String(myservo.read()));
    //mySerial.println("Test" + String(secondsSinceStart));

    mySerial.println("Temperature: " + String(dht.readTemperature()));
  }
}

void testSystems() {
  // Тест датчика температуры и влажности DHT11/////////////////////////////////////////////////////////////////////////////////////////
  float h = dht.readHumidity(); //Измеряем влажность
  float t = dht.readTemperature(); //Измеряем температуру
  if (isnan(h) || isnan(t)) {  // Проверка. Если не удается считать показания, выводится «Ошибка считывания», и программа завершает работу
    Serial.println("Датчик температуры и влажности DHT11: ошибка считывания");
    errorFlag = 1; // Включаем сигнализацию ошибки
  } else {
    String testString = "Датчик DHT11 работает.\tТемпература: " + String(t, 3) + ". Влажность: " + String(h, 3) + ".";
    Serial.println(testString);
    Serial.println();
  }
  // Тест датчика температуры и влажности DHT11/////////////////////////////////////////////////////////////////////////////////////////


  // Тест модуля времени ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Определяем системное время:                           // Время загрузки скетча.
  //  const char* strM = "JanFebMarAprMayJunJulAugSepOctNovDec"; // Определяем массив всех вариантов текстового представления текущего месяца.
  //  const char* sysT = __TIME__;                              // Получаем время компиляции скетча в формате "SS:MM:HH".
  //  const char* sysD = __DATE__;                              // Получаем дату  компиляции скетча в формате "MMM:DD:YYYY", где МММ - текстовое представление текущего месяца, например: Jul.
  //  Парсим полученные значения sysT и sysD в массив i:    // Определяем массив «i» из 6 элементов типа int, содержащий следующие значения: секунды, минуты, часы, день, месяц и год компиляции скетча.
  //  const int i[6] {(sysT[6] - 48) * 10 + (sysT[7] - 48),
  //                  (sysT[3] - 48) * 10 + (sysT[4] - 48),
  //                  (sysT[0] - 48) * 10 + (sysT[1] - 48),
  //                  (sysD[4] - 48) * 10 + (sysD[5] - 48),
  //                  ((int)memmem(strM, 36, sysD, 3) + 3 - (int)&strM[0]) / 3,
  //                  (sysD[9] - 48) * 10 + (sysD[10] - 48)
  //                 };
  // Для установки системного времени при загрузке скетча расскоментировать строку ниже
  // прошить, закоментировать и прошить еще раз чтобы при перезагрузке ардуино не записывалось время последней прошивки
  //time.settime(i[0] + 16, i[1], i[2], i[3], i[4], i[5]);
  //  Serial.println("Тестовый вывод времени: " + String(time.gettime("d-m-Y, H:i:s, D")));
  //  Serial.println();
  // Тест модуля времени ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  // Тест SD модуля ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  if (SD.begin(4)) {
    Serial.println("SD: инициализация успешна.");
  } else {
    Serial.println("SD: ощибка инициализации.");
    errorFlag = 1; // Включаем сигнализацию об ошибке
  }
  // Тест SD модуля ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  // Настройка и тест сервопривода /////////////////////////////////////////////////////////////////////////////////////////////////////
  // Тестовые вращения
  myservo.write(0);
  delay(1000);
  myservo.write(180);
  delay(1000);
  myservo.write(20);
  // Настройка и тест сервопривода /////////////////////////////////////////////////////////////////////////////////////////////////////


}

void check() {

  if (millis() - counterSD > sdDelay) {
    counterSD = millis();

    String dataString = stringToWrite();

    //Serial.println(dataString);

    // открываем файл, в который будет записана строка
    File dataFile = SD.open("test.txt", FILE_WRITE);

    if (dataFile) {
      // записываем строку в файл
      dataFile.println(dataString);
      dataFile.close();
      //Serial.println("Success!");
    } else {
      // выводим ошибку если не удалось открыть файл
      Serial.println("error opening file");
    }
  }
}

// Строка, которую мы запишем в файл
String stringToWrite() {

  // строка, которую мы запишем в файл
  float h = dht.readHumidity(); //Измеряем влажность
  float t = dht.readTemperature(); //Измеряем температуру
  if (isnan(h) || isnan(t)) {  // Проверка. Если не удается считать показания, выводится «Ошибка считывания», и программа завершает работу
    Serial.println("Ошибка считывания");
    return;
  }

  // Строка при подключенном модуле времени
  //  String dataString = "Time:\t" + String(time.gettime("d-m-Y, H:i:s, D")) + "\t"
  //                      + "Time since start:\t" + String(secondsSinceStart) + "\t"
  //                      + "Humidity:\t" + String(h, 3) + "\t"
  //                      + "Temperature:\t" + String(t, 3);

  // Без модуля времени (основаная рабочая строка, в обычном случае использовать ее)
  //  String dataString = "Time since start:\t" + String(secondsSinceStart) + "\t"
  //                      + "Humidity:\t" + String(h, 3) + "\t"
  //                      + "Temperature:\t" + String(t, 3);

  // Обычная строка плюс положение сервопривода (тестовая. тестируем залипание сервопривода)
  String dataString = "Time since start:\t" + String(secondsSinceStart) + "\t"
                      + "Humidity:\t" + String(h, 3) + "\t"
                      + "Temperature:\t" + String(t, 3) + "\t"
                      + "Servo Position:\t" + String(myservo.read());

  //Serial.println(dataString);

  return dataString;
}

void checkSD() {

  Serial.print("\nInitializing SD card...");

  if (!card.init(SPI_HALF_SPEED, chipSelect)) {
    // неверное подключение или карта неисправна
    Serial.println("initialization failed");
    return;
  } else {
    // всё ок!
    Serial.println("Wiring is correct and a card is present.");
  }

  // считываем тип карты и выводим его в COM-порт
  Serial.print("\nCard type: ");
  switch (card.type()) {
    case SD_CARD_TYPE_SD1:
      Serial.println("SD1");
      break;
    case SD_CARD_TYPE_SD2:
      Serial.println("SD2");
      break;
    case SD_CARD_TYPE_SDHC:
      Serial.println("SDHC");
      break;
    default:
      Serial.println("Unknown");
  }

  // инициализация файловой системы
  if (!volume.init(card)) {
    // неверная файловая система
    //Serial.println("Could not find FAT16/FAT32 partition.");
    return;
  }

  // считываем тип и вычисляем размер первого раздела
  uint32_t volumesize;
  //Serial.print("\nVolume type is FAT");
  //Serial.println(volume.fatType(), DEC);
  //Serial.println();

  volumesize = volume.blocksPerCluster(); // блоков на кластер
  volumesize *= volume.clusterCount(); // кластеров
  volumesize *= 512; // 512 байтов в блоке, итого байт..
  //Serial.print("Volume size (bytes): ");
  //Serial.println(volumesize);
  //Serial.print("Volume size (Kbytes): ");
  volumesize /= 1024;
  //Serial.println(volumesize);
  //Serial.print("Volume size (Mbytes): ");
  volumesize /= 1024;
  //Serial.println(volumesize);

  //Serial.println("\nFiles found on the card (name, date and size in bytes): ");
  root.openRoot(volume);
  // выводим список файлов
  root.ls(LS_R | LS_DATE | LS_SIZE);

  if (!SD.begin(chipSelect)) {
    //Serial.println("Card failed, or not present");
    return;
  }
}

// Отсчитывает время со старта работы в секундах (отработан, трогать не нужно!!)
void timeCounter() {

  if (millis() - counterTime > 1000) {
    counterTime = millis();

    secondsSinceStart = secondsSinceStart + 1;
  }
}

// Будильник повербанка (отработан, трогать не нужно!!)
void servoWaker() {
  if (millis() - counterWaker > wakerDelay) {
    counterWaker = millis();

    if (myservo.read() >= 160) flagReverse = 1;
    if (myservo.read() <= 20) flagReverse = 0;

    if (flagReverse == 0) {
      myservo.write(myservo.read() + 10);
    } else if (flagReverse == 1) {
      myservo.write(myservo.read() - 10);
    }
  }
}

// Сигнализация об ошибке. Если флаг поднят то мигаем светодиодом порта
void errorAlarm() {
  if (errorFlag) {
    if (millis() - counterError > 1000) {
      counterError = millis();
      Serial.print("Error");
    }
  }
}
