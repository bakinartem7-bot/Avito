# Дипломный проект: Сайт объявлений (Ads Service)

Сервис для размещения и просмотра объявлений с авторизацией пользователей.

## Стек технологий
- Java 17
- Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL
- Liquibase (миграции БД)
- Testcontainers (тестирование с реальной БД)
- Lombok
- Swagger UI (OpenAPI)
- JWT (авторизация)

## Запуск проекта

### Локально
1. Установите PostgreSQL.
2. Создайте базу данных `ads_db`.
3. В файле `src/main/resources/application.properties` укажите свои учетные данные для БД.
4. Запустите приложение через IDE или командой `mvn spring-boot:run`.

### Docker (опционально)
Используйте `docker-compose.yml` для поднятия БД и приложения.

## API
Документация доступна по адресу: `http://localhost:8080/swagger-ui.html`

## Структура проекта
- `controller`: REST контроллеры
- `service`: Бизнес-логика
- `repository`: Доступ к данным
- `entity`: Модели БД
- `dto`: Объекты передачи данных
- `mapper`: Конвертация Entity <-> DTO
- `exception`: Обработка ошибок
- `security`: Конфигурация безопасности

## Тесты
Проект покрыт юнит-тестами и интеграционными тестами (Testcontainers).
Запуск: `mvn test`

## Критерии диплома
- [x] Liquibase миграции
- [x] Валидация DTO
- [x] JavaDoc комментарии
- [x] Соответствие OpenAPI
- [x] Ролевая модель (User/Admin)
- [x] Защита от удаления чужих объявлений
