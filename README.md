# Modweave

Многомодульное Kotlin/Spring Boot-приложение и набор тестов для лабораторной
работы № 1.

## Быстрый запуск

```bash
./gradlew test
```

Unit-тесты через отдельную задачу:

```bash
./gradlew offlineTest
```

Запуск без доступа к интернету после предварительного кэширования зависимостей:

```bash
./gradlew --offline offlineTest
```

Явный запуск тестов в случайном порядке:

```bash
./gradlew clean randomTest
```

## Отчёты

После запуска `test` Gradle формирует стандартные отчёты:

- [JUnit HTML](build/reports/tests/test/index.html);
- [JUnit XML](build/test-results/test);
- [JaCoCo HTML](build/reports/jacoco/test/html/index.html);
- [JaCoCo XML](build/reports/jacoco/test/jacocoTestReport.xml).

## Лабораторная работа

Подробное описание лабораторной работы находится в
[docs/lr1/README.md](docs/lr1/README.md).

Ниже приведена карта требований: для каждого пункта указаны файлы, в которых
находится реализация или подтверждение.

| № | Требование | Файлы и подтверждение |
|---:|---|---|
| 1 | Позитивный и негативный тест для public-методов основных компонентов | [CategoryServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/CategoryServiceTest.kt), [UserServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/UserServiceTest.kt), [GameServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/GameServiceTest.kt), [ModServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/ModServiceTest.kt), [CollectionServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/CollectionServiceTest.kt), [CommentAndFileServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/CommentAndFileServiceTest.kt), [VersionServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/VersionServiceTest.kt) |
| 2 | Проверка обработки исключений | Тесты с `assertThrows` в [service](src/test/kotlin/git/walhay/modweave/service) |
| 3 | London style с mock и stub | Mockito-тесты в [service](src/test/kotlin/git/walhay/modweave/service), ручные стабы в [StubStyleServiceTest.kt](src/test/kotlin/git/walhay/modweave/service/StubStyleServiceTest.kt) |
| 4 | Arrange–Act–Assert и fixtures/helpers | [TestFixtures.kt](src/test/kotlin/git/walhay/modweave/testutils/TestFixtures.kt), [ServiceTestSupport.kt](src/test/kotlin/git/walhay/modweave/service/ServiceTestSupport.kt) |
| 5 | Один вызов тестируемого метода в Act | Все сервисные тесты в [service](src/test/kotlin/git/walhay/modweave/service); логика удаления мода из коллекции реализована в [CollectionService.kt](layers/business-logic/src/main/kotlin/git/walhay/modweave/api/collection/CollectionService.kt) |
| 6 | Отсутствие тестов приватных/protected-методов | Тесты обращаются к public API сервисов и компонентов |
| 7 | Data Builder и Object Mother | [TestFixtures.kt](src/test/kotlin/git/walhay/modweave/testutils/TestFixtures.kt) |
| 8 | Запуск из командной строки | [build.gradle](build.gradle), команды выше |
| 9 | Автоматические отчёты | Стандартные отчёты JUnit и JaCoCo, пути указаны в разделе [Отчёты](#отчёты) |
| 10 | Случайный порядок | `test`, `offlineTest` и [randomTest](build.gradle) используют `ClassOrderer.Random` и `MethodOrderer.Random` |
| 11 | Запуск без интернета | `./gradlew --offline offlineTest`; задача не запускает внешние сервисы |
| 12 | Анализ процессов | Раздел [Процессы и конфигурация](docs/lr1/README.md#процессы-и-конфигурация): одна JVM Gradle Test-задачи, `maxParallelForks` не настроен |
| 13 | Успешное выполнение | Проверяется командами `./gradlew test` и `./gradlew offlineTest` |
| 14 | Оценка покрытия строк и ветвлений | [jacocoTestReport](build.gradle), HTML/XML JaCoCo-отчёты |
| 15 | Защита от регрессии и поддерживаемость | Раздельные test suites, общие fixtures, AAA, mock/stub-сценарии, типовые аннотации и случайный порядок |

## Структура проекта

- [business-logic](layers/business-logic/src/main/kotlin) — доменные модели и бизнес-сервисы;
- [data-access](layers/data-access/src/main/kotlin) — слой доступа к данным и storage;
- [ui](layers/ui/src/main/kotlin) — HTTP, DTO, security и конфигурация;
- [unit tests](src/test/kotlin/git/walhay/modweave/service) — mock/stub-тесты сервисов;
- [repository unit tests](src/test/kotlin/git/walhay/modweave/repository) — unit-тесты repository-адаптеров с Mockito;
- [лабораторная документация](docs/lr1/README.md).
