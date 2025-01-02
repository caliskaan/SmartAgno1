#include <DHT.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <time.h>
#include <ArduinoWebsockets.h>
using namespace websockets;

const char* ssid = "Muhammed";           // Wi-Fi SSID (ağ adı)
const char* password = "abcd1234567"; 
const char* apiURL = "https://bbvg6eglze.execute-api.eu-central-1.amazonaws.com/prod/post-sensor-data";
//Sensor verilerinin gönderileceği API endpointi
const char* wsServer = "ws://3.73.144.4:3001"; // WebSocket sunucu adresi
const char* ntpServer = "pool.ntp.org";//saat bilgisinin alınacağı ntp sunucu adresi
const long gmtOffset_sec = 10800; // GMT+3 için 3 saat (3*3600 saniye)
const int daylightOffset_sec = 0; // Yaz saati uygulanmadığı için 0

int mq135Pin = 34;//hava kalite sensör pini
int yl69Pin = 35;//toprak nem sensör pini
int soilThresholdMin=40;//Başlangıç eşik değerleri(daha sonra mobil uygulamadan değişiyor)
int soilThresholdMax=70;
int soilMoisturePercent;
int airThresholdMin=100;
int airThresholdMax=200;
float tempThresholdMin=24.00;
float tempThresholdMax=29.00;

int minYL69=50;//Toprak nemi için referans değerler
int maxYL69=4095;
int dataCount=0;//Ölçüm sayısı herbir döngüde artacak

int sampleTime=1000;//ölçüm süresi gecikmesi(mobilden değişiebiliyor)

int cmfSoil=0;//Gün sonunda veri tabanına gönderilecek ortalamaları tutan değişkenler(dataCount a bölünecek)
double cmfAir=0.0;
double cmfTemp=0.0;
double cmfHum=0.0;

String data="default";//Websocketten gelecek olan data değişkeni

float vOut;
float RS;
float ppm;
float ratio;

//Sensörlerin ve aktüatörlerin bağlı olduğu pinler
#define DHTPIN 12
#define FANPIN 26
#define WATERPIN 22
#define LEDPIN 18
#define LIGHTPIN 21
#define LDRPIN 14
#define DHTTYPE DHT11
//Sensörlerden gelen direnç değerlerini anlamlı hale dönüştürmek için referans değerler
#define RL 10.0  // Lojik yük direnci (kiloohm)
#define VCC 5.0
#define RO = 10.0

//uygulama başlangıç ve bitiş tarihlerini tutacak olan değişkenler(time_t türünde)
time_t startTime;
time_t endTime;


DHT dht(DHTPIN, DHTTYPE);//Dht11 sensör nesnesi
WebsocketsClient wsClient; // WebSocket istemci nesnesi

//wi-fi bağlantı metodu
void connectToWiFi() {
  // Bağlantı kurulana kadar dene
  WiFi.begin(ssid, password);
  Serial.println("Wi-Fi'ye bağlanılıyor...");

  // Bağlantı sağlanana kadar bekle
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);  // 1 saniye bekle
    Serial.print(".");
  }}

//Websocketten(telefon tarafından ) gelen mesajları yakalamak için callback fonksiyonu
void onMessageCallback(WebsocketsMessage message) {
  //Gelen Mesajda veriler virfüllerle ayrıldı.Daha sonrasında gelen veri parse edildi.
  /*
  Örnek mesaj="airThresholdMin,200,airThresholdMax,300";
  ilk gelen veri anahtar 2.gelen veri değer bu şekilde gelen anahtarlara göre ilgili değerler 
  ilgili değişkenlere aktarıld
  
  */
  Serial.println("Gelen mesaj: " + message.data());
  
  String msg = message.data();
  int index = 0;
  String key, value;

  while ((index = msg.indexOf(',')) != -1) {
    key = msg.substring(0, index);  // Anahtar kısmını al
    msg = msg.substring(index + 1); // Kalan kısmı

    // İkinci virgüle kadar olan değeri alalım
    index = msg.indexOf(',');
    if (index == -1) {
      value = msg; // Son değer
    } else {
      value = msg.substring(0, index); // Değer kısmı
      msg = msg.substring(index + 1);  // Kalan kısmı
    }
    
    // Anahtar ve değere göre değişkenlere atama yapalıyor
    //eişk değererinin yeni değerleri atanıyor
    if (key == "soilMinThreshold") {
      soilThresholdMin = value.toInt();
    } else if (key == "soilMaxThreshold") {
      soilThresholdMax = value.toInt();
    } else if (key == "airMinThreshold") {
      airThresholdMin = value.toInt();
      Serial.println("Yeni airmin:");
      Serial.print(airThresholdMin);
    } else if (key == "airMaxThreshold") {
      airThresholdMax = value.toInt();
      Serial.println("Yeni airmax:");
      Serial.print(airThresholdMax); // Buradaki yanlışlık giderildi
    } else if (key == "tempThresholdMax") {
      tempThresholdMax = value.toFloat();
    } else if (key == "tempThresholdMin") {
      tempThresholdMin = value.toFloat();
    }
      else if(key=="sampleTime"){
        //Ölçm aralığının yeni değeri
          sampleTime=value.toInt();
      }
    
    Serial.println("Anahtar: " + key + " Değer: " + value);
  }
}

