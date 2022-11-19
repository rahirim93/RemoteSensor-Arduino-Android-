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