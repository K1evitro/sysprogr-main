Отчёт об обучении на курсе системного программирования с Spring Boot
1. Введение
Этот отчёт отражает мой путь обучения разработке REST API с использованием Spring Boot и Java. В процессе обучения я последовательно освоил инструменты разработки и реализовал комплексный проект системы управления автобусами и датчиками с полной аутентификацией, авторизацией, логированием и мониторингом. Курс охватил все ключевые аспекты backend-разработки: архитектуру, базы данных, безопасность и оптимизацию производительности.
2. Что я изучил за курс
2.1 Инструменты и архитектура (Лекции 1-2)
Я освоил экосистему Java-разработчика: VS Code с расширениями, системы сборки (Maven и Gradle), форматы данных (XML, JSON, YAML). Ключевое понимание — различие между фреймворками (Spring вызывает мой код через IoC) и библиотеками (я вызываю их сам). Вторая лекция раскрыла фундамент REST API: HTTP-методы (GET, POST, PUT, DELETE), статус-коды (200, 201, 400, 401, 403, 404, 500) и принцип stateless архитектуры. В своём проекте я реализовал полный CRUD: GET /api/buses, POST /api/buses, PUT /api/buses/{id}, DELETE /api/buses/{id}.
2.2 Spring Core и многопоточность (Лекции 3-4)
Лекция 3 была переломной — я понял суть Spring Core. Инверсия управления (IoC): контейнер Spring создаёт и управляет объектами вместо того, чтобы я создавал их вручную. Это позволяет:
•	Гибкую архитектуру (можно заменить реализацию без изменения кода)
•	Лёгкое тестирование (подмена зависимостей на моки)
Внедрение зависимостей (DI): я использовал три способа — через конструктор (рекомендуется), сеттер и поле (@Autowired). Критичное понимание жизненного цикла бина: создание → внедрение → @PostConstruct → использование → @PreDestroy.
Скоупы и многопоточность: singleton-бины (один экземпляр)  не потокобезопасны, если имеют состояние. Я сделал все сервисы stateless — без изменяемого состояния. Лекция 4 показала, как Tomcat использует thread pool (~50 потоков) для обработки параллельных HTTP-запросов. Race Condition: если singleton-бин меняет свое состояние из разных потоков, результаты непредсказуемы. Решение — использовать AtomicInteger или избегать состояния.
2.3 Spring MVC и CRUD архитектура (Лекции 5-6)
Лекция 5 показала, как Spring обрабатывает HTTP: @RestController (возвращает JSON), @GetMapping/@PostMapping/@PutMapping/@DeleteMapping (маршрутизация), @PathVariable (параметры в URL), @RequestParam (query-параметры), @RequestBody (JSON в тело запроса).
Лекция 6 структурировала знания в трёхслойную архитектуру:
text
Controller → Service → Repository → Database
Я реализовал эту архитектуру: Controller обрабатывает HTTP, Service содержит бизнес-логику (@Transactional), Repository работает с БД (extends JpaRepository). Использовал DTO (Data Transfer Object) для отделения API от БД модели.
2.4 Spring Data JPA и оптимизация (Лекции 7-8)
Лекция 7 раскрыла силу ORM через JPA: @Entity маппирует Java классы на таблицы, JpaRepository генерирует CRUD SQL автоматически. Кастомные методы типа findByBusId() — Spring сам генерирует SQL.
Лекция 8 покрыла критичную N+1 проблему: наивный запрос делает 1 SELECT для Bus'ов и N SELECT'ов для каждого Sensor'а. Решение: @EntityGraph(attributePaths = "sensors") для предзагрузки в одном запросе. Добавил индексы на часто запрашиваемые поля (bus_id, sensor_name) для ускорения SELECT. Использовал пагинацию (Page<T>, Pageable) для больших наборов данных вместо SELECT * из 1млн строк.
2.5 Файлы и экспорт, Security (Лекции 9-10)
Лекция 9 показала, как загружать файлы (MultipartFile) и экспортировать данные. Я реализовал GET /api/reports/export?format=csv — экспорт датчиков в CSV, который клиент скачивает через браузер с правильным Content-Disposition: attachment заголовком.
Лекция 10 была самой объёмной — это фундамент security. JWT (JSON Web Tokens): клиент получает токен при логине, отправляет его в каждом запросе как Authorization: Bearer <token>. Сервер проверяет подпись токена без обращения к БД. Ролевая система (RBAC): role → role_permission → permission. Каждый endpoint защищен через @PreAuthorize("hasAuthority('BUS_DELETE')") — только ADMIN может удалять. SecurityFilterChain конфигурирует правила для всех endpoints.
2.6 Дополнительные практики
Я интегрировал:
•	Логирование (@Slf4j): SLF4J на все методы (log.info, log.warn, log.error)
•	Интеграция Telegram: отправка логов в чат при создании/удалении объектов для мониторинга
•	Swagger/OpenAPI: документация всех endpoints через аннотации @Operation, @ApiResponse
•	Обработка ошибок: @ExceptionHandler для централизованной обработки исключений
•	Git история: регулярные коммиты показывают весь процесс разработки
3. Как применить эти знания в будущем
3.1 Для получения работы
Этот проект — прямое доказательство competentness:
•	REST API разработка — 90% веб-приложений используют REST
•	Spring Boot — стандарт для Java. 70% вакансий junior backend требуют Spring
•	JWT + RBAC — используется везде в production для безопасности
•	JPA + SQL оптимизация — критична для real-world больших БД
•	DevOps practices — логирование, мониторинг, Git история
Я разместу проект на GitHub с полной документацией (Swagger, README, ER-диаграмма БД) как портфолио.
3.2 Для развития — roadmap
Фронт-стек (1-2 недели): React или Vue для полного CRUD веб-приложения
Микросервисы (месяц 2-3): Spring Cloud (Eureka, API Gateway) для распределённых систем
Асинхронность (месяц 3-4): RabbitMQ/Kafka для event-driven архитектуры
Кэширование (месяц 4-5): Redis для ускорения часто запрашиваемых данных
DevOps (месяц 5-6): Docker, Docker Compose, CI/CD с GitHub Actions
Оркестрация (месяц 6-7): Kubernetes для production environment
Результат: от junior backend → senior backend → full-stack (backend + frontend + DevOps).
3.3 Для реальных проектов
Знания напрямую применяются:
•	API дизайн: правильные HTTP методы, версионирование, пагинация
•	Security: JWT для API, bcrypt для паролей, rate limiting от DDoS
•	Performance: индексы БД, пагинация вместо SELECT *, Redis кэширование
•	DevOps: логирование всех операций, health checks, graceful shutdown
4. Источники, использованные при обучении
10 лекций: Инструменты, HTTP/REST, Spring Core (DI/IoC), Веб-серверы, Spring MVC, CRUD архитектура, Spring Data JPA, Оптимизация запросов, Файлы и экспорт, Spring Security.
Официальная документация
•	https://spring.io/projects/spring-boot
•	https://spring.io/projects/spring-data-jpa
•	https://spring.io/projects/spring-security
•	https://www.postgresql.org/docs/
•	https://jwt.io/
Дополнительные ресурсы
•	Baeldung (статьи о Spring, Security, JPA)
•	Stack Overflow (решение конкретных проблем)
•	GitHub (примеры реальных Spring Boot проектов)
•	Postman, Git, IntelliJ IDEA (инструменты разработки)
5. Практические результаты
Я создал полноценное REST API приложение "Система управления автобусами и датчиками":
Полный CRUD (GET, POST, PUT, DELETE)
JWT аутентификация + RBAC авторизация (@PreAuthorize)
Полное логирование (@Slf4j) на все методы
Интеграция Telegram-бота для мониторинга
Экспорт данных в CSV
OpenAPI документация (Swagger UI)
Оптимизация БД (индексы, @EntityGraph, пагинация)
Трёхслойная архитектура: Controller → Service → Repository
Git история с информативными коммитами
Стек: Java 17 + Spring Boot 3.2 + PostgreSQL + Maven + Spring Security (JWT) + SLF4J + Swagger + Telegram API.
6. Заключение
Этот курс дал мне полный цикл backend-разработки: от фундаментальных концепций (IoC, DI, многопоточность) до production-ready features (Security, оптимизация, мониторинг).
Я понимаю:
•	Как работает Spring изнутри (контейнер, lifecycle, scope)
•	Как защитить приложение (JWT, RBAC, хеширование паролей)
•	Как оптимизировать БД (индексы, N+1 решение, пагинация)
•	Как логировать и мониторить (SLF4J, Telegram, Git history)
•	Как проектировать архитектуру (трёхслойная, REST, DTO)
