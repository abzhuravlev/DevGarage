package ru.zhuravlev.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.zhuravlev.entity.AppPhoto;
import ru.zhuravlev.entity.AppDocument;
import ru.zhuravlev.service.enums.LinkType;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
