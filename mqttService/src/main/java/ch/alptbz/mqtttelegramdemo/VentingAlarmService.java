package ch.alptbz.mqtttelegramdemo;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class VentingAlarmService {
	Properties config;
	Timer timer = new Timer();
	TelegramNotificationBot bot;
	Mqtt mqttClient;
	Calendar lastVented;

	public VentingAlarmService(Properties config, Mqtt mqttClient) {
		this.config = config;
		this.bot = new TelegramNotificationBot(this.config.getProperty("telegram-apikey"), this);
		this.mqttClient = mqttClient;
	}

	public void setLastVented(Calendar _lastVented) {
		this.lastVented = _lastVented;
		this.lastVented.add(Calendar.MINUTE, 50);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				String timeAsString = buildTimeStamp(lastVented);
				bot.sendVentingAlarmToRandomUser();
				try {
					mqttClient.publish("rooms/venting/alarm", timeAsString);
				} catch (MqttException e) {
					throw new RuntimeException(e);
				}
			}
		};
		timer.schedule(task, lastVented.getTime());
	}

	public void notifyVenting() {
		Set<Calendar> ventingTimes = getVentingTimes();

		for (Calendar time : ventingTimes) {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					String timeAsString = buildTimeStamp(time);
					bot.sendVentingNotificationToRandomUser();
					try {
						mqttClient.publish("rooms/venting", "notification");
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

	public void notifyClosing(){
		Calendar closingTime = lastVented;
		closingTime.add(Calendar.MINUTE,5);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				String timeAsString = buildTimeStamp(closingTime);
				try {
					mqttClient.publish("rooms/venting", "alarm");
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		};
		timer.schedule(task, closingTime.getTime());
	}

	@NotNull
	private static Set<Calendar> getVentingTimes() {
		Set<Calendar> ventingTimes = new HashSet<>();

		Calendar ventingTime1 = Calendar.getInstance();
		ventingTime1.set(Calendar.HOUR_OF_DAY, 13);
		ventingTime1.set(Calendar.MINUTE, 46);
		ventingTime1.set(Calendar.SECOND, 0);

		Calendar ventingTime2 = Calendar.getInstance();
		ventingTime2.set(Calendar.HOUR_OF_DAY, 13);
		ventingTime2.set(Calendar.MINUTE, 30);
		ventingTime2.set(Calendar.SECOND, 0);

		Calendar ventingTime3 = Calendar.getInstance();
		ventingTime3.set(Calendar.HOUR_OF_DAY, 13);
		ventingTime3.set(Calendar.MINUTE, 31);
		ventingTime3.set(Calendar.SECOND, 0);

		Calendar ventingTime4 = Calendar.getInstance();
		ventingTime4.set(Calendar.HOUR_OF_DAY, 13);
		ventingTime4.set(Calendar.MINUTE, 32);
		ventingTime4.set(Calendar.SECOND, 0);

		ventingTimes.add(ventingTime1);
		ventingTimes.add(ventingTime2);
		ventingTimes.add(ventingTime3);
		ventingTimes.add(ventingTime4);
		return ventingTimes;
	}

	@NotNull
	private String buildTimeStamp(Calendar time) {
		String timeAsString = time.get(Calendar.DAY_OF_MONTH) + "-";
		timeAsString = timeAsString.concat(time.get(Calendar.MONTH) + "-");
		timeAsString = timeAsString.concat(time.get(Calendar.YEAR) + "-");
		timeAsString = timeAsString.concat(time.get(Calendar.HOUR_OF_DAY) + "-");
		timeAsString = timeAsString.concat(String.valueOf(time.get(Calendar.MINUTE)));
		return timeAsString;
	}
}
