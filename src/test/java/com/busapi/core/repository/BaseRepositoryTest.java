package com.busapi.core.repository;

import com.busapi.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Sadece JPA bileşenlerini yükler, çok hızlıdır
@Import(JpaConfig.class) // Auditing config'ini dahil et
class BaseRepositoryTest {

    @Autowired
    private DummyRepository dummyRepository;

    @Autowired
    private TestEntityManager entityManager; // DB'ye native sorgu atmak için

    @Test
    @DisplayName("Kaydedilen entity için createdAt otomatik dolmalı")
    void shouldSetCreatedAtAutomatically() {
        // Given
        DummyEntity entity = new DummyEntity();
        entity.setName("Test Item");

        // When
        DummyEntity savedEntity = dummyRepository.save(entity);

        // Then
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("softDelete metodu isDeleted=true yapmalı ve deletedAt doldurmalı")
    void shouldSoftDeleteEntity() {
        // Given
        DummyEntity entity = new DummyEntity();
        entity.setName("Silinecek Item");
        DummyEntity saved = dummyRepository.save(entity);
        Long id = saved.getId();

        // When
        dummyRepository.softDelete(id);

        // Entity Manager'ı temizle ki cache'den gelmesin, DB'den taze çeksin
        entityManager.clear();

        // Then - Normal repository ile ararsak bulamamalıyız (@SQLRestriction sayesinde)
        Optional<DummyEntity> deletedEntity = dummyRepository.findById(id);
        assertThat(deletedEntity).isEmpty();

        // Then - Native Query ile bakarsak veritabanında silindi olarak durmalı
        DummyEntity nativeResult = (DummyEntity) entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM dummy_entity WHERE id = ?1", DummyEntity.class)
                .setParameter(1, id)
                .getSingleResult();

        assertThat(nativeResult).isNotNull();
        assertThat(nativeResult.isDeleted()).isTrue();
        assertThat(nativeResult.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("findAll silinmiş kayıtları getirmemeli")
    void shouldNotFindDeletedEntities() {
        // Given
        DummyEntity active = new DummyEntity();
        active.setName("Aktif");
        dummyRepository.save(active);

        DummyEntity deleted = new DummyEntity();
        deleted.setName("Silik");
        DummyEntity savedDeleted = dummyRepository.save(deleted);
        dummyRepository.softDelete(savedDeleted.getId());

        entityManager.clear();

        // When
        List<DummyEntity> all = dummyRepository.findAll();

        // Then
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("Aktif");
    }

    // --- TEST İÇİN GEREKLİ DUMMY CLASSLAR ---




}
