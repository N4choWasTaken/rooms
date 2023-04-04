package ch.alptbz.mqtttelegramdemo;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramNotificationBot
extends Thread implements UpdatesListener {

    private final TelegramBot bot;
    private final List<Long> users = Collections.synchronizedList(new ArrayList<Long>());

    public TelegramNotificationBot(String botToken) {
        bot = new TelegramBot(botToken);

        bot.setUpdatesListener(this);


    }

    public void sendTemperatureNotificationToAllUsers(double temperature) {
        for(Long user: users) {
            SendMessage reply = new SendMessage(user, "The temperature changed to %.2f °C".formatted(temperature));
            bot.execute(reply);
        }
    }

    @Override
    public int process(List<Update> updates) {
        for(Update update: updates) {
            if(update.message() == null) continue;
            String message = update.message().text();
            if(message == null) continue;
            if(message.startsWith("/help")) {
                SendMessage reply = new SendMessage(update.message().chat().id(), "Use /subscribe to subscribe to temperature updates. Use /unsubscribe to leave");
                bot.execute(reply);
            }
            if(message.startsWith("/subscribe")) {
                if(!users.contains(update.message().chat().id())) {
                    users.add(update.message().chat().id());
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "Welcome to Rooms! Use /unsubscribe to stop getting notifications.");
                    bot.execute(reply);
                }else{
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You are already subscribed the temperature notifications!");
                    bot.execute(reply);
                }
            }
            if(message.startsWith("/unsubscribe")) {
                if(users.contains(update.message().chat().id())) {
                    users.remove(update.message().chat().id());
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "Byebye!");
                    bot.execute(reply);
                }else{
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You cannot unsubscribe something you've never subscribed to.");
                    bot.execute(reply);
                }
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
