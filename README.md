# Spring Boot WebFlux Emulator

Эмулятор REST API на базе Spring WebFlux с настраиваемой задержкой ответа и мониторингом через Prometheus + Grafana.

## Возможности

- Эмуляция REST API с настраиваемой задержкой
- Асинхронная обработка запросов с помощью Project Reactor
- Мониторинг через Prometheus и Grafana
- Docker контейнеризация
- Оптимизированное управление потоками

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
  delay:
    min: 100  # Минимальная задержка (мс)
    max: 1000 # Максимальная задержка (мс)
  response-config: classpath:response.json  # Путь к JSON ответу
```

## Лицензия

MIT
