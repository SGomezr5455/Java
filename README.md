## YSTU Schedule API

REST API для управления расписанием Ярославского государственного технического университета.

## Технологии

| Технология | Версия |
|-----------|--------|
| Java | 25 |
| Spring Boot | 3.4.5 |
| Spring Security + JWT | 6.4.5 / JJWT 0.12.6 |
| Spring Data JPA + Hibernate | 6.6.13 |
| PostgreSQL | 18 |
| Liquibase | 4.29.2 |
| Maven | 3.x |
| JUnit 5 + Mockito | — |
| JaCoCo | 0.8.12 |

---

## Сущности (8 штук)

- **Faculty** — факультеты
- **Department** — кафедры (связь с Faculty)
- **Group** — учебные группы (связь с Faculty)
- **Teacher** — преподаватели (связь с Department, Subject)
- **Subject** — дисциплины
- **Room** — аудитории
- **Lesson** — занятия (связь с Subject, Teacher, Room, Group)
- **User** — пользователи системы с ролями

---

## Требования для запуска

- JDK 21+
- PostgreSQL (создать БД `ystu_schedule`)
- Maven

### Создание базы данных

```sql
CREATE DATABASE ystu_schedule;
CREATE USER ystu WITH PASSWORD 'ystu';
GRANT ALL PRIVILEGES ON DATABASE ystu_schedule TO ystu;
```

---

## Запуск приложения

Через файл ..\src\main\java\com\ystu\schedule\ScheduleApplication.java

Приложение запустится на порту **8081**.

При первом запуске Liquibase автоматически создаст все таблицы в базе данных.

---

## Запуск тестов

```bash
mvn verify
```

После выполнения отчёт о покрытии доступен в:
```
..\src\test\java\com\ystu\schedule\controller
```

Минимальное покрытие: **70%**

---

## Аутентификация

API использует **JWT Bearer Token** аутентификацию.

### Регистрация
```http
POST /api/v1/auth/register
Content-Type: application/json

{
    "username": "admin",
    "email": "admin@ystu.ru",
    "password": "admin123",
    "role": "ADMIN"
}
```

### Вход
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

Ответ содержит токен, который нужно передавать в заголовке:
```
Authorization: Bearer <token>
```

---

## Роли и права доступа

| Метод | VIEWER | EDITOR | ADMIN |
|-------|--------|--------|-------|
| GET | + | + | + |
| POST | - | + | + |
| PUT | - | + | + |
| DELETE | - | - | + |

---

## API Endpoints

### Факультеты
```
GET    /api/v1/faculties
GET    /api/v1/faculties/{id}
POST   /api/v1/faculties
PUT    /api/v1/faculties/{id}
DELETE /api/v1/faculties/{id}
```

### Кафедры
```
GET    /api/v1/departments?facultyId=
GET    /api/v1/departments/{id}
POST   /api/v1/departments
PUT    /api/v1/departments/{id}
DELETE /api/v1/departments/{id}
```

### Группы
```
GET    /api/v1/groups?facultyId=
GET    /api/v1/groups/{id}
POST   /api/v1/groups
PUT    /api/v1/groups/{id}
DELETE /api/v1/groups/{id}
```

### Преподаватели
```
GET    /api/v1/teachers?departmentId=
GET    /api/v1/teachers/{id}
POST   /api/v1/teachers
PUT    /api/v1/teachers/{id}
DELETE /api/v1/teachers/{id}
POST   /api/v1/teachers/{id}/subjects/{subjectId}
DELETE /api/v1/teachers/{id}/subjects/{subjectId}
```

### Дисциплины
```
GET    /api/v1/subjects
GET    /api/v1/subjects/{id}
POST   /api/v1/subjects
PUT    /api/v1/subjects/{id}
DELETE /api/v1/subjects/{id}
```

### Аудитории
```
GET    /api/v1/rooms?type=
GET    /api/v1/rooms/{id}
POST   /api/v1/rooms
PUT    /api/v1/rooms/{id}
DELETE /api/v1/rooms/{id}
```

Типы аудиторий: `LECTURE_HALL`, `LAB`, `SEMINAR_ROOM`, `GYM`

### Занятия
```
GET    /api/v1/lessons
GET    /api/v1/lessons?groupId=
GET    /api/v1/lessons?teacherId=
GET    /api/v1/lessons?roomId=
GET    /api/v1/lessons?weekNumber=
GET    /api/v1/lessons/{id}
POST   /api/v1/lessons
PUT    /api/v1/lessons/{id}
DELETE /api/v1/lessons/{id}
POST   /api/v1/lessons/{id}/groups/{groupId}
DELETE /api/v1/lessons/{id}/groups/{groupId}
```

Типы занятий: `LECTURE`, `PRACTICE`, `LAB`, `CONSULTATION`, `COURSEWORK`

---

## Мониторинг (Spring Boot Actuator)

```
GET /actuator/health    — состояние приложения и БД
GET /actuator/info      — информация о приложении
GET /actuator/metrics   — метрики JVM и HTTP запросов
GET /actuator/loggers   — уровни логирования
GET /actuator/env       — переменные окружения
```

---

## Логирование

Каждый HTTP запрос логируется в консоль:

```
[REQUEST]  POST /api/v1/faculties | user: admin | IP: 127.0.0.1
[RESPONSE] 201 | 15ms
```

---

## Структура проекта

```
src/
├── main/
│   ├── java/com/ystu/schedule/
│   │   ├── config/          — конфигурация (JPA, Security, JWT)
│   │   ├── controller/      — REST контроллеры
│   │   ├── dto/             — объекты запроса и ответа
│   │   ├── entity/          — JPA сущности
│   │   ├── exception/       — обработка исключений
│   │   ├── filter/          — фильтры (JWT, логирование)
│   │   ├── repository/      — репозитории Spring Data
│   │   ├── security/        — JWT утилиты, UserDetailsService
│   │   └── service/         — бизнес-логика
│   └── resources/
│       ├── db/changelog/    — Liquibase миграции
│       └── application.properties
└── test/
    └── java/com/ystu/schedule/
        ├── controller/      — интеграционные тесты (MockMvc + H2)
        └── service/         — unit тесты (Mockito)
```

---

## Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешно |
| 201 | Создано |
| 204 | Удалено |
| 400 | Ошибка валидации |
| 401 | Не аутентифицирован |
| 403 | Нет прав |
| 404 | Не найдено |
| 409 | Дубликат |
| 500 | Ошибка сервера |
