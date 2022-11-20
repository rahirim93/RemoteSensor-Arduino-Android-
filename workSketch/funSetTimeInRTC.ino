boolean funSetTimeInRTCNew() {
  // Очистка порта
  //while (mySerial.available()) mySerial.read();
  if (mySerial.available() > 0) {
    //Serial.print("Время с телефона: ");
    //Serial.println(b);
    //Serial.print("Время на ардуино: ");
    //Serial.println(time.gettimeUnix());
    time.settimeUnix(mySerial.readString().toInt());
    //Serial.print("Время на ардуино после настройки: ");
    //Serial.println(time.gettimeUnix());
    return true;
  } else return false;
}