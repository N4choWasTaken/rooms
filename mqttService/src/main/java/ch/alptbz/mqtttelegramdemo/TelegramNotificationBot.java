package ch.alptbz.mqtttelegramdemo;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TelegramNotificationBot
		extends Thread implements UpdatesListener {
	boolean silenced = false;
	private final TelegramBot bot;
	private final VentingAlarmService ventingAlarmService;
	private final List<Long> users = Collections.synchronizedList(new ArrayList<Long>());

	public TelegramNotificationBot(String botToken, VentingAlarmService ventingAlarmService) {
		bot = new TelegramBot(botToken);
		bot.setUpdatesListener(this);
		this.ventingAlarmService = ventingAlarmService;
	}

	Timer timer = new Timer();

	TimerTask disableSilencingAfterTenMinutes = new TimerTask() {
		@Override
		public void run() {
			silenced = false;
		}
	};

	public void sendVentingNotificationToRandomUser() {
		Random random = new Random();
			SendMessage reply = new SendMessage(users.get(random.nextInt(users.size())), "It's time for some fresh air.");
			bot.execute(reply);
	}

	public void sendVentingAlarmToRandomUser() {
		Random random = new Random();
		SendMessage reply = new SendMessage(users.get(random.nextInt(users.size())), "It's time for some fresh air! You haven't vented for 50 min! ");
		bot.execute(reply);
	}


	@Override
	public int process(List<Update> updates) {
		for (Update update : updates) {
			if (update.message() == null) continue;
			String message = update.message().text();
			if (message == null) continue;
			if (message.startsWith("/help")) {
				SendMessage reply = new SendMessage(update.message().chat()
						.id(), "Use /subscribe to subscribe to temperature updates. Use /unsubscribe to leave");
				bot.execute(reply);
			}
			if (message.startsWith("/subscribe")) {
				if (!users.contains(update.message().chat().id())) {
					users.add(update.message().chat().id());
					SendMessage reply = new SendMessage(update.message().chat().id(),
							"Welcome to Rooms! Use /unsubscribe to stop getting notifications.");
					bot.execute(reply);
				} else {
					SendMessage reply = new SendMessage(update.message().chat().id(),
							"You are already subscribed the temperature notifications!");
					bot.execute(reply);
				}
			}
			if (message.startsWith("/unsubscribe")) {
				if (users.contains(update.message().chat().id())) {
					users.remove(update.message().chat().id());
					SendMessage reply = new SendMessage(update.message().chat().id(),
							"Byebye!");
					bot.execute(reply);
				} else {
					SendMessage reply = new SendMessage(update.message().chat().id(),
							"You cannot unsubscribe something you've never subscribed to.");
					bot.execute(reply);
				}
			}
			if (message.startsWith("/silence")) {
				SendMessage reply = new SendMessage(update.message().chat().id(),
						" \uD83E\uDD2B Alarms silenced for 10 Minutes.");
				bot.execute(reply);
				silenced = true;
				timer.schedule(disableSilencingAfterTenMinutes, 10 * 60 * 1000);

			}
		}
		return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}
}
