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
long counter1;

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

boolean isWrite = true;  // Флаг для хранения состояния вкл.\выкл. запись ТМИ

void setup() {
  // Найстройка датчика DHT11
  dht.begin();

  time.begin();
  pinMode(2, INPUT);
  pinMode(3, OUTPUT);
  mySerial.begin(9600);
  Serial.begin(9600);
  initCard();
}

void loop() {

  readCommandFromAndroid();  // Прием команд от телефона

  readCommand();  // Прием команд с монитора порта

  // Если запись ТМИ включана то пишем на карту
  if (isWrite) {
    // Новый счетчик привязанный к модулю времени
    sdCounter();
  }
}

// Создание файла и запись ТМИ
void writeDataToSD() {
  String a = time.gettime("dmY");   // Получаем дату для формирования имени файла в формате ГГГГММДД
  a.concat(".txt");                 // Прибавляем расширение файла
  myFile = SD.open(a, FILE_WRITE);  // Открываем/создаем для записи
  if (myFile) {
    myFile.print(stringOfData());  // Записываем строку с ТМИ
    myFile.close();                // Закрываем
  } else {
    myFile.close();
  }
}



// Функция получения времени в минутах
int timeInMinutes() {
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

// Вспомогательная для вывода списка файлов
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

// Инициализация карты SD
void initCard() {
  Serial.print(F("Initializing SD card..."));
  if (!SD.begin(4)) {
    Serial.println(F("initialization failed!"));
    while (1)
      ;
  }
  Serial.println(F("initialization done."));
}