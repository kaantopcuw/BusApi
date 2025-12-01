# UUID Migration Guide

## Problem
Uygulama Long tipindeki ID'lerden UUID'ye geçiş yapıyor. Ancak PostgreSQL veritabanında mevcut veriler var ve bu veriler BIGINT (Long) tipinde ID'lere sahip.

## Hatalar
1. **IDENTITY column hatası**: PostgreSQL'de IDENTITY kolonları sadece integer tiplerinde olabilir, UUID olamaz
2. **Type casting hatası**: Mevcut BIGINT değerler UUID'ye otomatik cast edilemiyor

## Çözüm

### Seçenek 1: Veritabanını Sıfırdan Oluştur (Önerilen - Geliştirme Ortamı İçin)

Eğer veritabanındaki veriler önemli değilse (geliştirme ortamı):

```bash
# PostgreSQL'e bağlan
psql -U postgres

# Veritabanını sil ve yeniden oluştur
DROP DATABASE bus_db;
CREATE DATABASE bus_db;

# Uygulamayı başlat - Hibernate otomatik olarak tabloları UUID ile oluşturacak
./mvnw spring-boot:run
```

### Seçenek 2: Migration Script Kullan (Üretim Ortamı İçin)

Eğer veritabanındaki veriler önemliyse:

```bash
# 1. Veritabanını yedekle
pg_dump -U postgres bus_db > backup_before_uuid_migration.sql

# 2. Migration script'ini çalıştır
psql -U postgres -d bus_db -f src/main/resources/db/migration/V1__migrate_to_uuid.sql

# 3. Uygulamayı başlat
./mvnw spring-boot:run
```

### Seçenek 3: Flyway/Liquibase Kullan

Eğer Flyway veya Liquibase kullanmak isterseniz:

1. `pom.xml`'e Flyway dependency ekleyin:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

2. `application.properties`'e ekleyin:
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.jpa.hibernate.ddl-auto=validate
```

3. Migration dosyasını `src/main/resources/db/migration/` klasörüne koyun (zaten orada)

4. Uygulamayı başlatın - Flyway otomatik olarak migration'ı çalıştıracak

## Önemli Notlar

1. **Yedek Alın**: Migration öncesi mutlaka veritabanı yedeği alın
2. **Test Edin**: Önce test ortamında deneyin
3. **Downtime**: Migration sırasında uygulama çalışmıyor olmalı
4. **Geri Dönüş**: Bir sorun olursa yedeği geri yükleyin

## Migration Script'in Yaptıkları

1. Tüm foreign key constraint'leri kaldırır
2. Her tabloya yeni UUID kolonları ekler
3. Mevcut ID'leri UUID'ye map eder
4. Foreign key ilişkilerini günceller
5. Eski kolonları siler, yeni kolonları yeniden adlandırır
6. Primary key'leri yeniden oluşturur
7. Foreign key constraint'leri yeniden ekler

## Sorun Giderme

### Hata: "identity column type must be smallint, integer, or bigint"
**Çözüm**: BaseEntity'de `@GeneratedValue(strategy = GenerationType.IDENTITY)` yerine `@GeneratedValue(strategy = GenerationType.UUID)` kullanın (zaten yapıldı)

### Hata: "column cannot be cast automatically to type uuid"
**Çözüm**: Migration script'i kullanın veya veritabanını sıfırdan oluşturun

### Hata: Foreign key constraint violations
**Çözüm**: Migration script'i doğru sırayla çalıştırıldığından emin olun

## Geliştirme Ortamı İçin Hızlı Çözüm

```bash
# application.properties'i düzenle
spring.jpa.hibernate.ddl-auto=create-drop

# Uygulamayı başlat
./mvnw spring-boot:run

# Uygulama her başlatıldığında tabloları sıfırdan oluşturacak
```

**Uyarı**: Bu yöntem tüm verileri siler!

