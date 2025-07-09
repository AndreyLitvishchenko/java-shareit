# ShareIt - Сервис Шаринга Вещей

ShareIt - это веб-приложение для обмена вещами между пользователями. Пользователи могут добавлять свои вещи, бронировать вещи других пользователей и оставлять отзывы.

## Функциональность

### Управление пользователями
- Создание, просмотр, обновление и удаление пользователей
- Уникальность email адресов

### Управление вещами
- Создание, просмотр, обновление вещей
- Поиск вещей по названию и описанию
- Просмотр доступных для аренды вещей

### Система бронирования
- Создание запроса на бронирование
- Подтверждение/отклонение бронирования владельцем
- Просмотр бронирований пользователя
- Просмотр бронирований для вещей владельца
- Фильтрация бронирований по статусу (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)

### Система отзывов
- Добавление комментариев к вещам
- Только пользователи, бравшие вещь в аренду, могут оставлять отзывы
- Просмотр комментариев при просмотре вещей

## Технический стек

- **Java 11**
- **Spring Boot 3.1.0**
- **Spring Data JPA**
- **PostgreSQL** (продакшн)
- **H2** (тесты)
- **Maven**
- **Lombok**
- **JUnit 5 + Mockito** (тестирование)

## Структура проекта

```
src/main/java/ru/practicum/shareit/
├── booking/                    # Бронирования
│   ├── dto/                   # DTO классы
│   ├── mapper/                # Мапперы
│   ├── repository/            # Репозитории
│   ├── service/               # Сервисы
│   ├── Booking.java           # Сущность
│   ├── BookingController.java # Контроллер
│   ├── BookingState.java      # Состояния для фильтрации
│   └── BookingStatus.java     # Статусы бронирования
├── exception/                 # Обработка исключений
├── item/                      # Вещи
│   ├── dto/                   # DTO классы
│   ├── mapper/                # Мапперы
│   ├── model/                 # Сущности (Item, Comment)
│   ├── repository/            # Репозитории
│   ├── service/               # Сервисы
│   └── ItemController.java    # Контроллер
├── request/                   # Запросы на вещи (заготовка)
├── user/                      # Пользователи
│   ├── controller/            # Контроллер
│   ├── dto/                   # DTO классы
│   ├── mapper/                # Мапперы
│   ├── model/                 # Сущность User
│   ├── repository/            # Репозиторий
│   └── service/               # Сервисы
└── ShareItApp.java            # Главный класс приложения
```

## API Endpoints

### Пользователи
- `GET /users` - получить всех пользователей
- `GET /users/{userId}` - получить пользователя по ID
- `POST /users` - создать пользователя
- `PATCH /users/{userId}` - обновить пользователя
- `DELETE /users/{userId}` - удалить пользователя

### Вещи
- `GET /items` - получить вещи пользователя
- `GET /items/{itemId}` - получить вещь по ID
- `POST /items` - создать вещь
- `PATCH /items/{itemId}` - обновить вещь
- `GET /items/search?text={text}` - поиск вещей
- `POST /items/{itemId}/comment` - добавить комментарий

### Бронирования
- `POST /bookings` - создать бронирование
- `PATCH /bookings/{bookingId}?approved={approved}` - подтвердить/отклонить бронирование
- `GET /bookings/{bookingId}` - получить бронирование по ID
- `GET /bookings?state={state}` - получить бронирования пользователя
- `GET /bookings/owner?state={state}` - получить бронирования для вещей владельца

## Настройка базы данных

### Для разработки (PostgreSQL)
1. Установите PostgreSQL
2. Создайте базу данных `shareit`
3. Создайте пользователя `shareit` с паролем `shareit`
4. Настройте подключение в `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shareit
spring.datasource.username=shareit
spring.datasource.password=shareit
```

### Схема базы данных
Схема создается автоматически при запуске из файла `schema.sql`. Включает таблицы:
- `users` - пользователи
- `items` - вещи
- `bookings` - бронирования
- `comments` - комментарии
- `requests` - запросы на вещи

## Запуск приложения

### Из IDE
1. Клонируйте репозиторий
2. Откройте проект в IDE
3. Настройте подключение к PostgreSQL
4. Запустите `ShareItApp.main()`

### Из командной строки
```bash
# Компиляция
mvn clean compile

# Запуск тестов
mvn test

# Создание JAR файла
mvn clean package

# Запуск приложения
java -jar target/shareit-0.0.1-SNAPSHOT.jar
```

## Тестирование

Проект включает:
- **Unit тесты** для сервисов с Mockito
- **Integration тесты** контроллеров с MockMvc
- **End-to-end тесты** полного функционала

Запуск тестов:
```bash
mvn test
```

## Примеры использования API

### Создание пользователя
```bash
POST /users
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@example.com"
}
```

### Создание вещи
```bash
POST /items
X-Sharer-User-Id: 1
Content-Type: application/json

{
    "name": "Дрель",
    "description": "Мощная электрическая дрель",
    "available": true
}
```

### Создание бронирования
```bash
POST /bookings
X-Sharer-User-Id: 2
Content-Type: application/json

{
    "itemId": 1,
    "start": "2025-07-10T10:00:00",
    "end": "2025-07-12T10:00:00"
}
```

### Подтверждение бронирования
```bash
PATCH /bookings/1?approved=true
X-Sharer-User-Id: 1
```

### Добавление комментария
```bash
POST /items/1/comment
X-Sharer-User-Id: 2
Content-Type: application/json

{
    "text": "Отличная дрель, рекомендую!"
}
```

## Статусы бронирования

- **WAITING** - новое бронирование, ожидает подтверждения
- **APPROVED** - бронирование подтверждено владельцем
- **REJECTED** - бронирование отклонено владельцем
- **CANCELED** - бронирование отменено создателем

## Состояния для фильтрации бронирований

- **ALL** - все бронирования
- **CURRENT** - текущие бронирования
- **PAST** - завершенные бронирования
- **FUTURE** - будущие бронирования
- **WAITING** - ожидающие подтверждения
- **REJECTED** - отклоненные
