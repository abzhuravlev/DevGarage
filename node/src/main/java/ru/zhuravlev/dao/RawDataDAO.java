package ru.zhuravlev.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zhuravlev.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {

}
