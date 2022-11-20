// Счетчик привязанный к модулю времени
void sdCounter() {
  // При первом запуске, когда файла еще нет.
  // Существует ли файл
  // Если не существует, то создаем и записываем текущее время в секундах с Unix
  if (!SD.exists("pref.txt")) {
    myFile = SD.open("pref.txt", FILE_WRITE);  // Open
    String b(time.gettimeUnix());
    myFile.print(b);
    myFile.close();  // Close
  }

  // Если файл уже существует
  // То считать время из файла
  myFile = SD.open("pref.txt");  // Open
  String str = "";
  while (myFile.available()) {
    str.concat(myFile.readString());
  }
  myFile.close();  // Закрываем для чтения


  // Если прошел заданный промежуток времени
  if (time.gettimeUnix() - str.toInt() > 300) {
    mySerial.write("3021\n");
    writeDataToSD();                           // Записываем ТМИ
    SD.remove("pref.txt");                     // Удаляем
    myFile = SD.open("pref.txt", FILE_WRITE);  // Создаем новый
    String b(time.gettimeUnix());              // Считываем новое время
    myFile.print(b);                           // Записываем новое время
    myFile.close();                            // Закрываем
  }  
}