//Websocket sunucusuna bağlanmak için gerekli metod
void connectToWebSocket() {
  if (!wsClient.connect(wsServer)) {
    Serial.println("WebSocket sunucusuna bağlanılamadı.");
    return;
  }
  Serial.println("WebSocket sunucusuna başarıyla bağlanıldı!");
  
  // WebSocket mesajlarını dinle
  wsClient.onMessage(onMessageCallback);
}
//Websocket sunucusuna mesaj gönderme metodu (sensörden okunan değerler gönderilecek)
void sendDataToWebSocket(String data) {
  if (wsClient.available()) {//bağlantı kontrolü
    wsClient.send(data);
    Serial.println("WebSocket üzerinden veri gönderildi: " + data);
  } else {
    Serial.println("WebSocket bağlantısı aktif değil!");
  }
}

bool isEndOfDay() {//gün sonu kontrolü yapan metod
/*eğer gün sonu ise kümülatif toplam değişkenleri dataCount a böölünerek verilerin ortalaması
veri tabanına gönderilecek
*/
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {//zaman kontrolü
    Serial.println("Zaman bilgisi alınamıyor!");
    return false;
  }

  return (timeinfo.tm_hour == 23 && timeinfo.tm_min == 59);
  //saat in 23.59 olması gerekiyor
}

void resetCumulativeData() {
  //veri tabanına gönderme işlemi tamamlandıktan sonra yeni gün için değerler sıfırlanıyor
  cmfAir = 0.0;
  cmfTemp = 0.0;
  cmfHum = 0.0;
  cmfSoil = 0;
}

//Veri tabanına ortalama değerleri gönderme metodu
void sendDataToAPI(double avgTemp, double avgHum, double avgSoil, double avgAir) {
  if (WiFi.status() == WL_CONNECTED) {
    //ağ bağlantı kontrolü
    HTTPClient http;//http istekleri için http nesnemiz
    http.begin(apiURL);//ilgili api endopointi ile nesne başlatılıyor
    http.addHeader("Content-Type", "application/json");//json şeklinde gönderilecek
    // JSON verisi oluşturma
    String jsonData = "{\"air_temperature\":" + String(avgTemp) +
                  ",\"air_humidity\":" + String(avgHum) +
                  ",\"soil_moisture\":" + String(avgSoil) +
                  ",\"air_quality\":" + String(avgAir) +
                  ",\"light_intensity\":"+String(LDRPIN)+"}";


    // POST isteği gönderme
    int httpResponseCode = http.POST(jsonData);
    if (httpResponseCode > 0) {
      Serial.println("Veri gönderildi: " + String(httpResponseCode));
      Serial.println("Sunucu yanıtı: " + http.getString());

    } else {
      Serial.println("API isteği başarısız: " + String(httpResponseCode));
    }
    http.end();
  } else {
    Serial.println("Wi-Fi bağlantısı yok!");
  }
}

/*
Program aynı zamanda aktüatör olaylarınıda veri tabanına gönderiyor
Örneğin sulama açıldığında ilgili tabloya "Sulama açıldı" şeklinde bir post işlemi yapılıypr
sendFailuretoAPI metodu bu işlemi yapıypr
*/
void sendFailureToAPI(String failure) {
  if (WiFi.status() == WL_CONNECTED) {//Ağ kontrolü
    HTTPClient http;
    http.begin("https://ncogc55knl.execute-api.eu-central-1.amazonaws.com/prod/post-failure");

    http.addHeader("Content-Type", "application/json");
    // JSON verisi oluşturma
    String jsonData = "{\"fail_type\":\"" + failure + "\"}";
    // POST isteği gönderme
    int httpResponseCode = http.POST(jsonData);
    if (httpResponseCode > 0) {
      Serial.println("Veri gönderildi: " + String(httpResponseCode));
      Serial.println("Sunucu yanıtı: " + http.getString());

    } else {
      Serial.println("API isteği başarısız: " + String(httpResponseCode));
    }
    http.end();
  } else {
    Serial.println("Wi-Fi bağlantısı yok!");
  }
}

void calculateAndSendAverages(int dataCount) {
  //gün sonunda ortalama değerleri hesaplayıp veri tabanına gönderen metod
  float avgTemp = cmfTemp / dataCount;
  float avgHum = cmfHum / dataCount;
  float avgSoil = cmfSoil / dataCount;
  float avgAir = cmfAir / dataCount;
  sendDataToAPI(avgTemp, avgHum, avgSoil, avgAir);
}

