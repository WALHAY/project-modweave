# Лабораторная работа № 1

## Объект тестирования

Тестируется многомодульное Kotlin/Spring Boot-приложение `modweave`. Основные компоненты:

- `layers/business-logic` — сервисы бизнес-логики;
- `layers/business-logic` и общие unit-компоненты;
- `src/test/.../service/*ServiceTest.kt` — раздельные unit-тесты сервисов с Mockito;
- `src/test/.../service/ServiceTestSupport.kt` — общие настройки paging и сортировки.

## Выполненные требования

### Техники подготовки данных

| Набор тестов | Техника | Применение |
|---|---|---|
| `CategoryServiceTest`, `UserServiceTest` | Граничные значения и классы эквивалентности | пустые/обычные значения, максимальный размер страницы, дубликаты логина, email и категории |
| `GameServiceTest`, `ModServiceTest` | Переходы состояний | создание, сохранение изображения, ошибка промежуточного шага и компенсационное удаление |
| `CollectionServiceTest`, `CommentServiceTest` | Сценарии взаимодействия | последовательность вызовов зависимостей, добавление/удаление модов и проверка результата |
| `FileServiceTest` | Таблица решений | все файлы загружены либо откат уже загруженных файлов при исключении |
| `VersionServiceTest` | Комбинаторное тестирование | владелец/не владелец, уникальное/повторное имя версии, разные статусы |
| `SupportingComponentsTest` | Покрытие ветвей и преобразований | paging/sorting, DTO, JWT, security-проверки, валидатор изображений и строковые утилиты |
| `StubStyleServiceTest` | Stub-тестирование | ручной in-memory `CategoryRepository`, который задаёт состояние и возвращает результаты без проверки взаимодействий |
| все сервисные тесты | Data Builder и Object Mother | `TestFixtures` предоставляет фабрики доменных объектов и builder-подобные параметры со значениями по умолчанию |
Каждый тест построен в стиле Arrange-Act-Assert: подготовка данных и mock/stub-настроек
выполняется до действия, вызов тестируемого метода отделён от последующих проверок.
Если один сценарий ранее содержал успешный и ошибочный варианты, они разделены на
отдельные тесты. Для сервисов применены два варианта взаимодействия с зависимостями:
London-style с Mockito mock и отдельные stub-тесты с ручными реализациями зависимостей.

В сервисном, вспомогательном и stub-наборе сейчас 86 unit-тестов. Монолитный
`BusinessLogicServiceTest.kt` удалён:
каждый набор теперь запускается как отдельный top-level JUnit-класс, что упрощает
навигацию и поддержку.

## Запуск

Обычный прогон всех тестов:

```bash
./gradlew test
```

Случайный порядок классов и методов включается в задачах `test` и `offlineTest` через
JUnit Jupiter system properties.

Для явной демонстрации требования случайного порядка предусмотрена отдельная Gradle-задача:

```bash
./gradlew clean randomTest
```

`randomTest` запускает весь доступный unit-набор, а `offlineTest` запускает его же
без обращения к сети:

```bash
./gradlew clean offlineTest
```

Режим без сети после предварительного кэширования зависимостей:

```bash
./gradlew --offline offlineTest
```

## Покрытие и отчет

JaCoCo автоматически запускается после `test` и формирует:

- HTML: `build/reports/jacoco/test/html/index.html`;
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`;
- line coverage и branch coverage — в HTML/XML отчетах.

После полного прогона текущие aggregate-показатели JaCoCo:

| Метрика | Покрытие |
|---|---:|
| Instructions | 56.60% |
| Lines | 60.27% |
| Branches | 58.96% |
| Classes | 67.14% |

JUnit автоматически формирует XML и HTML-результаты в `build/test-results/test` и
`build/reports/tests/test/index.html`. Дополнительные отчёты строятся JaCoCo:
`build/reports/jacoco/test/html/index.html` и
`build/reports/jacoco/test/jacocoTestReport.xml`.

## Процессы и конфигурация

По умолчанию Gradle запускает один JVM-процесс `Test` на задачу `test`; количество forked JVM можно изменить параметром `maxParallelForks` у задачи `Test`. В текущей конфигурации отдельные forked JVM не включены, поэтому все тестовые классы одной задачи выполняются в одном процессе, а JUnit управляет порядком методов и классов внутри него.

`offlineTest` запускает unit-тесты с mock/stub-зависимостями. При запуске `--offline`
Gradle использует только локальный кэш зависимостей.

Для сдачи достаточно приложить исходный проект, `docs/lr1/README.md`, HTML-отчёт JUnit и HTML/XML-отчёты JaCoCo. Полный результат проверяется командой `./gradlew test`; offline-сценарий — командами `./gradlew offlineTest` или `./gradlew --offline offlineTest`.
