# NewSEM Server

Серверная часть проекта на Java Spring Boot с JWT (access/refresh), ролевой моделью и HTTPS.

## Технологии

- Java 21
- Spring Boot 3.2.5
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven

## Быстрый старт

### 1. Переменные окружения

Пример в `.env.example`.

Минимальные:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_ACCESS_SECRET`
- `JWT_REFRESH_SECRET`

### 2. Запуск (HTTP)

```bash
mvn spring-boot:run
```

Сервис доступен по адресу `http://localhost:8080`.

## Аутентификация и авторизация

- JWT access/refresh токены.
- Роли: `ROLE_ADMIN`, `ROLE_HR`, `ROLE_INTERVIEWER`, `ROLE_CANDIDATE`.
- Доступ к эндпоинтам ограничен в `SecurityConfig`.

### Основные эндпоинты

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`

## Лицензирование (основные операции)

- `POST /api/licenses/create` (админ)
- `POST /api/licenses/activate`
- `POST /api/licenses/check`
- `POST /api/licenses/renew`

## PostgreSQL

Пример подключения (используется по умолчанию):

```bash
DB_URL=jdbc:postgresql://localhost:5432/newsem_db
DB_USER=postgres
DB_PASSWORD=postgres
```

## HTTPS (TLS)

Для TLS используется профиль `tls` и keystore в `./.secrets/tls/`.

### Генерация цепочки сертификатов

```bash
chmod +x ./scripts/tls/generate-chain.sh
TLS_KEYSTORE_PASSWORD='strong-password' \
./scripts/tls/generate-chain.sh --student-id <STUDENT_ID> --out-dir ./.secrets/tls --domain localhost
```

### Запуск в TLS режиме

```bash
export TLS_KEYSTORE_PASSWORD='strong-password'
export TLS_KEYSTORE_PATH='./.secrets/tls/server-keystore.p12'
./mvnw spring-boot:run -Dspring-boot.run.profiles=tls
```

По умолчанию TLS профиль слушает порт `8443`.

## CI (GitHub Actions)

Используется `.github/workflows/ci.yml`:
- `test`: `mvn -B test`
- `build`: `mvn -B -DskipTests package`

### Secrets для GitHub Actions

Если используются приватные переменные/секреты, заведите их в репозитории:
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`
- `TLS_KEYSTORE_PATH`, `TLS_KEYSTORE_PASSWORD`, `TLS_KEY_ALIAS`, `TLS_PORT`
- `TICKET_SIGNING_SECRET`, `TICKET_TTL_SECONDS`

## Теория: UML-диаграммы (кратко)

- **Use Case**: роли и сценарии использования системы.
- **Class**: структуры классов и их связи (ассоциации, наследование, композиция).
- **Sequence**: взаимодействие объектов во времени.
- **Activity**: поток работ/логики, ветвления и условия.
- **State**: состояния объекта и переходы между ними.
- **Component**: модули системы и их зависимости.
- **Deployment**: развертывание компонентов на узлах инфраструктуры.

## Теория: ER-диаграммы (кратко)

- **Сущности**: объекты предметной области (таблицы).
- **Атрибуты**: свойства сущности (поля таблицы).
- **Связи**: отношения между сущностями.
- **Кратность**: 1:1, 1:N, M:N.
- **Ключи**: первичный ключ (PK) и внешние ключи (FK).
