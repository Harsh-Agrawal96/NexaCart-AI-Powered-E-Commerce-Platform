# N11 Bootcamp — Backend

Bu depo, e-ticaret senaryosuna yönelik **Spring Boot 3** tabanlı mikroservis mimarisini içerir. Uygulamalar **Java 21** ve **Spring Cloud 2024** ile çalışır; paylaşılan tipler ve sözleşmeler için **`common-lib`** modülü kullanılır.

## Genel çalışma stratejisi

- **Keşif (Eureka):** `discovery-service` servislerin kayıt ve keşfini yönetir. Diğer servisler (gateway hariç tutulabilen desenler) instance bilgisini buradan alır.
- **Yapılandırma (Config Server):** `config-service` merkezi ayarları sunar; `config-repo` altındaki profillere göre servisler ayağa kalkarken gerekli ortak/bağlantı bilgilerini buradan okur. **Fail-fast** ve **retry** ile config hazır olmadan servisin sessizce yanlış ayarla çalışması engellenir.
- **API girişi:** `api-gateway` dış dünyadan gelen isteklerin tek giriş noktasıdır; route’lar ve filtreler genelde config üzerinden yönetilir; arka planda Eureka ile ilgili servislere yönlendirme yapılır.
- **Servisler arası iletişim:** İhtiyaç duyan modüller **OpenFeign** ile senkron HTTP çağrısı yapar.
- **Olay tabanlı akış:** Sipariş, ödeme, ürün görüntüleme, sepet gibi akışlarda **RabbitMQ (AMQP)** ile mesaj yayınlanır; özellikle **log-service** bu mesajları tüketerek davranış / iz kayıtlarını işler. Yayın hataları ana iş akışını düşürmemeye çekilir (best-effort).

## Modüller

| Modül | Rol | Strateji / not |
|--------|-----|----------------|
| **common-lib** | Ortak DTO, exception, saga/event sabitleri, enum’lar | Diğer modüller bu artefact’a bağımlıdır; tekrarlanan sözleşmeyi tek yerde tutar. |
| **discovery-service** | Eureka sunucusu | Servis kaydı ve lookup; gateway ve istemci servisler registry’yi kullanır. |
| **config-service** | Spring Cloud Config | Native profil ile classpath `config-repo`; Eureka ile entegre instance kaydı. |
| **api-gateway** | HTTP gateway | Config Server’dan ayar import eder; tek porttan API erişimi ve routing. |
| **user-service** | Kullanıcı kimlik ve profil işlemleri | Genelde doğrudan istemci/gateway üzerinden çağrılır. |
| **product-service** | Ürün katalog ve görünürlük | Ürün ile ilgili okuma/yazma; kullanıcı davranışı için MQ ile olay yayını. |
| **cart-service** | Sepet | Ürün doğrulama için Feign; sepet güncellemelerinde log/experience için MQ olayları. |
| **order-service** | Sipariş yaşam döngüsü | Feign ile iletişim; sipariş/saga tarafında AMQP kullanımı. |
| **payment-service** | Ödeme işlemleri | Feign + AMQP; ödeme sonucu ve entegrasyon mesajları. |
| **comment-service** | Ürün yorumları / değerlendirme | Ürün doğrulama vb. için Feign. |
| **log-service** | Davranış ve log kayıtları | AMQP tüketimi ve kalıcılık (ör. JPA); analiz ve iz için merkezi dinleyici. |
| **ai-service** | Yapay zeka destekli özellikler | Feign ile diğer servislerle konuşabilir; iş kuralları serviste kalır. |

## Çalıştırma

Projeyi kökten derlemek için:

```bash
mvn clean install
```

Tek bir servisi çalıştırmak için ilgili modül dizininde:

```bash
mvn spring-boot:run
```

Gerçek ortamda sıra genelde şöyledir: **discovery-service** → **config-service** → **api-gateway** ve domain servisleri (Config ve Eureka’nın hazır olması gateway ve servislerin doğru ayarla ayağa kalkması için önemlidir).

---

*Bu dosya mimariye hızlı uyum sağlamanız için özet bilgi içerir; portlar ve profil dosyaları `config-service` ve ilgili `application.yml` kaynaklarında güncel kalır.*
