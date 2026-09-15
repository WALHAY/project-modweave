# Лабораторная работа № 1

## Объект тестирования

Тестируется многомодульное Kotlin/Spring Boot-приложение `modweave`. Основные компоненты:

- `layers/business-logic` — сервисы бизнес-логики;
- `layers/data-access` и `src/test/.../repository` — адаптеры доступа к PostgreSQL;
- `src/test/.../service/BusinessLogicServiceTest.kt` — unit-тесты сервисов с Mockito.

## Выполненные требования

### Техники подготовки данных

| Набор тестов | Техника | Применение |
|---|---|---|
| `CategoryServiceTests`, `UserServiceTests` | Граничные значения и классы эквивалентности | пустые/обычные значения, максимальный размер страницы, дубликаты логина, email и категории |
| `GameServiceTests`, `ModServiceTests` | Переходы состояний | создание, сохранение изображения, ошибка промежуточного шага и компенсационное удаление |
| `CollectionServiceTests`, `CommentServiceTests` | Сценарии взаимодействия | последовательность вызовов зависимостей и проверка результата |
| `FileServiceTests` | Таблица решений | все файлы загружены либо откат уже загруженных файлов при исключении |
| `VersionServiceTests` | Комбинаторное тестирование | владелец/не владелец, уникальное/повторное имя версии, разные статусы |
| все сервисные тесты | Data Builder и Object Mother | `TestFixtures` предоставляет фабрики доменных объектов и builder-подобные параметры со значениями по умолчанию |
| `*RepositoryTest` | Классический вариант | тесты выполняются с PostgreSQL Testcontainers без mock/stub |

Каждый тест построен в стиле Arrange-Act-Assert. Для сервисов применён London-style через Mockito; repository-тесты оставлены в классическом варианте.

## Запуск

Обычный прогон всех тестов:

```bash
./gradlew test
```

Случайный порядок классов и методов включается в `test` и `offlineTest` через JUnit Jupiter system properties.

Только unit-тесты без PostgreSQL/Testcontainers:

```bash
./gradlew offlineTest
```

Режим без сети после предварительного кэширования зависимостей:

```bash
./gradlew --offline offlineTest
```

Интеграционные тесты доступа к данным:

```bash
./gradlew test --tests 'git.walhay.modweave.repository.*'
```

## Покрытие и отчет

JaCoCo автоматически запускается после `test` и формирует:

- HTML: `build/reports/jacoco/test/html/index.html`;
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`;
- line coverage и branch coverage — в HTML/XML отчетах.

Allure-результаты записываются в `build/allure-results`. HTML-отчет генерируется командой:

```bash
./gradlew allureReport
```

## Процессы и конфигурация

По умолчанию Gradle запускает один JVM-процесс `Test` на задачу `test`; количество forked JVM можно изменить параметром `maxParallelForks` у задачи `Test`. В текущей конфигурации отдельные forked JVM не включены, поэтому все тестовые классы одной задачи выполняются в одном процессе, а JUnit управляет порядком методов и классов внутри него. Testcontainers запускается в том же тестовом JVM-процессе и создает отдельный контейнер PostgreSQL для интеграционного набора.

## Примечание по offline-режиму

`offlineTest` исключает тесты, унаследованные от `PostgresTestTemplate`, так как они требуют Docker-образ PostgreSQL и инфраструктуру Testcontainers. Unit-тесты с Mockito не обращаются к сети и запускаются через локальный кэш Gradle.
