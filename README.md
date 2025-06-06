# Spring Boot WebFlux Emulator

Эмулятор REST API на базе Spring WebFlux. Предоставляет эмуляцию общего API с настраиваемой задержкой ответа, а также специализированный эмулятор для ответов на запросы заказов (order requests) с динамической генерацией JSON. Включает мониторинг через Prometheus + Grafana.

## Возможности

- **Эмуляция общего REST API** с настраиваемой неблокирующей задержкой ответа (конфигурируется через `application.yml`, ответ из `response.json`).
- **Эмуляция сервиса ответов на запросы заказов (Order Requests)**:
    - Динамическая генерация JSON-ответов на основе параметров запроса.
    - Данные для генерации ответов (информация о купонах) загружаются из `src/main/resources/cpn-list.json`.
    - Использование Jackson для сериализации JSON с точным соответствием предопределенной структуре (с помощью Java Records и аннотации `@JsonPropertyOrder`).
    - Настраиваемая неблокирующая задержка ответа (конфигурируется через `application.yml`).
- Асинхронная обработка всех запросов с помощью Project Reactor.
- **Юнит-тесты** для проверки структуры JSON и времени отклика сервиса заказов.
- Мониторинг через Prometheus и Grafana.
- Docker контейнеризация.
- Оптимизированное управление потоками.

## Технологии

- Java 21
- Spring Boot 3.1.5
- Spring WebFlux
- Project Reactor
- Prometheus
- Grafana
- Docker

## Запуск

1. Сборка проекта:
```bash
mvn clean package
```

2. Сборка и запуск Docker контейнера:
```bash
docker build -t emulator-service .
docker run -d -p 8080:8080 --name emulator emulator-service
```

3. Проверка работоспособности:
```bash
curl http://localhost:8080/api/v1/emulate
```

## Мониторинг

1. Метрики доступны по адресу:
```
http://localhost:8080/actuator/prometheus
```

2. Основные метрики:
- `application_thread_pool_size` - размер пула потоков приложения
- `jvm_threads_live_threads` - количество живых потоков JVM
- `jvm_threads_states_threads` - состояния потоков

## Конфигурация

Настройки в `application.yml`:
```yaml
emulator:
  delay: # Общие настройки задержки, применяются ко всем эмулируемым ответам
    min: 100  # Минимальная задержка (мс)
    max: 1000 # Максимальная задержка (мс)
  # Конфигурация для общего эмулятора (ответ из response.json)
  response-config: classpath:response.json

# Важно: Для эмуляции сервиса заказов используется файл `src/main/resources/cpn-list.json`.
# Этот файл содержит данные о купонах и должен присутствовать для корректной работы соответствующего эндпоинта.
# Структура этого файла важна для правильной десериализации.
```

## Лицензия

MIT
