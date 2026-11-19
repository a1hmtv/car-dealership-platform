![Header](https://capsule-render.vercel.app/api?type=waving&color=0:FFF0F5,50:F0B5CF,100:D77EA5&height=280&section=header&text=car%20dealership&fontSize=65&fontColor=672344&fontFamily=Georgia&fontAlignY=38&desc=MICROSERVICE%20BACKEND%20PLATFORM&descSize=18&descAlignY=58)
![Java](https://img.shields.io/badge/Java-963B67?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-AC4D77?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-B74D70?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-672344?style=for-the-badge&logo=docker&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-963B67?style=for-the-badge)
![Keycloak](https://img.shields.io/badge/Keycloak-AC4D77?style=for-the-badge&logo=keycloak&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-B74D70?style=for-the-badge&logo=gradle&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-672344?style=for-the-badge&logo=liquibase&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-963B67?style=for-the-badge&logo=swagger&logoColor=white)

## ✧ О проекте

Backend-приложение для автоматизации бизнес-процессов мультибрендового автосалона. Система объединяет управление продажами автомобилей, подбор индивидуальных комплектаций, складской учёт и сопровождение клиентских заказов.

Проект построен на микросервисной архитектуре с использованием синхронного и асинхронного межсервисного взаимодействия.

## ✧ Функциональные возможности

- Каталог автомобилей с расширенной системой фильтрации.
- Конфигуратор автомобилей с проверкой совместимости комплектующих и расчётом итоговой стоимости.
- Управление жизненным циклом заказов.
- Оформление заявок на тест-драйв.
- Управление складскими остатками и резервирование комплектующих.
- Организация внутренних процессов сборки автомобилей.
- Разграничение прав доступа пользователей.
- Получение актуальных данных о наличии автомобилей.

## ✧ Архитектура

Система разделена на два автономных микросервиса, каждый из которых имеет собственную базу данных и API.

**Order Service**

- Управление клиентскими заказами.
- Конфигурирование автомобилей.
- Обработка заявок на тест-драйв.
- Управление жизненным циклом заказов.
- Публикация бизнес-событий.

**Storage Service**

- Управление каталогом автомобилей и комплектующих.
- Учёт складских остатков.
- Резервирование ресурсов.
- Управление заказами на сборку.
- Обработка событий клиентских заказов.

## ✧ Межсервисное взаимодействие

В системе используются два подхода к взаимодействию микросервисов:

- **Асинхронное взаимодействие** — обмен событиями через брокер сообщений для выполнения распределённых бизнес-операций.
- **Синхронное взаимодействие** — получение актуальной информации о складских остатках посредством gRPC.

## ✧ Безопасность

- Централизованная аутентификация через Keycloak.
- Интеграция со Spring Security.
- Ролевая модель доступа (RBAC).
- Проверка принадлежности ресурсов пользователю.
- Декларативная авторизация на уровне методов.

## ✧ Надёжность системы

- Transactional Outbox Pattern.
- Идемпотентная обработка событий.
- Поддержка повторной доставки сообщений.
- Сквозное логирование и корреляция событий.
- Обработка таймаутов и недоступности микросервисов.

## ✧ Архитектурные принципы и паттерны

- Object-Oriented Programming
- SOLID
- Separation of Concerns
- Dependency Injection
- Repository Pattern
- Transactional Outbox Pattern
- Event-Driven Architecture
- Database per Service
- Data Transfer Object

## ✧ Тестирование

Стратегия тестирования включает проверку доменной логики, работы с базой данных, REST API, межсервисного взаимодействия и обработки нештатных ситуаций.

Для изолированного интеграционного тестирования инфраструктурных компонентов используется Testcontainers.

---

*Проект разработан в рамках изучения backend-разработки, проектирования программного обеспечения и распределённых систем.*
![Footer](https://capsule-render.vercel.app/api?type=waving&color=0:FFF0F5,50:F0B5CF,100:D77EA5&height=110&section=footer&text=%E2%99%A1&fontSize=38&fontColor=672344&fontAlign=50&fontAlignY=72)