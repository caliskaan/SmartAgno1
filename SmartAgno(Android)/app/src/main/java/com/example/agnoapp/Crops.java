package com.example.agnoapp;

/*
* Bu sınıf veri tabanından gelecek olan mahsül özelliklerini tutmaktadır.
* Buradaki her bir değer veri tabanı tablosundaki sütünlara karşılık gelir. Bu
* sayede veri tabanında tablo olarak tutulan verileri nesne haline getirmekteyiz.
*
* */
public class Crops {
   public String sensorEvent;
   public String date;
   public int eventId;
   public String soilMoisture;
   public String airTemp;
   public String airHumidity;
   public String co2;
   public Crops(String sensorEvent,String date){
       this.date=date;
       this.sensorEvent=sensorEvent;
   }

    public Crops(int eventId,String soilMoisture, String airTemp, String airHumidity, String co2, String date) {
        this.soilMoisture = soilMoisture;
        this.airTemp = airTemp;
        this.airHumidity = airHumidity;
        this.co2 = co2;
        this.eventId = eventId;
        this.date = date;
    }
}
