# Bus Services API - Frontend Entegrasyon Dökümanı

Bu döküman, Otobüs Bilet Satış ve Yönetim Sistemi'nin backend API uçlarını (endpoints), veri yapılarını ve iş akışlarını açıklar.

**Base URL:** `http://localhost:8080/api/v1`
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 1. Genel Standartlar

### 1.1. API Yanıt Formatı (Wrapper)
Tüm endpointler (başarılı veya hatalı) aşağıdaki standart `ApiResponse` yapısını döner. Frontend state yönetimi ve hata yakalama mekanizması bu yapıya göre kurulmalıdır.

```json
{
  "success": true,             // İşlem teknik olarak başarılı mı?
  "message": "İşlem başarılı.", // Kullanıcıya gösterilecek bilgilendirme mesajı (Toast message)
  "data": { ... },             // Asıl veri (Object veya Array olabilir)
  "validationErrors": null,    // 400 Hatalarında alan bazlı hatalar: { "email": "boş olamaz" }
  "timestamp": 1701234567890   // Sunucu zamanı (Epoch)
}
```

### 1.2. Hata Yönetimi (HTTP Status)

- **200 OK**: İstek başarıyla işlendi. `success: true` kontrolü yapılmalı.
- **400 Bad Request**: Validasyon hatası. `validationErrors` objesi içindeki key-value değerleri form inputlarının altında gösterilmeli.
- **401 Unauthorized**: Token yok veya süresi dolmuş. Kullanıcıyı Login sayfasına yönlendirin.
- **403 Forbidden**: Yetkisiz erişim. (Örn: Müşteri rolüyle Admin paneline istek atılması).
- **404 Not Found**: İstenen kaynak (Sefer, Kullanıcı, Şehir) bulunamadı.
- **500 Internal Server Error**: Sunucu tarafında beklenmeyen hata. Genel bir hata mesajı gösterin.

### 1.3. Authentication (JWT)

Auth (Login/Register) ve Public (Sefer Arama, Şehir Listesi) modülleri hariç tüm isteklerde Header'da token gönderilmelidir.

- **Header Key**: `Authorization`
- **Header Value**: `Bearer <JWT_TOKEN>`

---

## 2. Enums (Sabit Değerler)

Frontend dropdown, radio button ve logic kontrolleri için bu değerleri kullanmalıdır.

| Enum Tipi | Değerler | Açıklama |
|-----------|----------|----------|
| **Gender** | MALE, FEMALE | Yolcu cinsiyeti. |
| **BusType** | STANDARD_2_2, SUITE_2_1 | Otobüs tipi. Koltuk dizilimi (SVG/Grid) buna göre çizilmeli. |
| **PaymentType** | CREDIT_CARD, CASH, AGENCY_BALANCE | Ödeme Yöntemi. CASH ve AGENCY_BALANCE sadece Acenta/Admin içindir. |
| **UserRole** | ROLE_ADMIN, ROLE_CUSTOMER, ROLE_DRIVER, ROLE_HOST, ROLE_AGENCY_STAFF | Kullanıcı yetkileri. Menü erişimi buna göre düzenlenmeli. |
| **TicketStatus** | SOLD, RESERVED, CANCELLED | Bilet durumu. |

---

## 3. Modül Bazlı Endpointler & İş Akışları

### A. Kimlik Doğrulama (Auth)

Kullanıcıların sisteme giriş yapması veya kaydolması.

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/auth/login` | Giriş yap. Dönen token, userId ve role local storage'da saklanmalı. |
| POST | `/auth/register` | Yeni müşteri kaydı. Başarılı olursa otomatik login akışına yönlendirilebilir. |

### B. Lokasyon (Location - Public)

Anasayfa arama kutusundaki "Nereden - Nereye" dropdownlarını doldurmak için.

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/locations/cities` | Tüm şehirleri listeler (id, name, plateCode). |
| GET | `/locations/cities/{id}/districts` | Seçilen şehre ait ilçeleri getirir. |

### C. Sefer Arama & Listeleme (Voyage - Public)

Müşterinin sefer aradığı liste ekranı.

**Endpoint:** `GET /voyages/trips/search`

**Query Parameters:**
- `date`: 2025-12-01 (ISO-8601 Formatında YYYY-MM-DD)
- `fromId`: Kalkış İlçe ID (District ID)
- `toId`: Varış İlçe ID (District ID)

**Response (Örnek):**

```json
[
  {
    "id": 10,
    "routeName": "İstanbul - Ankara",
    "departureTime": "14:00",
    "date": "2025-12-01",
    "busPlateNumber": "34 BUS 34",
    "price": 500.00,
    "status": "SCHEDULED"
  }
]
```

### D. Satış & Koltuk Seçimi (Sales - Order)

Projenin en kritik akışı burasıdır. "Sefer Seç -> Koltuk Seç -> Ödeme Yap" adımlarını içerir.

#### Adım 1: Koltuk Durumunu Çek

Kullanıcı seferi seçtiğinde otobüs şemasını çizmek için kullanılır.

**Endpoint:** `GET /sales/trip/{tripId}/seats`

**Response:**

```json
[
  { "seatNumber": 1, "occupied": true, "occupantGender": "MALE" }, // Dolu, Erkek (Yanına kadın oturamaz)
  { "seatNumber": 2, "occupied": false, "occupantGender": null }   // Boş
]
```

