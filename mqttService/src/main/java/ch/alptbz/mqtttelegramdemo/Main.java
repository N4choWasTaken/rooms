package ch.alptbz.mqtttelegramdemo;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	public static double sensor1_temperature = 0;
	public static double sensor1_humidity = 0;
	public static double sensor2_temperature = 0;
	public static double sensor2_humidity = 0;
	private static Logger logger;
	static Properties config;

	private static boolean loadConfig() {
		config = new Properties();
		try {
			config.load(new FileReader("config.properties"));
			return true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading config file", e);
		}
		return false;
	}

	public static void main(String[] args) throws InterruptedException, MqttException {
		logger = Logger.getLogger("main");

		if (!loadConfig()) return;

		logger.info("Config file loaded");
		Mqtt mqttClient = new Mqtt(config.getProperty("mqtt-url"), "runner-12");

		VentingAlarmService ventingAlarmService = new VentingAlarmService(config, mqttClient);
		ventingAlarmService.notifyVenting();

		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		Logger.getGlobal().addHandler(ch);

		try {
			mqttClient.start();
			mqttClient.subscribe("rooms/#");
			mqttClient.publish("M5Stack", "test");
		} catch (MqttException e) {
			e.printStackTrace();
		}

		while (true) {
			mqttClient.addHandler((topic, mqttMessage) -> {
				if (topic.equals("rooms/sens1/temp")) {
					sensor1_temperature = Double.parseDouble(mqttMessage.toString());
				}
				if (topic.equals("rooms/sens1/hum")) {
					sensor1_humidity = Double.parseDouble(mqttMessage.toString());
				}
				if (topic.equals("rooms/sens2/temp")) {
					sensor2_temperature = Double.parseDouble(mqttMessage.toString());
				}
				if (topic.equals("rooms/sens2/hum")) {
					sensor2_humidity = Double.parseDouble(mqttMessage.toString());
				}
				if (topic.equals("rooms/windows")) {
					String message = mqttMessage.toString();
					if (message.equals("opened")) {

					} else if (message.equals("closed")) {
						ventingAlarmService.setLastVented(Calendar.getInstance());

					}
					Calendar lastVented = Calendar.getInstance();
					ventingAlarmService.setLastVented(lastVented);
					ventingAlarmService.notifyClosing();
				}
			});
			double avgTemperature = (sensor1_temperature + sensor2_temperature) / 2;
			double avgHumidity = (sensor1_humidity + sensor2_humidity) / 2;

			try {
				System.out.println(avgTemperature);
				mqttClient.publish("rooms/avg/temp", String.valueOf(avgTemperature).formatted("%.2f"));
				mqttClient.publish("rooms/avg/hum", String.valueOf(avgHumidity).formatted("%.2f"));
			} catch (MqttException e) {
				e.printStackTrace();
			}
			Thread.sleep(1000);
		}
	}
}
