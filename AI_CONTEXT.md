# Контекст WebLab №3

Цель: сервер ModWeave по контракту OpenAPI с рабочим Swagger UI, совместимостью v1/v2 и простым HATEOAS. Термины и сценарии описаны в [README](README.md); требования — [labs.pdf](labs.pdf), страницы 2 и 7–8. Проверка требований и план по слоям — [docs/lab-review.md](docs/lab-review.md), решения — [ADR](docs/architecture/decisions.md), цикл работы с агентом — [AI_REVIEW.md](AI_REVIEW.md).

Источником истины служит [openapi/openapi.yaml](openapi/openapi.yaml) и подключенные YAML. Сборка включает этот же контракт в Swagger. Сначала обоснованно меняется исходный контракт, затем код и тесты; тесты нельзя ослаблять ради успешного прогона.

Границы: `layers/api` содержит HTTP, DTO, ссылки и security; `layers/domain` — модели, сервисы, команды и интерфейсы Repository; `layers/persistence` — JPA-модели, адаптеры Repository и S3. Контроллеры не обращаются к JPA. Существующие v1 и v2 работают одновременно с общими сервисами. TUI/GUI в текущем исходном дереве отсутствуют. Лабораторные №4–9, новый клиент и GraphQL не входят в текущую задачу.

До одобрения мод и его файлы доступны только автору/администратору. Действия в `_links` отражают права, но безопасность обеспечивают серверные проверки. Сохраняются JSON-поля `content`/`items` и обязательные `href`/`method`. Ссылки строятся через `apiLink(Controller::method, ...)` и `WebMvcLinkBuilder.linkTo`, HTTP-методы берутся из аннотаций контроллера. `ApiResourceAssembler` собирает DTO в `EntityModel`; адаптер `ApiModel` сохраняет форму OpenAPI. Общий advice подключает assembler в v2, оставляя v1 без ссылок. Тот же builder используется для Location и файлового Link.

Нужны JDK 25, Node.js 22.12+ и Docker. Запуск:

```sh
docker compose -f docker/docker-compose.yaml up -d
export JWT_SECRET="$(openssl rand -base64 32)"
./gradlew bootRun
```

Swagger UI: http://localhost:8080/api/v2/docs. Секрет хранить вне репозитория и сохранять между запусками. Не включать реальные персональные данные и секреты в контекст или логи.

Проверки:

```sh
./gradlew check ktfmtCheck bootJar
./gradlew test --tests 'git.walhay.modweave.integration.ApiContractTest'
./gradlew mockOpenApi
# В другом терминале, пока mock запущен:
./gradlew testOpenApi
```

`check` включает линтер OpenAPI и тесты с изолированными PostgreSQL/S3. HTTP-тест проверяет все 37 operationId, положительные/отрицательные сценарии, схемы, обязательные заголовки, PATCH/null, права, ссылки, условные запросы, v1/v2 и ресурсы Swagger. Результаты — `build/reports/tests/test/index.html`. `ktfmtCheck` и компиляция проверяют формат и типы; направления импортов проверяются при ревью. Полнофункциональность Swagger проверяется дополнительно в браузере через Try it out / Execute.
