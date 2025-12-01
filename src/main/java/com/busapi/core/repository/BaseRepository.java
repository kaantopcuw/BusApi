package com.busapi.core.repository;

import com.busapi.core.entity.BaseEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@NoRepositoryBean // Spring'in bundan bir bean oluşturmasını engeller, sadece kalıtım için
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<@NonNull T, @NonNull UUID> {

    // Standart silme yerine soft delete yapan metot
    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.deletedAt = CURRENT_TIMESTAMP")
    void softDeleteAll();
}