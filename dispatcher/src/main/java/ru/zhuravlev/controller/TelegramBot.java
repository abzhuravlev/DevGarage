package ru.zhuravlev.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    private UpdateController updateController;

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }
    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var originalMessage = update.getMessage();
        updateController.processUpdate(update);


//        var response = new SendMessage();
//        response.setChatId(originalMessage.getChatId());
//                response.setText("Привет от разработчика!)" +
//                "оставь тут свои: \n" +
//                        "Фамилия Имя:\n" +
//                        "Возраст:\n" +
//                        "Кем Работаешь:\n" +
//                        "Хобби, увлечения:\n" +
//                        "Семейное положение:\n" +
//                        "Три главных ваших качеств:\n" +
//                        "Три отрицательных ваших качеств:\n" +
//                        "Какой запрос ( если есть):\n" +
//                        "Зачем тебе это надо:");
//        sendAnswerMessage(response);

    }
    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
