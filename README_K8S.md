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

## API Authentication & Testing

The APIs are secured with Keycloak. You need an Access Token to call them.

### 1. Generate Access Token (PowerShell)

Replace `<Keycloak-IP>` and `<Keycloak-Port>` with your actual values (e.g. `54.162.24.33:30381`).

```powershell
$uri = "http://<Keycloak-IP>:<Keycloak-Port>/realms/ebank/protocol/openid-connect/token"

$body = @{
  grant_type    = "password"
  client_id     = "gateway"
  client_secret = "GehfUy2Oy0wr3n6LOBwACF7RrO3zhpUK"
  username      = "ADMIN"
  password      = "ADMIN"
}

$response = Invoke-RestMethod `
  -Method Post `
  -Uri $uri `
  -ContentType "application/x-www-form-urlencoded" `
  -Body $body

$accessToken = $response.access_token
Write-Output $accessToken
```

### 2. Create User (POST)

Use **Postman** to create a user.

*   **URL**: `http://<Gateway-IP>:<Gateway-Port>/api/users` (e.g. `http://54.162.24.33:31806/api/users`)
*   **Method**: `POST`
*   **Auth**: Bearer Token (Paste the `$accessToken` from the step above)
*   **Body** (JSON):

```json
{
    "firstName": "Alice",
    "lastName": "smith",
    "email": "alice.smith@ebank.local",
    "username": "alice",
    "phoneNumber": "+0112345678",
    "address": "Tunis, Tunisia",
    "role": "USER",
    "active": true
}
```

### 3. Get User List (GET)

*   **URL**: `http://<Gateway-IP>:<Gateway-Port>/api/users`
*   **Method**: `GET`
*   **Auth**: Bearer Token (Same token)

**Expected Result**:

```json
[
    {
        "id": 1,
        "firstName": "Alice",
        "lastName": "smith",
        "email": "alice.smith@ebank.local",
        "active": true,
        "address": "Tunis, Tunisia",
        "phoneNumber": "+0112345678",
        "role": "USER",
        "username": "alice",
        "createdAt": "2026-01-22T21:23:37.32357",
        "updatedAt": "2026-01-22T21:23:37.323658"
    }
]
```
