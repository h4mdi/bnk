# eBank Kubernetes Deployment

This document describes the architecture and deployment process for the eBank application on Kubernetes.

Given That the architecture is resource-intensive and time-consuming to build, we retained only the basic components ( Keycloak, Postgres, Eureka, Spring Api Gateway, **User Service**)

## Architecture Architecture

The application is deployed as a set of microservices in the `ebank` namespace.

### Components

1.  **Infrastructure**:
    *   **PostgreSQL**: Database for persisting service data.
    *   **Keycloak**: Identity and Access Management (Authentication/Authorization).
        *   Configured with `ebank` realm and necessary clients.
        *   Exposed via **NodePort** (AWS Academy restriction).
    *   **Discovery Server**: Service Registry (Eureka).

2.  **API Gateway**:
    *   Entry point for the application.
    *   Routes requests to microservices.
    *   Exposed via **NodePort** (AWS Academy restriction).

3.  **Microservices**:
    *   **User Service**: Manages user profiles and data.
        *   Image: `h4mdi/ebank-user:latest`
        *   Connects to Postgres (`ebank_users` DB) and Eureka.

> [!NOTE]
> All services run in the `ebank` namespace.

## Prerequisites

*   A running Kubernetes cluster (e.g., AWS Academy).
*   `kubectl` configured to communicate with the cluster.

## Deployment Steps

Follow these steps to deploy the application.

### 1. Create Namespace & Infrastructure

Deploy the database, identity provider, and namespace resources.

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Deploy Keycloak (with auto-imported config)
kubectl apply -f k8s/keycloak/keycloak.yaml

# Deploy Postgres
kubectl apply -f k8s/postgres/ 
```

### 2. Deploy Discovery & Gateway

```bash
# Deploy Discovery Server
kubectl apply -f k8s/services/discoveryserver.yaml

# Deploy Gateway
kubectl apply -f k8s/services/gateway.yaml
```

### 3. Deploy User Service

```bash
kubectl apply -f k8s/services/userservice.yaml
```

## Accessing the Application

Since LoadBalancers are restricted, services are exposed via **NodePort**.

1.  **Find the Node Ports**:

    ```bash
    kubectl get svc -n ebank
    ```

    Look for the `PORT(S)` column for `gateway` and `keycloak`. It will look like `8080:3xxxx/TCP`. The `3xxxx` is your NodePort.

2.  **Use the public IPv4 address** combined with the port to test access: 
- Access URLs:
    *   **Gateway**: `http://<NodeIP>:<GatewayNodePort>`
    *   **Keycloak**: `http://<NodeIP>:<KeycloakNodePort>`

## Verification

Check the status of all pods in the `ebank` namespace:

```bash
kubectl get pods -n ebank
```

You should see:
*   `discoveryserver` (Running)
*   `gateway` (Running)
*   `keycloak` (Running)
*   `userservice` (Running)
*   `postgres` (Running - if deployed)
