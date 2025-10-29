# Task Management API

RESTful API для управления задачами с поддержкой HATEOAS (Level 3 maturity).

## Особенности

- ✅ **OpenAPI спецификация** полностью описывает API
- ✅ **OpenAPI Generator** для генерации кода
- ✅ **REST conventions** - все endpoints следуют REST принципам
- ✅ **HTTP методы и идемпотентность** - правильное использование методов
- ✅ **HTTP статус коды** - корректные коды для всех сценариев
- ✅ **Фильтрация, сортировка и пагинация** - полная поддержка
- ✅ **HATEOAS links** - Level 3 maturity с навигационными ссылками
- ✅ **Версионирование API** - поддержка версий через URL
- ✅ **Глобальная обработка ошибок** - единый формат ответов
- ✅ **Интеграционные тесты** - покрытие минимум 80%

## Технологии

- Java 21
- Spring Boot 3.2.5
- Spring Data JPA
- H2 Database (для разработки)
- Liquibase (миграции)
- MapStruct (маппинг)
- Lombok
- OpenAPI 3.0 / Swagger
- RestAssured (тестирование)
- JaCoCo (покрытие кода)

## Запуск

### Предварительные требования

- Java 21+
- Gradle 8+

### Локальная разработка

1. Клонируйте репозиторий
2. Запустите приложение:
   ```bash
   ./gradlew bootRun
   ```
3. Откройте Swagger UI: http://localhost:8080/swagger-ui.html
4. H2 Console: http://localhost:8080/h2-console

### Тестирование

```bash
# Запуск всех тестов
./gradlew test

# Запуск с покрытием
./gradlew test jacocoTestReport

# Просмотр отчета покрытия
open build/reports/jacoco/test/html/index.html
```

### Сборка

```bash
# Сборка проекта
./gradlew build

# Очистка и сборка
./gradlew clean build
```

## API Endpoints

### Задачи

- `GET /api/v1/tasks` - Список задач (с фильтрацией, сортировкой, пагинацией)
- `POST /api/v1/tasks` - Создание задачи
- `GET /api/v1/tasks/{id}` - Получение задачи по ID
- `PUT /api/v1/tasks/{id}` - Обновление задачи
- `PATCH /api/v1/tasks/{id}` - Частичное обновление (JSON Patch)
- `DELETE /api/v1/tasks/{id}` - Удаление задачи

### Комментарии

- `GET /api/v1/tasks/{id}/comments` - Список комментариев к задаче
- `POST /api/v1/tasks/{id}/comments` - Создание комментария

## Фильтрация и сортировка

### Параметры запроса для GET /tasks

- `status` - фильтр по статусу (TODO, IN_PROGRESS, DONE)
- `assignee` - фильтр по исполнителю
- `priority` - фильтр по приоритету (LOW, MEDIUM, HIGH, CRITICAL)
- `sort` - сортировка (например: `priority:desc,createdAt:asc`)
- `page` - номер страницы (начиная с 0)
- `size` - размер страницы (1-100, по умолчанию 20)

### Примеры запросов

```bash
# Получить все задачи со статусом TODO
GET /api/v1/tasks?status=TODO

# Получить задачи с высокой приоритетом, отсортированные по дате создания
GET /api/v1/tasks?priority=HIGH&sort=createdAt:desc

# Пагинация
GET /api/v1/tasks?page=0&size=10

# Комбинированные фильтры
GET /api/v1/tasks?status=IN_PROGRESS&assignee=john&page=0&size=5
```

## HATEOAS

API поддерживает HATEOAS (Level 3 maturity). Каждый ресурс содержит ссылки для навигации:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Пример задачи",
  "status": "TODO",
  "priority": "HIGH",
  "_links": {
    "self": {
      "href": "/api/v1/tasks/550e8400-e29b-41d4-a716-446655440000",
      "method": "GET"
    },
    "comments": {
      "href": "/api/v1/tasks/550e8400-e29b-41d4-a716-446655440000/comments",
      "method": "GET"
    }
  }
}
```

## Обработка ошибок

API использует единый формат для всех ошибок:

```json
{
  "error": "NOT_FOUND",
  "message": "Задача с ID 550e8400-e29b-41d4-a716-446655440000 не найдена",
  "timestamp": "2024-01-01T12:00:00Z",
  "path": "/api/v1/tasks/550e8400-e29b-41d4-a716-446655440000"
}
```

### Типы ошибок

- `400 BAD_REQUEST` - Некорректный запрос
- `404 NOT_FOUND` - Ресурс не найден
- `405 METHOD_NOT_ALLOWED` - Метод не поддерживается
- `422 VALIDATION_ERROR` - Ошибка валидации данных
- `500 INTERNAL_SERVER_ERROR` - Внутренняя ошибка сервера

## Валидация

API поддерживает валидацию входных данных:

- Заголовок задачи: 3-200 символов
- Описание: до 2000 символов
- Текст комментария: 1-2000 символов
- Автор комментария: 1-255 символов

## Версионирование

API использует версионирование через URL:
- Текущая версия: `/api/v1/`
- Будущие версии: `/api/v2/`, `/api/v3/` и т.д.

## Покрытие тестами

Проект включает комплексные интеграционные тесты с покрытием минимум 80%:

- Тестирование всех CRUD операций
- Тестирование фильтрации и сортировки
- Тестирование пагинации
- Тестирование обработки ошибок
- Тестирование валидации данных
- Тестирование HATEOAS ссылок

## Структура проекта

```
src/
├── main/
│   ├── java/
│   │   └── ru/mentee/power/
│   │       ├── controller/     # REST контроллеры
│   │       ├── service/        # Бизнес-логика
│   │       ├── domain/         # Доменные модели и репозитории
│   │       ├── dto/            # DTO классы
│   │       ├── exception/      # Обработка ошибок
│   │       └── config/         # Конфигурация
│   └── resources/
│       ├── application.properties
│       ├── task-management-api.yaml  # OpenAPI спецификация
│       └── db/changelog/       # Liquibase миграции
└── test/
    ├── java/
    │   └── ru/mentee/power/integration/  # Интеграционные тесты
    └── resources/
        └── application-test.properties
```

## Лицензия

MIT License
