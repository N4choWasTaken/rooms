#  Example Java Gradle Application featuring Telegram and MQTT 

## Requirements
 - Java JDK 17

## Recommendations
 - Use IntelliJ IDEA

## Getting started
- Clone the repository
```bash
git clone https://github.com/alptbz/mqtttelegramdemojava
```
- Change into repository
```bash
cd mqtttelegramdemojava/
```
- Copy config example and edit accordingly
```bash
cp config.properties.example config.properties
```
- Open Folder with IntelliJ IDEA
- Run gradle
- Fix gradle errors (mostly wrong version, java 17 requires gradle 7.2)
- Go to `src/main/ch.alptbz.mqtttelegramdemo/Main`, right click file, select run

## Telegram Bot
You can create a new TelegramBot and acquire an API Token using BotFather. All information related to Telegram Bots can be found [here](https://core.telegram.org/bots#6-botfather).

## Links
- https://github.com/eclipse/paho.mqtt.java
- https://github.com/pengrad/java-telegram-bot-api