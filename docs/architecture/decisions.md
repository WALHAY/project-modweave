# Архитектурные решения WebLab №3

## ADR-001: границы API, домена и хранения

Принято: три Gradle-модуля `api`, `domain`, `persistence`. API и persistence зависят от домена. Домен хранит интерфейсы Repository; адаптеры JPA преобразуют сущности БД в доменные модели, контроллеры преобразуют доменные модели в DTO. Оба API используют общие сервисы.

Альтернативы: передавать JPA-сущности прямо в HTTP или объединить всё в один модуль. Они короче, но нарушают требование №3.4 и связывают контракт с деталями БД. Полное удаление зависимостей домена от Spring потребовало бы переноса транзакций, кэша и security в отдельные обёртки; для этой лабораторной это не требуется. Граница моделей и Repository остаётся явной.

## ADR-002: Spring HATEOAS с сохранением OpenAPI

Принято: `ApiResourceAssembler` реализует `RepresentationModelAssembler<ApiResource, ApiModel>`. DTO содержат только данные; assembler добавляет связи с учётом текущего пользователя, включая вложенные файлы и элементы. `ApiModel` наследует стандартный `EntityModel`, который разворачивает DTO в JSON, и адаптирует ссылки к обязательным полям `{href, method}` из OpenAPI. Одна функция `apiLink(Controller::method, ...)` строит ссылки через `WebMvcLinkBuilder.linkTo(Method, ...)`, получает HTTP-метод из `RequestMapping` и учитывает общий префикс `/api/v2`. Она используется также для Location и файлового Link. Общий `ResponseBodyAdvice` выбирает assembler только в v2 и вычисляет ETag; это позволяет не дублировать контроллеры чтения ради v1/v2. Пагинация сохраняет исходные query-параметры и плоские поля контракта.

Перед реализацией прочитан [официальный справочник Spring HATEOAS](https://docs.spring.io/spring-hateoas/docs/current/reference/html/), разделы 2.1–2.4 и 3.1: `Link`, `EntityModel`, `CollectionModel`, `WebMvcLinkBuilder`, а также раздел об assemblers. Работа с реальными зависимостями подтверждена контрактными тестами.

Альтернативы: стандартный HAL, URI-шаблоны строками и `methodOn`. HAL меняет форму ссылок, коллекций и пагинации без потребности клиентов; небольшой адаптер EntityModel позволяет использовать библиотеку и сохранить контракт. Строковые URI-шаблоны дублируют маршруты контроллеров; от них отказались в пользу ссылок на методы. `methodOn` требует проксируемого возвращаемого типа и падает на финальных Kotlin data class; документированный `linkTo(Method, ...)` этого ограничения не имеет. При этом библиотека не учитывает `addPathPrefix` из MVC-конфигурации, поэтому адаптер добавляет его централизованно. Подключена только библиотека HATEOAS, без дополнительных HAL-конвертеров.

## ADR-003: использовать автоконфигурацию и внедрение сервисов по классам

Принято: удалить девять интерфейсов, повторяющих единственный сервис, и использовать constructor injection. kotlin-spring открывает классы @Service для security/cache/transaction proxies; отдельные интерфейсы для этого не нужны. VersionService получает ModRepository вместо ленивой обратной ссылки на ModService, устраняя цикл. Интерфейсы Repository, ISimpleStorageService и ObjectStorageClient сохраняют настоящие границы и альтернативные реализации. Короткие команды и исключения объединены в предметные файлы, общая проверка principal переиспользуется контроллерами.

RedisCacheManager создаётся автоконфигурацией Boot. TTL, префикс и null задаются свойствами, RedisCacheManagerBuilderCustomizer сохраняет transactionAware. Стандартный Logback заменяет отдельный XML; свойства ротации и профили сохраняют консоль/файл для dev, файл для prod и консоль для test. Диалект и драйвер PostgreSQL определяются автоматически. Настройки Kotlin/JDK/BOM общие для модулей; domain использует spring-data-commons, spring-web и spring-tx без JPA, persistence подключает JPA.

Прочитаны официальные справочники Spring Boot 4.0: [Kotlin](https://docs.spring.io/spring-boot/4.0/reference/features/kotlin.html), [внедрение зависимостей](https://docs.spring.io/spring-boot/4.0/reference/using/spring-beans-and-dependency-injection.html), [Redis-кэш](https://docs.spring.io/spring-boot/4.0/reference/io/caching.html), [логирование](https://docs.spring.io/spring-boot/4.0/reference/features/logging.html), [управление зависимостями](https://docs.spring.io/spring-boot/4.0/reference/using/build-systems.html). Для сохранения сериализации и транзакций проверен исходник автоконфигурации версии 4.0.5. В cleanup удалена только внешняя транзакция вокруг nextTask: сам метод уже выполняет claim через REQUIRES_NEW. Outbox, блокировки, retry и компенсация S3 сохранены.

Альтернатива: универсальные CRUD-контроллеры и репозитории либо слияние всех слоёв. Они затрудняют сохранение разных правил доступа, публикации, коллекций и требований лабораторной. Сокращается повторение, а предметные операции и три модели остаются явными.

## ADR-004: AWS SDK для S3-интерфейса SeaweedFS

Принято: заменить MinIO Java Client на S3Client из AWS SDK for Java v2, версия 2.57.2 задаётся в одном месте. Сохранены endpoint/credentials/region, бакеты, ключи, HTTP-контракт и журнал компенсации/outbox. Синхронный SDK использует UrlConnectionHttpClient, таймауты подключения 5 секунд, чтения 30 секунд и всей операции 2 минуты. Spring закрывает S3Client при остановке. Для версии SeaweedFS 4.48 используются path-style, обычный PUT без chunked encoding и RequestChecksumCalculation.WHEN_REQUIRED: optional checksum-трейлеры вызывали Content-Md5 error. NoSuchBucketPolicy при удалении отсутствующей политики считается завершённой операцией; другие ошибки не скрываются.

Потоковая загрузка использует RequestBody.fromContentProvider с точным размером MultipartFile. ContentStreamProvider.fromInputStreamSupplier закрывает предыдущий поток и открывает новый при retry; последний закрывается через use даже при исключении. Это сохраняет повторные попытки для временных multipart-файлов без mark/reset и не загружает тело целиком в память.

Альтернатива: официальный com.seaweedfs:seaweedfs-client 4.38. Он работает с Filer/gRPC, требует другого подключения и не управляет S3-политиками. В опубликованных исходниках FilerClient.lookupEntry возвращает null при любых исключениях, а SeaweedWrite.writeMeta не проверяет error в ответе createEntry; для наших гарантий потребовались бы дополнительные проверки и обёртки. Для обычного object storage стандартный S3-клиент проще; native имеет смысл при необходимости файловых возможностей Filer.

Прочитаны [официальный README SeaweedFS](https://github.com/seaweedfs/seaweedfs#the-most-complete-s3-api), [нативный Java Client](https://github.com/seaweedfs/seaweedfs/tree/master/other/java/client), исходники опубликованной версии 4.38 и [документация AWS по потоковым загрузкам](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/best-practices-s3-uploads.html). Совместимость проверяется на закреплённом контейнере SeaweedFS, включая крупный поток, Unicode/пробел/+, политики и повторное удаление.
