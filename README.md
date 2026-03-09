# My Blog Backend Application

Бэкенд для приложения-блога, реализованный на чистом Spring Framework (без Spring Boot) с использованием сервлет-контейнера Tomcat.

## Технологии

- Java 21
- Spring Framework 6.1 (MVC, JDBC, Test)
- Jakarta Servlet 6.0
- Apache Tomcat 10
- H2 Database (in-memory для разработки)
- Maven
- JUnit 5, Mockito

## Функциональность

- ✅ Управление постами (CRUD)
- ✅ Загрузка и получение изображений
- ✅ Лайки постов
- ✅ Управление комментариями (CRUD)
- ✅ Пагинация и поиск постов
- ✅ Фильтрация по тегам
- ✅ REST API для фронтенда

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
- Maven 3.8+
- Apache Tomcat 10
- (Опционально) Docker для фронтенда

## Сборка проекта

```bash
# Клонировать репозиторий
git clone https://github.com/Besfian/java-dev.git
cd java-dev

# Собрать проект (с тестами)
./mvnw clean package

# Собрать без тестов (для быстрого деплоя)
./mvnw clean package -DskipTests