# Awesome Pizza

REST API to manage the orders of the **Awesome Pizza** pizzeria.

- The **customer** places an order without signing up, gets an **order code** and uses it to track the order until it is ready.
- The **pizzaiolo** sees the queue of orders, takes **one order at a time**, marks it as ready and moves on to the next one.

**Stack:** Java 21 · Spring Boot 3.5 (Web, Data JPA, Validation) · H2 in-memory database · springdoc-openapi · JUnit 5, Mockito, AssertJ

## Running the application

Only JDK 21 is required: Maven is downloaded by the wrapper.

```bash
./mvnw spring-boot:run
./mvnw test
```

| URL | What it is |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API documentation: every endpoint can be tried from here |
| http://localhost:8080/h2-console | Database console (JDBC URL `jdbc:h2:mem:awesomepizza`, user `sa`, no password) |

The database is in memory: it is created at startup, filled with the menu from `data.sql` and cleared when the application stops.

## API

| Method | Endpoint | Who | Description |
|---|---|---|---|
| GET  | `/api/menu` | customer | Pizzas available to order |
| POST | `/api/orders` | customer | Places an order → `201` with the order code |
| GET  | `/api/orders/{code}` | customer | Status of an order |
| GET  | `/api/pizzaiolo/queue` | pizzaiolo | `PENDING` orders, oldest first |
| GET  | `/api/pizzaiolo/current` | pizzaiolo | Order in preparation (`204` if none) |
| POST | `/api/pizzaiolo/next` | pizzaiolo | Takes the next order (`204` if the queue is empty, `409` if one is already in preparation) |
| POST | `/api/pizzaiolo/orders/{code}/ready` | pizzaiolo | Marks the order as ready |

Errors use the standard **ProblemDetail** format (RFC 9457).