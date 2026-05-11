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

## ЭЦП тикета лицензии

Подпись тикета выполняется по схеме:
- canonical JSON (детерминированная сериализация)
- UTF-8 байты канонического JSON
- `SHA256withRSA`
- Base64-представление подписи

### Генерация keystore и сертификата

```bash
chmod +x ./scripts/signature/generate-signature-keystore.sh
./scripts/signature/generate-signature-keystore.sh
```

По умолчанию скрипт создаёт:
- `./.secrets/signature/ticket-signing.p12` (приватный ключ + сертификат)
- `./.secrets/signature/ticket-signing.crt` (публичный сертификат)
- `./.secrets/signature/ticket-signing.crt.base64` (значение для CI/CD variable)

### Переменные окружения для подписи

- `SIGNATURE_KEYSTORE_PATH`
- `SIGNATURE_KEYSTORE_TYPE` (обычно `PKCS12`)
- `SIGNATURE_KEYSTORE_PASSWORD`
- `SIGNATURE_KEY_ALIAS`
- `SIGNATURE_KEY_PASSWORD`
- `SIGNATURE_ALGORITHM` (по умолчанию `SHA256withRSA`)
- `SIGNATURE_PUBLIC_CERT_BASE64` (Base64 от X.509 сертификата с публичным ключом)
- `TICKET_TTL_SECONDS`

## PostgreSQL

Пример подключения (используется по умолчанию):

```bash
DB_URL=jdbc:postgresql://localhost:5432/newsem_db
DB_USER=postgres
DB_PASSWORD=postgres
```

## Загрузка сигнатур из файлов и MinIO

Для файлов сигнатур используется приватный bucket в MinIO. Исходный файл сохраняется в object storage, а в БД хранится рассчитанная сигнатура и метаданные объекта.

### Docker Compose

```bash
docker compose up -d
```

Поднимутся:
- `postgres`
- `minio`
- `minio-init` для создания приватного bucket `malware-signatures`, сервисного пользователя `newsem-service` и выдачи ему policy `readwrite`

### Переменные окружения для object storage

- `STORAGE_ENDPOINT`
- `STORAGE_ACCESS_KEY`
- `STORAGE_SECRET_KEY`
- `STORAGE_BUCKET`
- `STORAGE_PRESIGNED_URL_EXPIRY_MINUTES`
- `STORAGE_FIRST_BYTES_LENGTH`

По умолчанию:

```bash
STORAGE_ENDPOINT=http://localhost:9000
STORAGE_ACCESS_KEY=newsem-service
STORAGE_SECRET_KEY=newsem-service-secret
STORAGE_BUCKET=malware-signatures
STORAGE_PRESIGNED_URL_EXPIRY_MINUTES=60
STORAGE_FIRST_BYTES_LENGTH=16
```

### Новые malware API endpoints

Все endpoints ниже доступны только пользователю с ролью `ADMIN`:

- `POST /api/malware-signatures/upload`
- `POST /api/malware-signatures/files/presigned-urls`
- `GET /api/malware-signatures`
- `GET /api/malware-signatures/increment`
- `POST /api/malware-signatures/by-ids`
- `GET /api/malware-signatures/{id}/history`
- `GET /api/malware-signatures/{id}/audit`

`POST /api/malware-signatures/upload` принимает `multipart/form-data`:
- `file` - обязательный файл
- `threatName` - необязательное имя угрозы

Расчет сигнатуры по файлу выполняется так:
- `firstBytesHex` - первые `STORAGE_FIRST_BYTES_LENGTH` байт файла
- `remainderHashHex` - `SHA-256` от оставшейся части файла
- `remainderLength` - длина оставшейся части
- `offsetStart = 0`
- `offsetEnd = длина сохраненного префикса - 1`

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
- `SIGNATURE_KEYSTORE_PATH`, `SIGNATURE_KEYSTORE_TYPE`
- `SIGNATURE_KEYSTORE_PASSWORD`, `SIGNATURE_KEY_ALIAS`, `SIGNATURE_KEY_PASSWORD`
- `SIGNATURE_PUBLIC_CERT_BASE64` (рекомендуется в `Repository Variables`, не в `Secrets`)
- `TICKET_TTL_SECONDS`

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
