package ch.alptbz.mqtttelegramdemo;

import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static ch.alptbz.mqtttelegramdemo.Main.config;

public class VentingAlarmService {
	Properties config;
	Timer timer = new Timer();
	TelegramNotificationBot bot;
	Mqtt mqttClient;

	public VentingAlarmService(Properties config, Mqtt mqttClient) throws MqttException {
		this.config = config;
		this.bot = new TelegramNotificationBot(this.config.getProperty("telegram-apikey"));
		this.mqttClient = mqttClient;

	}

	public void notifyVenting() {

		Set<Calendar> ventingTimes = new HashSet<>();

		Calendar ventingTime1 = Calendar.getInstance();
		ventingTime1.set(Calendar.HOUR_OF_DAY, 8);
		ventingTime1.set(Calendar.MINUTE, 43);
		ventingTime1.set(Calendar.SECOND, 0);

		Calendar ventingTime2 = Calendar.getInstance();
		ventingTime2.set(Calendar.HOUR_OF_DAY, 8);
		ventingTime2.set(Calendar.MINUTE, 44);
		ventingTime2.set(Calendar.SECOND, 0);


		Calendar ventingTime3 = Calendar.getInstance();
		ventingTime3.set(Calendar.HOUR_OF_DAY, 11);
		ventingTime3.set(Calendar.MINUTE, 31);
		ventingTime3.set(Calendar.SECOND, 0);


		Calendar ventingTime4 = Calendar.getInstance();
		ventingTime4.set(Calendar.HOUR_OF_DAY, 11);
		ventingTime4.set(Calendar.MINUTE, 30);
		ventingTime4.set(Calendar.SECOND, 0);


		ventingTimes.add(ventingTime1);
		ventingTimes.add(ventingTime2);
		ventingTimes.add(ventingTime3);
		ventingTimes.add(ventingTime4);


		for (Calendar time : ventingTimes) {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					String timeAsString = time.get(Calendar.DAY_OF_MONTH) + "-";
					timeAsString = timeAsString.concat(time.get(Calendar.MONTH) + "-");
					timeAsString = timeAsString.concat(time.get(Calendar.YEAR) + "-");
					timeAsString = timeAsString.concat(time.get(Calendar.HOUR_OF_DAY) + "-");
					timeAsString = timeAsString.concat(String.valueOf(time.get(Calendar.MINUTE)));

					bot.sendVentingNotificationToAllUsers();
					try {
						mqttClient.publish("rooms/venting/alarm", timeAsString);
					} catch (MqttException e) {
						e.printStackTrace();
					}

				}
			};
			Calendar now = Calendar.getInstance();
			long delay = time.getTimeInMillis() - now.getTimeInMillis();

			if (delay < 0) {
				time.add(Calendar.DAY_OF_MONTH, 1);
				delay = time.getTimeInMillis() - now.getTimeInMillis();
			}
			timer.scheduleAtFixedRate(task, delay, 24 * 60 * 60 * 1000);

		}
	}
}
