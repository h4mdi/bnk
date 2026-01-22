# eBank

Banking application built with Spring Boot microservices.

## Setup

You'll need Docker and Docker Compose installed. Java 21 and Maven are required if you want to run services locally.

## Getting Started

Just run:

```bash
docker-compose up -d
```

This starts everything - PostgreSQL, Keycloak, Kafka, Eureka, the gateway, and all the microservices. Give it a minute or two to fully start up.

Once it's running, you can access:
- API Gateway: http://localhost:8080
- Keycloak: http://localhost:8090 (admin/admin)
- Eureka: http://localhost:8761
- Kafka UI: http://localhost:8091

## Databases

PostgreSQL creates the databases automatically on startup. Each service has its own database (ebank_users, ebank_accounts, etc).

## Authentication

Uses Keycloak for auth. Set up a realm called `ebank` and configure a client for the gateway. All API calls need a JWT token in the Authorization header.

## Running Services Locally

If you prefer running services from your IDE:

1. Start the infrastructure first:
```bash
docker-compose up -d postgres keycloak zookeeper kafka discoveryserver
```

2. Then run each service individually. Use `application.properties` (not the docker one) when running locally.

## Services

- userservice, accountservice, cardservice, transactionservice, notificationservice
- gateway (port 8080)
- discoveryserver (Eureka on 8761)

## Shutting Down

```bash
docker-compose down
```

Add `-v` if you want to remove volumes too.