**Frontend Logic:** Eğer otobüs tipi STANDARD_2_2 ise ve yandaki koltuk doluysa, kullanıcının seçtiği cinsiyet ile yan koltuktaki `occupantGender` aynı olmalıdır. Aksi takdirde seçim engellenmeli veya uyarı verilmelidir.

#### Adım 2: Satın Al (Sipariş Oluştur)

Seçilen koltuklar ve girilen ödeme bilgileri tek bir istekte gönderilir.

**Endpoint:** `POST /sales/orders`

**Request Body:**

```json
{
  "tripId": 10,
  "contactEmail": "ali@mail.com",
  "contactPhone": "5551234567",
  "paymentInfo": {
    "paymentType": "CREDIT_CARD", // veya CASH (Sadece yetkili ise)
    "transactionId": "iyzico-token-123" // Sanal Pos'tan dönen ID
  },
  "billingAddress": { // Fatura bilgisi (Opsiyonel olabilir)
    "city": "İstanbul",
    "fullAddress": "X Mah. Y Sok.",
    "taxOffice": "Mecidiyeköy", 
    "taxNumber": "11111111111"
  },
  "tickets": [ // Sepetteki her yolcu için bir obje
    {
      "seatNumber": 2,
      "passengerName": "Ayşe",
      "passengerSurname": "Yılmaz",
      "passengerTc": "22222222222",
      "passengerGender": "FEMALE"
    }
  ]
}
```

**Başarılı Response:**

Dönen `orderPnr` değeri kullanıcıya "Teşekkürler" sayfasında gösterilmelidir.

```json
{
  "orderPnr": "PNR123456",
  "totalPrice": "500.00",
  "contactEmail": "ali@mail.com",
  "tickets": [...]
}
```

### E. Kullanıcı Paneli (Profile)

Login olmuş kullanıcının göreceği veriler.

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/users/{id}/history` | Kullanıcının Puan Bakiyesi ve Geçmiş Biletleri. |
| GET | `/users/{id}` | Kullanıcı profil bilgileri. |

### F. Yönetim Paneli (Admin / Agency Backoffice)

Sadece yetkili personelin (ROLE_ADMIN, ROLE_AGENCY_MANAGER vb.) kullanacağı ekranlar.

#### 1. Acenta Yönetimi

- `POST /agencies`: Yeni acenta oluştur (Sadece Admin).
- `GET /agencies`: Tüm acentaları listele. (Kullanıcı oluştururken personeli acentaya bağlamak için dropdown'da kullanılır).

#### 2. Filo & Sefer Operasyonları

- `POST /fleet/buses`: Yeni otobüs tanımla (Plaka, Tip, Kapasite).
- `GET /fleet/buses`: Otobüsleri listele.
- `POST /voyages/routes`: Yeni güzergah tanımla. (Duraklar ve KM bilgisi ile).
- `POST /voyages/definitions`: Sefer şablonu oluştur (Örn: Her gün 10:00).
- `POST /voyages/trips/generate?date=2025-12-01`: Şablona göre o günün seferlerini otomatik oluştur.

#### 3. Atama İşlemleri (Operasyonel)

Bir sefer oluşturulduğunda otobüsü ve personeli boştur. Sefer yaklaşınca bunlar atanır.

- `PUT /voyages/trips/{tripId}/assign-bus/{busId}`: Sefere fiziksel otobüs ata.
- `PUT /voyages/trips/{tripId}/assign-crew`: Sefere Şoför ve Muavin ata.
    - **Body:** `{ "driverId": 100, "hostId": 101 }`

#### 4. Manifesto (Yolcu Listesi)

Şoför, Muavin veya Acenta çalışanının yolcu kontrolü yapması için detaylı liste.

- `GET /voyages/trips/{tripId}/manifest`
    - **Response:** Yolcu Adı, TC, Telefon, Koltuk No, İneceği Yer bilgilerini içerir.

#### 5. Dashboard (Raporlama)

Yönetici ana ekranı.

- `GET /reports/dashboard`: Toplam Ciro, Toplam Gider, Net Kar ve Toplam Bilet Sayısı (Aylık özet).
- `POST /reports/expenses`: Sisteme gider fişi (Yakıt, Bakım, Personel gideri) girilmesi.

---

## 4. Frontend Geliştirici Notları

### Validasyon

Backend `@Valid` anotasyonları ile veriyi korur ancak kullanıcı deneyimi (UX) için Frontend tarafında da validasyon yapılmalıdır.

- **Email**: Format kontrolü.
- **TC No**: 11 hane ve sayısal kontrolü.
- **Zorunlu Alanlar**: Boş geçilemez kontrolleri.

### Cinsiyet Kuralı (UX)

Backend'de "Bayan yanı Bay" kontrolü vardır ve hata fırlatır. Ancak kullanıcı koltuk seçerken anlık olarak (yan koltuk doluysa ve cinsiyeti farklıysa) o koltuğu seçilemez (disabled) yapmak veya uyarı göstermek en iyi deneyimdir. Bunun için `/seats` endpoint'inden gelen `occupantGender` bilgisini kullanın.

### Tarih Formatı

API `yyyy-MM-dd` (Örn: 2025-12-01) formatını bekler. Datepicker component'inin çıktısını buna göre formatlayın.

### Role Based Access (RBAC)

JWT içindeki role bilgisine göre menüleri gizleyip/gösterin.

- **Admin**: Her yeri görür.
- **Customer**: Sadece Arama ve Profil.
- **Agency**: Satış, Manifesto ve Acenta raporları.