# Task Management System

Task management system built with:
- **Spring Boot (Java 21)**
- **JPA / Hibernate**
- **PostgreSQL**
- **Docker**
- **JUnit**
- **Mockito**
- **testContainers**
- **API documentation - Open API / Swagger**


## Features


- CRUD operations for tasks (Create, Read, Update, Delete)
- DTO-based request and response mapping for clean separation
- Input validation with `jakarta.validation`
- Global CORS configuration for frontend-backend communication
- OpenAPI 3.0 documentation via `springdoc-openapi`
- Unit and integration testing with JUnit + MockMvc + testcontainers

## Prerequisites
Before running the project make sure you have:
- [Docker](https://docs.docker.com/get-docker/) ≥ 20.x
- [Docker Compose](https://docs.docker.com/compose/install/) ≥ 2.x

## Getting Started
- **clone the repository**
```bash
git clone https://github.com/your-username/task-management-api.git
 task-management-api
```
- **create an .env file**
- create .env file with followings

```
  POSTGRES_USER=devuser
  POSTGRES_PASSWORD=devpassword
  POSTGRES_DB=postgres

```
- **Build and start containers**
```
docker compose up --build
```
## API documentation
Once the app is running:

**Swagger UI** : http://localhost:4000/swagger-ui.html

**OpenAPI JSON** : http://localhost:4000/v3/api-docs


