#include <iarduino_RTC.h>
iarduino_RTC time(RTC_DS3231);

#include <SoftwareSerial.h>

SoftwareSerial mySerial(2, 3); // указываем пины rx и tx соответственно

#include <SPI.h>
#include <SD.h>

Sd2Card card;
SdVolume volume;
SdFile root;

const int chipSelect = 4;

File root2;
File myFile;


void setup() {
  time.begin();
  pinMode(2, INPUT);
  pinMode(3, OUTPUT);
  mySerial.begin(9600);
  Serial.begin(9600);
  Serial.println(F("onSetup"));
  initCard();
  listCommands();
}

void loop() {
  readCommand();

  if (millis() % 1000 == 0) { // если прошла 1 секунда
    Serial.println(time.gettime("d-m-Y, H:i:s, D")); // выводим время
    delay(1); // приостанавливаем на 1 мс, чтоб не выводить время несколько раз за 1мс
  }
}

// Функция чтения команд из порта
void readCommand() {
  if (Serial.available() > 0) {

    int COMMAND_NUMBER = Serial.parseInt(); // Считываем с порта число команды

    // Первая команда (при числе команды "1")
    // Выводит список файлов
    if (COMMAND_NUMBER == 1) {
      Serial.println(F("Команда вывода списка файлов"));
      root2 = SD.open("/");
      printDirectory(root2, 0);
      root2.close();
    }

    // Вторая команда (при числе команды "2")
    // Создает файл с заданным именем
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
          name = Serial.readString(); // Считываем имя нового файла
          name += ".txt"; // Добавляем тип файла
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
    }

    // Третья команда (при числе команды "3")
    // Удаляет файл с заданным именем
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
          name = Serial.readString(); // Считываем имя удаляемого файла
          name += ".txt"; // Добавляем тип файла
          Serial.println("Имя удаляемого файла: " + name);
          // Проверяем есть ли такой файл
          if (!SD.exists(name)) {
            Serial.println("Такого файла нет");
            break;
          } else {
            Serial.println("Файл с таким именем существует");
          }

          Serial.println("Удаление файла");
          if (SD.remove(name)) {
            Serial.println("Файл удален");
          } else {
            Serial.println("Файл не удален");
          }
          break;
        }
      }
    }

    if (COMMAND_NUMBER == 4) {
      //Serial.println("Команда диагностики карты");
      myFile = SD.open("test.txt");
      if (myFile) {
        Serial.println(F("test.txt:"));
        //int a = myFile.size();
        //Serial.println(F(a));
        Serial.print(F("Размер: "));
        Serial.println(myFile.size(), DEC);



        // read from the file until there's nothing else in it:
        while (myFile.available()) {
          Serial.write(myFile.read());

          //mySerial.write(myFile.read());
        }
        // close the file:
        myFile.close();
      } else {
        // if the file didn't open, print an error:
        Serial.println(F("error opening test.txt"));
      }
      //mySerial.print(382);
      // mySerial.print("A");
      //mySerial.println(3821);
    }

    if (COMMAND_NUMBER == 5) {
      //Serial.println("Команда диагностики карты");
      myFile = SD.open("test.txt");
      if (myFile) {
        Serial.println(F("test.txt:"));
        //int a = myFile.size();
        //Serial.println(F(a));
        Serial.print(F("Размер: "));
        Serial.println(myFile.size(), DEC);



        // read from the file until there's nothing else in it:
        while (myFile.available()) {
          //Serial.write(myFile.read());

          mySerial.write(myFile.read());
        }
        // close the file:
        myFile.close();
      } else {
        // if the file didn't open, print an error:
        Serial.println(F("error opening test.txt"));
      }
      //mySerial.print(382);
      // mySerial.print("A");
      //mySerial.println(3821);
    }

    if (COMMAND_NUMBER == 6) {
      mySerial.print("Hello");
    }
  }
}


void printDirectory(File dir, int numTabs) {
  while (true)
  {
    File entry =  dir.openNextFile();
    if (! entry)
    {
      // файлов больше нет
      break;
    }
    for (uint8_t i = 0; i < numTabs; i++)
    {
      Serial.print('\t');
    }
    Serial.print(entry.name());
    if (entry.isDirectory())
    {
      Serial.println("/");
      printDirectory(entry, numTabs + 1);
    }
    else
    {
      // у файлов есть размеры, у каталогов - нет
      Serial.print("\t\t");
      Serial.println(entry.size(), DEC);
    }
    entry.close();
  }
}

void listCommands() {
  Serial.println(F("Список команд:"));
  Serial.println(F("1 - Вывести список файлов"));
  Serial.println(F("2 - Создать файл"));
  Serial.println(F("3 - Удалить файл"));
  //Serial.println("Список команд:");
  //Serial.println("Список команд:");
  //Serial.println("Список команд:");
}

void initCard() {
  Serial.print(F("Initializing SD card..."));
  if (!SD.begin(4)) {
    Serial.println(F("initialization failed!"));
    while (1);
  }
  Serial.println(F("initialization done."));
}
