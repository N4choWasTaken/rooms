#include "M5Core2.h"
#include <WiFi.h>
#include <PubSubClient.h>
#include "Adafruit_Sensor.h"
#include <Adafruit_BMP280.h>
#include "M5_ENV.h"
#include <Adafruit_GFX.h>

SHT3X sht30;
QMP6988 qmp6988;

float tmp = 0.0;
float hum = 0.0;

WiFiClient espClient;
PubSubClient client(espClient);

// Configure the name and password of the connected wifi and your MQTT Serve host. 
const char* ssid = "Sunrise_2.4GHz_3FD930";
const char* password = "hmenD17cg4c5";
const char* mqtt_server = "cloud.tbz.ch";

unsigned long lastMsg = 0;
#define MSG_BUFFER_SIZE	(50)
char msg[MSG_BUFFER_SIZE];
int value = 0;

bool openWindows = false;  // Flag to indicate if the window is open or closed

void setupWifi();
void callback(char* topic, byte* payload, unsigned int length);
void reConnect();

void drawUI(bool isOpen) {
  M5.Lcd.clear();
  M5.Lcd.setTextSize(2);
  M5.Lcd.setCursor(10, 10);
  M5.Lcd.printf("Temperature: %.2f C", tmp);
  M5.Lcd.setCursor(10, 50);
  M5.Lcd.printf("Humidity: %.2f %%", hum);

  M5.Lcd.setTextSize(2);
  M5.Lcd.setCursor(10, 220);
  M5.Lcd.setTextColor(WHITE);
  M5.Lcd.printf("Window: ");
  if (isOpen) {
    M5.Lcd.setTextColor(GREEN);
    M5.Lcd.printf("Opened");
  } else {
    M5.Lcd.setTextColor(RED);
    M5.Lcd.printf("Closed");
  }

  M5.Lcd.drawRect(20, 120, 100, 50, WHITE);
  M5.Lcd.setTextSize(2);
  M5.Lcd.setCursor(40, 135);
  M5.Lcd.print("Button");

  if (M5.Touch.ispressed()) {
    Point touch = M5.Touch.getPressPoint();
    int x = touch.x;
    int y = touch.y;
    if (x >= 20 && x <= 120 && y >= 120 && y <= 170) {
      openWindows = !openWindows;  // Toggle the window flag
      drawUI(openWindows);
      if (openWindows) {
        client.publish("rooms/window", "opened");
      } else {
        client.publish("rooms/window", "closed");
      }
      delay(1000);
    }
  }

  delay(50);
}

void setup() {
  M5.begin();
  M5.Lcd.clear();

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

    drawUI(openWindows);
    
  } else {
    tmp = 0.0;
    hum = 0.0;
    drawUI(openWindows);
  }

    if (M5.BtnA.isPressed()) {
      openWindows = !openWindows;  // Toggle the window flag
      drawUI(openWindows);
      if (openWindows) {
        client.publish("rooms/window", "opened");
      } else {
        client.publish("rooms/window", "closed");
      }
      delay(1000);
    }

  unsigned long now = millis(); //Obtain the host startup duration. 
  if (now - lastMsg > 2000) {
    lastMsg = now;
    ++value;
    snprintf (msg, MSG_BUFFER_SIZE, "%.2f", tmp);
    client.publish("rooms/temp", msg);
    snprintf (msg, MSG_BUFFER_SIZE, "%.2f", hum);
    client.publish("rooms/hum", msg);
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
  M5.Lcd.clear();
}

void callback(char* topic, byte* payload, unsigned int length) {
  //M5.Lcd.print("Message arrived [");
  //M5.Lcd.print(topic);
  //M5.Lcd.print("] ");
  //for (int i = 0; i < length; i++) {
  //  M5.Lcd.print((char)payload[i]);
  //}
  //M5.Lcd.println();
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
      M5.Lcd.clear();
      // Once connected, publish an announcement to the topic. 
      client.publish("M5Stack", "hello world");
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