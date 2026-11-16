# Common Lib

Çalışan bir mikroservis değil; diğer modüllerin **`pom.xml`** içinde bağımlılık olarak eklediği **ortak Java kütüphanesidir**. Gateway ile iş servisleri arasında **aynı HTTP başlık adları**, RabbitMQ’da **aynı kuyruk/exchange isimleri**, API’de **aynı cevap gövdesi ve hata modeli** kullanılsın diye paylaşılan sınıflar burada toplanır. Böylece sabitler tek yerde kalır, tip uyumu bozulmaz.

---

## Projede yeri

| Rol | Açıklama |
|-----|-----------|
| Maven artefaktı | `com.n11bootcamp:common-lib` — parent POM’da `dependencyManagement` ile sürüm sabitlenir; servisler doğrudan ekler. |
| CI / derleme | Çok modülü build sırasında genelde **önce** `common-lib` kurulur (`mvn -pl common-lib install`), sonra diğer modüller derlenir. |
| Konteyner | Kendi Dockerfile’ı yok; JAR olarak diğer servislerin classpath’ine gömülür. |

---

## Paket yapısı (özet)

| Paket | İçerik |
|-------|--------|
| `constant` | **`HeaderConstants`** — `X-User-Id`, `X-User-Email`, `X-Trace-Id`, yetkilendirme önekleri vb. Gateway ile servisler aynı başlık sözleşmesini kullanır.<br>**`SagaConstants`** — RabbitMQ exchange, kuyruk ve routing key sabitleri (sipariş/stok/ödeme akışı ve kullanıcı davranışı olayları). |
| `enums` | Ortak enum’lar: örneğin sipariş durumu, ödeme sağlayıcı tipi, kullanıcı davranış tipi. |
| `event` | Saga ve davranış için **serileştirilebilir olay** sınıfları (`OrderCreatedEvent`, ödeme/stok olayları, `UserBehaviorEvent`, yük taşıyıcıları). Servisler arası mesaj gövdesi uyumu için paylaşılır. |
| `response` | **`ApiResponse`** standart API zarfı; **`PageResponse`** sayfalı liste için ortak model. |
| `exception` | Ortak hata modeli, istisna hiyerarşisi ve **tek** `@RestControllerAdvice` — ayrıntı için aşağıdaki bölüm. |
| `base` | **`BaseEntity`** — UUID birincil anahtar, oluşturma/güncelleme zamanı, yumuşak silme alanları (`@MappedSuperclass`). |
| `config` | **`MessageConfig`** — `messages.properties` ile çok dilli / merkezi mesaj kaynağı için `MessageSource` bean’i. |

---

## İstisna işleme ve hata enum kalıbı

### Tek merkezi `GlobalExceptionHandler`

Projede **`@RestControllerAdvice`** ile HTTP cevabı üreten ana işleyici **`common-lib` içindedir** (`GlobalExceptionHandler`). **Spring MVC** kullanan iş mikroservislerinde (`@RestController`) bu sınıf classpath’te olduğu sürece ayrı `ControllerAdvice` yazılmadan **aynı JSON gövde şekli** (`ApiResponse` + **`ProblemDetail`**) kullanılır.

**Not:** **API Gateway** WebFlux tabanlıdır; bu işleyici MVC istek döngüsü içindir. Gateway tarafındaki hatalar ve gateway’in `common-lib` kullanımı (ör. başlık sabitleri) bu modelden farklı çalışabilir.

İşleyicinin ele aldığı başlıca durumlar:

| Tür | Örnek | Sonuç özeti |
|-----|--------|----------------|
| **`CoreException`** (ve alt sınıfları) | İş kuralı veya kaynak bulunamadı | Enum’dan gelen HTTP durumu, `ProblemDetail` içinde `code`, başlık ve açıklama (`MessageSource` ile mesaj anahtarı çözülür). |
| **`MethodArgumentNotValidException`** | Bean Validation (`@Valid`) | 400, alan bazlı hata haritası, sabit kod örneği `VALIDATION-400`. |
| **`MethodArgumentTypeMismatchException`** | Yanlış parametre tipi | 400. |
| **`HttpMessageNotReadableException`** | Bozuk JSON gövdesi | 400. |
| **`Exception`** (geri kalanı) | Beklenmeyen hata | 500, loglanır, kullanıcıya genel mesaj. |

### Enum tabanlı hata kodu (`BaseErrorCode`)

İş ve genel hatalar **string sabitleri dağınık tutulmak yerine** ortak bir arayüz üzerinden modellenir:

**`BaseErrorCode`** arayüzü şunları taşır:

- **`code`** — API’ye gömülen makine-dostu kod (ör. `GEN-404`, servise özel önekler).
- **`messageKey`** / **`titleKey`** — `messages.properties` içindeki anahtarlar; çeviri ve parametreli mesaj için kullanılır.
- **`httpStatus`** — Bu hata için uygun HTTP durumu.

**`GeneralErrorCode`** enum’u bu arayüzü uygular ve **tüm servislerde paylaşılan genel** durumları tanımlar (örneğin doğrulama özeti, beklenmeyen hata, genel “bulunamadı”).

Her mikroservis kendi iş alanına özel durumları **ayrı bir enum** ile tanımlar ve yine **`BaseErrorCode`** uygular; örnekler: `ProductErrorCode`, `OrderErrorCode`, `CartErrorCode`, `UserErrorCode`, `CommentErrorCode` vb. Böylece:

- Hata **kodu**, **HTTP durumu** ve **mesaj anahtarı** tek yerde (enum sabiti) toplanır.
- Yeni bir iş kuralı hatası eklerken çoğunlukla **yeni bir enum sabiti** ve **properties satırı** yeterli olur.

### İstisna hiyerarşisi

- **`CoreException`** — `BaseErrorCode` ve isteğe bağlı **`MessageSource` argümanları** (`Object... args`) taşır; mesajlar `resolveMessage(key, args)` ile biçimlenir.
- **`BusinessException`**, **`ResourceNotFoundException`** — `CoreException` uzantıları; anlam farkı kodda okunabilirlik içindir, işleyici hepsi için **`CoreException`** yakalayıcısını kullanır.

Akış: Servis katmanı `throw new BusinessException(SomeErrorCode.XXX, arg1, arg2)` → `GlobalExceptionHandler` enum’dan HTTP durumunu ve kodunu okuyup **`ApiResponse.error(ProblemDetail)`** döner.

---

## Otomatik yapılandırma

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` dosyasında **`GlobalExceptionHandler`** listelenir; `common-lib` classpath’te olan Spring Boot uygulamalarında bu sınıf yüklenir ve ortak hata cevabı formatı devreye girer.

---

## Kaynak dosyalar

- **`messages.properties`** — `GlobalExceptionHandler` ve hata kodları için mesaj anahtarları (UTF-8).

---

## Neden ayrı modül?

- **DRY:** Aynı sabiti her serviste kopyalamak hem hata hem sürüm uyumsuzluğu riski yaratır.  
- **Sözleşme:** Gateway’in eklediği başlık adı ile `user-service`’in okuduğu adın birebir eşleşmesi gerekir.  
- **CI / Jib tartışması:** Çok modülü yapı ve bu kütüphanenin tüm servislere zincirleme bağımlılığı, görüntü üretiminde **Dockerfile ile çok aşamalı Maven** tercihinin gerekçelerinden biridir (bkz. `api-gateway/README.md`).

---

## İlgili dokümantasyon

- HTTP başlıklarının gateway tarafı: [`../api-gateway/README.md`](../api-gateway/README.md)
