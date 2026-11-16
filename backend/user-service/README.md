# User Service

Kullanıcı profil verisini yöneten mikroservistir. **Kimlik doğrulama Keycloak** üzerinden yapılır; bu servis Kayıt sırasında Keycloak’ta hesap oluşturur ve **PostgreSQL** içinde iş kullanıcı kaydını (`keycloakId` ile eşleşen) tutar. Genel API cevapları **`common-lib`** içindeki `ApiResponse` / `PageResponse` modeline uyar; iş ve doğrulama hataları **`UserErrorCode`** (`BaseErrorCode`) ile `GlobalExceptionHandler` üzerinden döner.

---

## Bağımlılıklar ve yapılandırma

| Dış sistem | Kullanım |
|------------|-----------|
| **Config Server** | `spring.config.import` → `configserver:http://config-service:8888`; gerçek ayarlar `config-repo/user-service.yml`. |
| **Eureka** | Servis keşfi (`spring.application.name: user-service`). |
| **PostgreSQL** | Kalıcı kullanıcı tablosu; bağlantı bilgisi ortam değişkenleri ile (`POSTGRES_*`). |
| **Keycloak** | Admin API (`keycloak-admin-client`): kayıtta realm içinde kullanıcı oluşturma; `keycloak.*` yapılandırması Config’ten (`server-url`, `realm`, `client-id`, `client-secret`). |
| **common-lib** | `BaseEntity`, HTTP yanıt zarfları, istisna kalıbı; gateway ile uyumlu **`HeaderConstants`** (`X-User-Id`, `X-User-Email`). |

**Varsayılan port:** `8081` (`user-service.yml`).

---

## Veri modeli

- **`User`** entity — `common-lib` **`BaseEntity`** üzerinden UUID kimlik, oluşturma/güncelleme zaman damgası ve **yumuşak silme** (`delete()` / `restore()`).
- Alanlar: ad, soyad, benzersiz **e-posta**, telefon, **aktif** bayrağı, **Keycloak kullanıcı kimliği** (`keycloakId`).
- Silme işlemi fiziksel silme değil; kayıt “silinmiş” olarak işaretlenir.

---

## Keycloak ile kayıt akışı

1. İstemci **`POST /api/v1/users/register`** ile (`RegisterRequest`) istek atar.
2. **`KeycloakService`** Keycloak Admin API ile realm’de kullanıcı oluşturur (şifre dahil).
3. Başarılı yanıttan **Keycloak kullanıcı id** alınır; yerel **`User`** kaydı bu id ile kaydedilir.
4. E-posta çakışması (ör. 409) veya oluşturma hatası **`BusinessException`** + **`UserErrorCode`** ile üst katmana iletilir.

---

## HTTP API özeti

| Controller | Taban yol | Amaç |
|------------|-----------|------|
| **`UserController`** | `/api/v1/users` | Sayfalı liste, **`GET /me`** (isteğe **`X-User-Id`** başlığı — gateway JWT sonrası doldurulur), ID ile okuma, oluşturma/güncelleme/silme, **`POST /register`**. |
| **`AdminController`** | `/api/v1/admin/users` | Silinmiş kullanıcıyı okuma, **`PATCH .../restore`** ile geri yükleme. |
| **`InternalController`** | `/internal/users` | Servisler arası: e-posta veya ID ile kullanıcı sorgusu (diğer mikroservisler doğrudan URL ile çağırabilir). |

Gateway üzerinden gelen trafik için rota ve güvenlik kuralları **`api-gateway`** yapılandırmasında tanımlıdır (ör. kayıt uçları genelde kimlik doğrulamasız).

---

## Hata kodları

**`UserErrorCode`** (`implements BaseErrorCode`): kullanıcı bulunamadı, zaten silinmiş, e-posta zaten kullanılıyor, Keycloak oluşturma hatası vb. Mesaj anahtarları `messages.properties` ile çözülür.

---

## Teknoloji (özet)

Spring Boot Web, Data JPA, Validation, Eureka Client, Config Client, PostgreSQL sürücüsü, MapStruct, SpringDoc OpenAPI, Actuator, Keycloak Admin Client.

---

## Kaynak kod yapısı (özet)

```
user-service/
├── controller/     UserController, AdminController, InternalController
├── service/        UserService, KeycloakService (+ impl)
├── repository/
├── entity/, dto/, mapper/, validator/
├── config/         KeycloakConfig, SwaggerConfig
├── exception/      UserErrorCode
├── src/main/resources/application.yml   → Config Server bootstrap
└── Dockerfile
```

---

## İlgili dokümantasyon

- Ortak kütüphane ve istisna enum kalıbı: [`../common-lib/README.md`](../common-lib/README.md)
- Merkezi ayarlar: [`../config-service/README.md`](../config-service/README.md)
- Giriş ve güvenlik: [`../api-gateway/README.md`](../api-gateway/README.md)
