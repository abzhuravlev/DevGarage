package ru.zhuravlev.controller;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.zhuravlev.service.UpdateProducer;
import ru.zhuravlev.utils.MessageUtils;

import static ru.zhuravlev.model.RabbitQueue.*;

@Component
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils;
    private UpdateProducer updateProducer;
    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Receiver update is null");
            return;
        }
        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Receiver unsupported message type " + update);
        }
    }

    private void distributeMessageByType(Update update) {
    var message = update.getMessage();
    if (message.hasText()) {
        processTextMessage(update);
    } else if (message.hasDocument()) {
        processDocMessage(update);
    } else if (message.hasPhoto()) {
        processPhotoMessage(update);
    } else {
        setUnsupportedMessageTypeView(update);
    }
    }

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer ) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Неподдержимаемый тип сообщения!");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);

    }

    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Воу!) Вот это фотка, бейби!");
        setView(sendMessage);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }
}
