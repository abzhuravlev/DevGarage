package ru.zhuravlev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zhuravlev.entity.AppDocument;

public interface AppDocumentDAO extends JpaRepository<AppDocument, Long> {
}
