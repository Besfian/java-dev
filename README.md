# My Blog Backend Application

Бэкенд для приложения-блога, реализованный на Spring Boot 3.2 с использованием встроенного сервлет-контейнера Tomcat.

## Технологии

- Java 21
- Spring Boot 3.2.4
- Spring Web MVC
- Spring Data JDBC
- H2 Database (in-memory)
- Gradle 8.5
- JUnit 5, Mockito, Spring Boot Test
- Lombok

## Функциональность

- ✅ Управление постами (CRUD)
- ✅ Загрузка и получение изображений
- ✅ Лайки постов
- ✅ Управление комментариями (CRUD)
- ✅ Пагинация и поиск постов
- ✅ Фильтрация по тегам (#тег)
- ✅ REST API для фронтенда
- ✅ Глобальная обработка исключений
- ✅ CORS поддержка для фронтенда

## API Endpoints

### Посты

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/posts?search=&pageNumber=1&pageSize=5` | Получить список постов |
| GET | `/api/posts/{id}` | Получить пост по ID |
| POST | `/api/posts` | Создать новый пост |
| PUT | `/api/posts/{id}` | Обновить пост |
| DELETE | `/api/posts/{id}` | Удалить пост |
| POST | `/api/posts/{id}/likes` | Поставить лайк |
| PUT | `/api/posts/{id}/image` | Загрузить изображение |
| GET | `/api/posts/{id}/image` | Получить изображение |

### Комментарии

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/posts/{postId}/comments` | Получить комментарии поста |
| GET | `/api/posts/{postId}/comments/{commentId}` | Получить комментарий |
| POST | `/api/posts/{postId}/comments` | Создать комментарий |
| PUT | `/api/posts/{postId}/comments/{commentId}` | Обновить комментарий |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | Удалить комментарий |

## Требования к окружению

- JDK 21
- Gradle 8.5+
- (Опционально) Docker для фронтенда

## Сборка и запуск

### Сборка проекта

```bash
# Очистить и собрать проект
./gradlew clean build

# Собрать без тестов
./gradlew clean build -x test