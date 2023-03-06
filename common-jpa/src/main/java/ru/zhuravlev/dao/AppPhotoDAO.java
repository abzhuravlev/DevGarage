package ru.zhuravlev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zhuravlev.entity.AppPhoto;

public interface AppPhotoDAO extends JpaRepository<AppPhoto, Long> {
}
