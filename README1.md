Crewmeister Test Assignment - Java Backend Developer

# Crewmeister Coding Challenge

This repository contains a Spring Boot application developed as part of a take-home assignment for Crewmeister. The application fetches historical exchange rates (EUR base) from the [Bundesbank](https://www.bundesbank.de) and stores them in a local database.

---

## 🚀 Features Implemented

- Fetch and store exchange rates for all supported currencies from the Bundesbank public API.
- Scheduler runs on application startup to retrieve and persist the latest exchange rates.
- API endpoints to:
    - List supported currencies.
    - Fetch all exchange rates
    - Fetch exchange rates for a given date.
    - Convert foreign currency to EUR for a given date.
- In-memory H2 database used for simplicity.
- If the application restarts and data is lost (due to H2 volatility), an API is available to re-trigger the data fetch and persist it again.
- Dockerized application with Kubernetes support for local development.
- Postman collection included for easy testing of APIs.

---

## 📦 Tech Stack

- Java 11
- Spring Boot 2.4.1
- Spring Data JPA
- H2 Database (in-memory)
- Maven
- Docker & Kubernetes
- JUnit 5 + Mockito (for testing)

---

## 🛠️ How to Clone the Repository

git clone https://github.com/your-username/cm-coding-challenge.git
cd cm-coding-challenge

🧪 How to Run Locally (Dev Mode)
# Build and run the application
./mvnw spring-boot:run

# Or package and run to create jar
./mvnw clean package 

Once the application starts:

A scheduled job will run automatically.

It fetches exchange rate data from the external API and stores it in the H2 database.

If data is lost (e.g., after restart), you can trigger the fetch manually using the /api/fetch endpoint.

🐳 Running with Docker
✅ Prerequisites:
Docker Desktop
Enable Kubernetes support in Docker settings (if running via Kubernetes)

Build and Run Container
# Build Docker image
docker build -t fx-app:latest .

☸️ Running with Kubernetes
Kubernetes manifests provided in k8s/ directory.

kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/configmap.yaml 

 kubectl get svc fx-service 

# Access the application
kubectl port-forward service/fx-service 8080:8080
📮 Postman Collection
A Postman collection is provided in the repo:
https://.postman.co/workspace/My-Workspace~13364efd-5ffb-403d-8cc4-4811cbba9baf/collection/40244431-8b630269-8a3f-4011-9961-b2f933bfaf23?action=share&creator=40244431
📁 postman/CrewmeisterFX.postman_collection.json

Import it into Postman to test all available endpoints.

🔗 Useful Endpoints
Endpoint	                                                Description
GET /api/currencies                                         List all currencies
GET /api/exchange-rates                                     List all exchange rates
GET /api/exchange-rates?date=YYYY-MM-DD	                    Fetch all exchange rates for a specific date
GET /api/convert?currency=USD&amount=100&date=YYYY-MM-DD	Convert amount to EUR on a specific date
POST /api/fetch	                                            Trigger manual data fetch from Bundesbank

