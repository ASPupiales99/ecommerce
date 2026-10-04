# E-commerce microservices

This repository contains a Spring Boot e-commerce platform composed of independently deployable services. The Kubernetes setup runs the application services, their data stores, messaging, authentication, and observability components.

## Kubernetes architecture

The Kubernetes deployment contains:

|Component|Purpose|Technology|
|---|---|---|
|API Gateway|Public entry point and routing to product, order, and inventory APIs|Spring Cloud Gateway|
|Product service|Product catalog CRUD operations|Spring Boot, Spring MVC, MongoDB|
|Inventory service|Stock management and stock checks|Spring Boot, Spring MVC, Spring Data JPA, MySQL|
|Order service|Authenticated order creation, lookup, history, and deletion|Spring Boot, Spring MVC/WebFlux, Spring Data JPA, PostgreSQL|
|Notification service|Sends confirmation and cancellation emails from order events|Spring Boot, Spring AMQP, Spring Mail|
|Keycloak|OAuth2/OIDC identity provider and JWT issuer|Keycloak|
|RabbitMQ|Asynchronous order, inventory, and notification events|RabbitMQ|
|MongoDB|Product persistence|MongoDB 7|
|MySQL|Inventory persistence|MySQL 8|
|PostgreSQL|Order persistence and Keycloak persistence|PostgreSQL 16|
|Grafana LGTM|Logs, metrics, traces, and dashboards|Grafana OTEL-LGTM|

## Request and event flow

1. Clients call the API Gateway through its NodePort.
2. The gateway routes `/api/v1/product/**`, `/api/v1/order/**`, and `/api/v1/inventory/**` to the corresponding service.
3. Keycloak issues JWTs, and the gateway/order service validate them using the Keycloak issuer.
4. The order service stores orders in PostgreSQL and publishes `order.placed` events through RabbitMQ.
5. The inventory service consumes placed orders, reduces stock, and publishes confirmation or cancellation events.
6. The order and notification services consume the resulting events. The notification service sends confirmation or cancellation email.
7. The order service also has an outbox table and relayer for recovering events that could not be published immediately.

## Service features

### API Gateway

- Routes product, order, and inventory API paths.
- Provides the external application entry point.
- Uses OAuth2 resource-server JWT configuration.
- Exports telemetry to Grafana LGTM.

### Product service

- Lists products.
- Retrieves a product by ID.
- Creates, updates, and deletes products.
- Stores product data in MongoDB.
- Validates request DTOs.

### Inventory service

- Lists inventory records.
- Checks whether a SKU has enough stock.
- Creates, updates, and deletes inventory records.
- Reduces stock after an order is placed.
- Stores inventory data in MySQL.
- Publishes order confirmation or cancellation events.

### Order service

- Creates orders for authenticated users.
- Lists the current user's order history; administrators can access the broader history according to JWT roles.
- Retrieves and deletes orders.
- Stores orders and outbox events in PostgreSQL.
- Calls inventory through the internal service client.
- Publishes order events through RabbitMQ and retries publication through the outbox relayer.

### Notification service

- Consumes order confirmation and cancellation events.
- Sends confirmation and cancellation emails through the configured SMTP provider.

## Technology stack

- Java and Spring Boot
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- Spring Data MongoDB
- Spring Data JPA with MySQL and PostgreSQL
- Spring AMQP and RabbitMQ
- Spring Mail
- Keycloak
- Docker and Docker Compose
- Kubernetes
- OpenTelemetry and Grafana LGTM
- Maven

## Prerequisites

- Docker with a local Kubernetes cluster, such as Docker Desktop Kubernetes, Minikube, or Kind.
- `kubectl` configured for that cluster.
- Java and Maven if building the services locally.
- Docker images built with the exact names and tags used by the manifests:
  - `api-gateway:1.0.0`
  - `product-service:1.0.0`
  - `inventory-service:1.0.0`
  - `order-service:1.0.0`
  - `notification-service:1.0.0`

The application deployments use `imagePullPolicy: Never`, so these images must be available in the Kubernetes node runtime.

## Configure environment values

Never commit actual passwords or SMTP credentials. Start from the example file:

```sh
cp K8s/secrets.env.example K8s/secrets.env
```

Replace every `CHANGE_ME` value in `K8s/secrets.env`. The file is ignored by Git. Create the Kubernetes Secret:

```sh
kubectl create secret generic ecommerce-secrets \
  --from-env-file=K8s/secrets.env
```

For local Compose development, copy the root template instead:

```sh
cp .env.example .env
```

Replace its `CHANGE_ME` values before starting Compose. Do not reuse production credentials for local development.

## Build the Kubernetes application images

Build each service image from its service directory. For example:

```sh
docker build -t api-gateway:1.0.0 ./api-gateway
docker build -t product-service:1.0.0 ./product-service
docker build -t inventory-service:1.0.0 ./inventory-service
docker build -t order-service:1.0.0 ./order-service
docker build -t notification-service:1.0.0 ./notification-service
```

With Minikube, build inside Minikube's Docker daemon or load the images explicitly:

```sh
minikube image load api-gateway:1.0.0
minikube image load product-service:1.0.0
minikube image load inventory-service:1.0.0
minikube image load order-service:1.0.0
minikube image load notification-service:1.0.0
```

## Deploy with Kubernetes

Apply the persistent volume claims, databases, infrastructure, application services, and observability components:

```sh
kubectl apply -f K8s/infrastructure-pvcs.yaml
kubectl apply -f K8s/databases.yaml
kubectl apply -f K8s/rabbitmq-pvc.yaml
kubectl apply -f K8s/rabbitmq-deployment.yaml
kubectl apply -f K8s/rabbitmq-service.yaml
kubectl apply -f K8s/security-observability.yaml
kubectl apply -f K8s/product-deployment.yaml
kubectl apply -f K8s/inventory-deployment.yaml
kubectl apply -f K8s/order-deployment.yaml
kubectl apply -f K8s/notification-deployment.yaml
kubectl apply -f K8s/gateway-deployment.yaml
kubectl apply -f K8s/gateway-service.yaml
```

Check rollout and service status:

```sh
kubectl get pods
kubectl get services
kubectl rollout status deployment/gateway-deployment
kubectl rollout status deployment/product-deployment
kubectl rollout status deployment/inventory-deployment
kubectl rollout status deployment/order-deployment
kubectl rollout status deployment/notification-deployment
```

The gateway is exposed as a NodePort on port `30000`. The exact URL depends on the cluster:

```sh
minikube service gateway-service --url
```

Keycloak, RabbitMQ management, and Grafana use internal ClusterIP services in the Kubernetes manifests. Access them through port forwarding when needed:

```sh
kubectl port-forward service/keycloak 8080:8080
kubectl port-forward service/rabbitmq 15672:15672
kubectl port-forward service/lgtm 3000:3000
```

For production, place the gateway behind an HTTPS ingress or load balancer and configure HTTPS for Keycloak rather than exposing development HTTP traffic.

## Local Docker Compose

The root Compose file starts the supporting infrastructure:

```sh
docker compose --env-file .env up -d
```

The standalone product service Compose file can be run from its directory after creating its local environment file:

```sh
cp product-service/.env.example product-service/.env
docker compose -f product-service/docker-compose.yaml --env-file product-service/.env up -d
```

Compose is intended for local development. Its ports are bound to `127.0.0.1`; do not expose the development services directly to an untrusted network.
