#include "M5Core2.h"
#include <WiFi.h>
#include <PubSubClient.h>
#include "Adafruit_Sensor.h"
#include <Adafruit_BMP280.h>
#include "M5_ENV.h"

SHT3X sht30;
QMP6988 qmp6988;


float tmp = 0.0;
float hum = 0.0;

WiFiClient espClient;
PubSubClient client(espClient);

// Declare the mac variable as a global variable
char macChar[18];

// Configure the name and password of the connected wifi and your MQTT Serve host. 
const char* ssid = "LERNKUBE";
const char* password = "l3rnk4b3";
const char* mqtt_server = "cloud.tbz.ch";

unsigned long lastMsg = 0;
#define MSG_BUFFER_SIZE	(50)
char msg[MSG_BUFFER_SIZE];
int value = 0;

void setupWifi();
void callback(char* topic, byte* payload, unsigned int length);
void reConnect();

void setup() {
  M5.Lcd.setTextFont(FONT2);
  M5.begin();
  setupWifi();
  client.setServer(mqtt_server, 1883); 
  client.setCallback(callback); 

  Wire.begin(); //Wire init, adding the I2C bus.
  qmp6988.init();

  
}

void loop() {
  if (!client.connected()) {
    reConnect();
  }
  client.loop();  //This function is called periodically to allow clients to process incoming messages and maintain connections to the server.

  if(sht30.get()==0){ //Obtain the data of shT30.
    tmp = sht30.cTemp;  //Store the temperature obtained from shT30.
    hum = sht30.humidity; //Store the humidity obtained from the SHT30. 
    
  }else{
    tmp=0,hum=0;
  }

  unsigned long now = millis(); //Obtain the host startup duration. 
  if (now - lastMsg > 2000) {
    lastMsg = now;
    ++value;
    snprintf (msg, MSG_BUFFER_SIZE, "%.2f", tmp);
    if(String(macChar)== "30:C6:F7:1F:22:D4") {
      client.publish("rooms/sens1/temp", msg);
      snprintf (msg, MSG_BUFFER_SIZE, "%.2f", hum);
      client.publish("rooms/sens1/hum", msg);
    } else {
      client.publish("rooms/sens2/temp", msg);
      snprintf (msg, MSG_BUFFER_SIZE, "%.2f", hum);
      client.publish("rooms/sens2/hum", msg);
    }
  }
}

void setupWifi() {
  delay(10);
  M5.Lcd.printf("Connecting to %s",ssid);
  WiFi.mode(WIFI_STA);  //Set the mode to WiFi station mode.
  WiFi.begin(ssid, password); //Start Wifi connection. 

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    M5.Lcd.print(".");
  }
  M5.Lcd.printf("\nSuccess\n");

  // Get the MAC address and print it on the display
  uint8_t mac[6];
  WiFi.macAddress(mac);
  sprintf(macChar, "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
  M5.Lcd.print("MAC address: ");
  for (int i = 0; i < 6; ++i) {
    M5.Lcd.printf("%02X", mac[i]);
    if (i < 5) {
      M5.Lcd.print(":");
    }
  }
  M5.Lcd.println();
}


void callback(char* topic, byte* payload, unsigned int length) {
  M5.Lcd.print("Message arrived [");
  M5.Lcd.print(topic);
  M5.Lcd.print("] ");
  for (int i = 0; i < length; i++) {
    M5.Lcd.print((char)payload[i]);
  }
  M5.Lcd.println();
}

void reConnect() {
  while (!client.connected()) {
    M5.Lcd.print("Attempting MQTT connection...");
    // Create a random client ID. 
    String clientId = "M5Stack-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect. 
    if (client.connect(clientId.c_str())) {
      M5.Lcd.printf("\nSuccess\n");
      // Once connected, publish an announcement to the topic. 
      client.publish("M5Stack", "Program started!");
      // ... and resubscribe. 
      client.subscribe("M5Stack");
    } else {
      M5.Lcd.print("failed, rc=");
      M5.Lcd.print(client.state());
      M5.Lcd.println("try again in 5 seconds");
      delay(5000);
    }
  }
}