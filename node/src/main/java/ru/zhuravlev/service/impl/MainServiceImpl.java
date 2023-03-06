package ru.zhuravlev.service.impl;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.zhuravlev.dao.AppUserDAO;
import ru.zhuravlev.dao.RawDataDAO;
import ru.zhuravlev.entity.AppDocument;
import ru.zhuravlev.entity.AppPhoto;
import ru.zhuravlev.entity.AppUser;
import ru.zhuravlev.entity.RawData;
import ru.zhuravlev.exceptions.UploadFileException;
import ru.zhuravlev.service.FileService;
import ru.zhuravlev.service.MainService;
import ru.zhuravlev.service.ProducerService;
import ru.zhuravlev.service.enums.LinkType;
import ru.zhuravlev.service.enums.ServiceCommands;

import static ru.zhuravlev.entity.enums.UserState.BASIC_STATE;
import static ru.zhuravlev.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.zhuravlev.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    String ugar = EmojiParser.parseToUnicode(":stuck_out_tongue_closed_eyes:");
    String eye = EmojiParser.parseToUnicode(":stuck_out_tongue_winking_eye:");
    String heart = EmojiParser.parseToUnicode(":heart:");
    String smile = EmojiParser.parseToUnicode(":wink:");
    String kiss = EmojiParser.parseToUnicode(":kissing_heart:");
    String crazy = EmojiParser.parseToUnicode(":crazy_face:");
    String heartEye = EmojiParser.parseToUnicode(":heart_eyes:");
    String clown = EmojiParser.parseToUnicode(":clown_face:");
    String kissingHeart = EmojiParser.parseToUnicode(":kissing_heart:");
    String explode = EmojiParser.parseToUnicode(":exploding_head:");
    String sweat = EmojiParser.parseToUnicode(":sweat_smile:");
    String astonished = EmojiParser.parseToUnicode(":astonished:");
    String whoa = EmojiParser.parseToUnicode(":flushed:");
    String yum = EmojiParser.parseToUnicode(":yum:");
    String nice = EmojiParser.parseToUnicode(":relieved:");
    String party = EmojiParser.parseToUnicode(":relaxed:");
    String zub = EmojiParser.parseToUnicode(":grin:");
    String scream = EmojiParser.parseToUnicode(":scream:");
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, FileService fileService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var textMessage = update.getMessage();
        var telegramUser = textMessage.getFrom();
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommands = ServiceCommands.fromValue(text);
        if (CANCEL.equals(serviceCommands)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
        } else {
            log.error("Непредвиденное действие пользователя" + userState);
            output = ("Ты ввел какое-то не то сообщение, ты меня удивляешь!)");
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);

    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        try {

            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            var answer = "Документ успешно загружен! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {

            log.error(ex);
            String error = "К сожалению, загрузка файла не удалась(((";
            sendAnswer(error, chatId);
        }
    }


    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте свою учетную запись для входа.";
            sendAnswer(error, chatId);
            return true;
        } else if (BASIC_STATE.equals(userState)) {
            var error = kissingHeart + " ";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            var answer = "Фото загружено! Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //TODO добавить регистрацию
            return "Временно недоступно...";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (JOKE.equals(cmd)) {
            return joke();
        } else if (ANSWER.equals(cmd)) {
            return "Умничка!" + kiss + " Скажу секрет, когда будем вместе)\n"
                    + "/else?";
        } else if (ELSE.equals(cmd)) {
            return "Ты можешь отправить сюда свою фотку, чтобы я раньше смог увидеть твои глазки" + eye;
            //+ sendImageUploadingAFile("resources/pic/demo.jpg", "@Sasha_GSXR");
        } else if (MEET.equals(cmd)) {
            return "Приглашаю тебя сегодня вечером на свидание" + ugar + "У нас заказан столик на 20:30 в одном прикольном месте. \n"
                    + "Я думаю, мы классно проведем вечер) Уверен, сейчас ты как всегда красива" + nice + ", а настроение я тебе обязательно, если что, подниму" + party + "\n"
                    + "Так что в 19:00 буду у тебя" + zub + "\n"
                    + "/place?";
        } else if (PLACE.equals(cmd)) {
            return "Первый в Москве андерграунд ресторан" + whoa + "Москва, Страстной бул., 4, стр. 3.";
//        } if (ANSWER.equals(cmd) == false) {
//            return "Хмммм, а ну-ка попробуй еще разок!D";
        } else if (START.equals(cmd)) {
            return "Привет!" + kissingHeart + "Я - виртуальный Саша, и я хочу сделать этот день немного особенным." + smile + "И по-особенному поздравить тебя с днём влюбленных сердечек" + scream + "\n"
                    + "Это - интерактивная " + heart + "валентинка" + heart + ", здесь есть пара прикольных вещей, которые я могу показать тебе.\n"
                    + "Чтобы посмотреть все, напиши мне \n"
                    + "/help, и я отвечу)";
        } else {
            return whoa + "Я пока еще не настолько феноменально умён" + astonished + "\n"
                    + "Давай еще раз попробуем." + yum + "\n"
                    + help();
        }
    }

    private String help() {
        return "Cписок доступных команд \n"
                + heart + "валентинки" + heart + ":\n"
                + "/date - " + heartEye + "14.02" + heartEye + "(щелкни, чтобы узнать, что мы будем сегодня делать);\n"
                + "/jopa - " + clown + "загадка" + clown;
//                + "/cancel - отмена выполнения текущей команды(добавил по-приколу)";
//                + "/registration - регистрация пользователя;\n"
        //TODO добавить несколько пунктов
    }

    private String joke() {
        return "Если отгадаешь одну из твоих фраз, которую я загадал, то расскажу тебе один свой секрет" + sweat
                + "только вводи внимательно" + astonished;
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена, бейби!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистраии
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

//    private SendPhoto sendImageUploadingAFile(String filePath, String chatId) {
//        // Create send method
//        SendPhoto sendPhotoRequest = new SendPhoto();
//        // Set destination chat id
//        sendPhotoRequest.setChatId(chatId);
//        // Set the photo file as a new photo (You can also use InputStream with a constructor overload)
//        sendPhotoRequest.setPhoto(new InputFile(new File("resources/pic/demo.jpg")));
//        try {
//            // Execute the method
//            sendPhoto(sendPhotoRequest);
//        } catch (UploadFileException e) {
//            e.printStackTrace();
//        }
//        return sendPhotoRequest;
//    }
}
