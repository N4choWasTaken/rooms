package ch.alptbz.mqtttelegramdemo;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static double temperature = 0;
    public static double humidity = 0;
    private static Logger logger;
    private static Properties config;

    private static boolean loadConfig() {
        config = new Properties();
        try {
            config.load(new FileReader("config.properties"));
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading config file",e);
        }
        return false;
    }

    public final static void main(String[] args) throws InterruptedException {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        Logger.getGlobal().addHandler(ch);

        logger = Logger.getLogger("main");

        if(!loadConfig()) return;

        logger.info("Config file loaded");

        TelegramNotificationBot tnb = new TelegramNotificationBot(config.getProperty("telegram-apikey"));

        logger.info("TelegramBot started");

        Mqtt mqttClient = new Mqtt(config.getProperty("mqtt-url"), "runner-12");
        try {
            mqttClient.start();
            mqttClient.subscribe("rooms/#");
            mqttClient.publish("M5Stack", "test");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttClient.addHandler(new BiConsumer<String, MqttMessage>() {
            @Override
            public void accept(String s, MqttMessage mqttMessage) {
                if(s.equals("rooms/temp")) {
                    temperature = Double.parseDouble(mqttMessage.toString());
                }
                if(s.equals("rooms/hum")) {
                    humidity = Double.parseDouble(mqttMessage.toString());
                }
            }
        });

        double lastTemperature = temperature;

        while(true) {
            if(Math.abs(lastTemperature - temperature) >= 1)  {
                System.out.println("Temperature changed. Current: %.2f".formatted(temperature));
                tnb.sendTemperatureNotificationToAllUsers(temperature);
                lastTemperature = temperature;
            }
            Thread.sleep(1000);
        }

    }

}