void setup() {
  connectToWiFi();
  Serial.begin(115200);
  dht.begin();//dht11 sensörünü başlatma
  //pin konfigrasyonlari
  pinMode(LDRPIN, INPUT_PULLUP);
  pinMode(WATERPIN, OUTPUT);
  pinMode(LIGHTPIN, OUTPUT);
  pinMode(FANPIN, OUTPUT);
  digitalWrite(WATERPIN, LOW);
  digitalWrite(LIGHTPIN,LOW);
  digitalWrite(FANPIN, LOW);
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

  connectToWebSocket();
  analogReadResolution(12);  //12-bit derinliğinde sensör okuması yapıyoruz.
  analogSetAttenuation(ADC_11db);

  struct tm timeinfo;//time nesnemiz

  if (getLocalTime(&timeinfo)) {
    startTime = mktime(&timeinfo);  // Başlangıç zamanını alıyoruz
    Serial.println("Başlangıç saati alındı.");
    Serial.println(&timeinfo, "Başlangıç Saati: %Y-%m-%d %H:%M:%S");
  } else {
    Serial.println("Başlangıç zamanı alınamıyor!");
  }
}
void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectToWiFi(); // Bağlantı kopmuşsa yeniden bağlan
  }
  if (!wsClient.available()) {
    connectToWebSocket(); 
  }
  wsClient.poll();
  // MQ135 sensöründen analog veri okuma
  int mq135Value = analogRead(mq135Pin);
  // YL69 toprak nem sensöründen analog veri okuma
  int yl69Value = analogRead(yl69Pin);
  float temperature = dht.readTemperature();  // Sıcaklık (Celsius)
  float humidity = dht.readHumidity();
  soilMoisturePercent = map(yl69Value, minYL69, maxYL69, 100, 0);//Toprak nem sensründen gelen veriyi anlamlı hale getiriyoruz
  //mq135 sensrü için değer dönüşümü
  vOut = (mq135Value / 4095.0) * VCC;  // Voltajı hesaplama
  RS = RL * ((VCC / vOut) - 1);
  ppm = 116.6020682 * pow(ratio, -2.769034857);  // CO2 için katsayılar
  ratio = RS / RO;
  //kümülatif değerler her döngüde artıyor
  cmfTemp=cmfTemp+temperature;
  cmfHum=cmfHum+humidity;
  cmfSoil=cmfSoil+soilMoisturePercent;
  cmfAir=cmfAir+ppm;

  //sensörlerin kontrolü eşik değerlere ulaşırsa gerekli işlemler yapılıyor
  if (digitalRead(LDRPIN)==HIGH && digitalRead(LIGHTPIN)==LOW) {
      digitalWrite(LIGHTPIN, HIGH);
      sendFailureToAPI("Aydınlatma Açıldı");
  }
  if (digitalRead(LDRPIN)==LOW && digitalRead(LIGHTPIN)==HIGH){
      digitalWrite(LIGHTPIN,LOW);
      sendFailureToAPI("Aydınlatma Kapatıldı");
    
  }
  if (soilMoisturePercent>=soilThresholdMax && digitalRead(WATERPIN)==HIGH) {
    digitalWrite(WATERPIN, LOW);
    sendFailureToAPI("Sulama Kapatıldı");
  }
  if (soilMoisturePercent <=soilThresholdMin  && digitalRead(WATERPIN)==LOW) {
    digitalWrite(WATERPIN, HIGH);
    sendFailureToAPI("Sulama Açıldı");
  }
  if (ppm < airThresholdMin && digitalRead(FANPIN)==HIGH) {
    digitalWrite(FANPIN, LOW);
    sendFailureToAPI("Havalandırma Kapatıldı");
  }
  if (ppm >=airThresholdMax && digitalRead(FANPIN)==LOW) {
    digitalWrite(FANPIN, HIGH);
    sendFailureToAPI("Havalandırma Açıldı");
  }
  dataCount++;//her döngüde ölçüm sayısı alınıyor
  
  if (isEndOfDay()){
      //gün sonu kontrolü 
      calculateAndSendAverages(dataCount);
      dataCount=0;
      delay(58000);//eğer gün sonu ise tekrar girmemesi için 58 saniye bekliyor
      resetCumulativeData();
  }
  if (String(ppm)=="inf"){
    /*Bazen sensörlerden okuma hatası alabiliyoruz bu da mobil tarafına sayısal değer gitmediği için
    sorun yaratıyor eğer sensörlerden okuma hatası alırsak varsayılan olarak -1 değeri atanıyor*/
    ppm=-1;
  }
  if(String(temperature)=="nan"){
    temperature=-1;
  }
  if(String(humidity)=="nan"){
    humidity=-1;
  }
  /*gönderilecek sensör verisini Data,100 etiketi ile etiketliyoruz.Bu sayede mobil uygulama
  gelen verinin sensör verisi olduğunu anlayıp progress bar da gösteriyor
  */

  data="Data,100,Temp,"+String(temperature)+",Hum,"+String(humidity)+",Soil,"+String(soilMoisturePercent)+",Air,"+String(ppm)+",sampleTime,"+String(sampleTime);
  sendDataToWebSocket(data);
  delay(sampleTime);  // 1 saniye bekle
}






