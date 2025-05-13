# Crewmeister Coding Challenge

This repository contains a Spring Boot application developed as part of a take-home assignment for Crewmeister. The application fetches historical exchange rates (EUR base) from the [Bundesbank](https://www.bundesbank.de) and stores them in a local database.

---

## 🚀 Features Implemented

- Fetch and store exchange rates for all supported currencies from the Bundesbank public API.
- At application startup, a `@PostConstruct` method automatically fetches exchange rates and stores them in the database.
- A scheduled job runs every week at 6 AM to check for new currencies and fetch exchange rates to store them.
- API endpoints to:
    - List supported currencies.
    - Fetch all exchange rates
    - Fetch exchange rates for a given date.
    - Convert foreign currency to EUR for a given date.
- In-memory H2 database used for simplicity.
- If the application restarts and data is lost (due to H2 volatility), an /api/save API is available to re-trigger the data fetch and persist it again.
- Both unit and integration test cases are implemented.
- Dockerized application with Kubernetes support for local development.
- Postman collection included for easy testing of APIs.
- JaCoCo plugin integrated with 90%+ unit test coverage.

---

## 📦 Tech Stack

- Java 11
- Spring Boot 2.4.1
- Spring Data JPA
- H2 Database (in-memory)
- Maven
- Docker & Kubernetes (local setup via Docker Desktop)
- JUnit 5 + Mockito (for testing)
- JaCoCo (code coverage)

---

## 🛠️ How to Clone the Repository
git clone https://github.com/Prajakta06021994/crewmeister-coding-challenge.git

cd crewmeister-coding-challenge

🧪 How to Run Locally (Dev Mode)
# Build and run the application
./mvnw spring-boot:run

Once the application starts:
A scheduled job will run automatically.
It fetches exchange rate data from the external API and stores it in the H2 database.
If data is lost (e.g., after restart), you can trigger the fetch manually using the /api/fetch endpoint.

# Testing & Coverage
Run Tests and Generate Report

./mvnw clean verify

View Coverage Report (JaCoCo)

open target/site/jacoco/index.html

✅ Achieved 90%+ unit test coverage

# Running with Docker

✅ Prerequisites:

To create jar :- ./mvnw clean package

Docker Desktop

Enable Kubernetes support in Docker settings (if running via Kubernetes)

🐳 Build and Run Container

Build Docker image

docker build -t fx-app:latest .

# Running with Kubernetes

Kubernetes manifests provided in k8s/ directory.

kubectl apply -f k8s/deployment.yaml

kubectl apply -f k8s/service.yaml

kubectl apply -f k8s/configmap.yaml 

kubectl get svc fx-service 

Access the application
  
kubectl port-forward service/fx-service 8080:8080

# Postman Collection
A Postman collection is provided in the repo:

📁 /postman/CrewmeisterFX.postman_collection.json

# Import it into Postman to test all available endpoints.

🔗 Useful Endpoints

Endpoint	                                                                Description

GET /api/currencies                                                    - List all currencies

GET /api/exchange-rates                                                - List all exchange rates

GET /api/exchange-rates?date=YYYY-MM-DD	                               - Fetch all exchange rates for a specific date

GET /api/convert?currency=USD&amount=100&date=YYYY-MM-DD	           - Convert amount to EUR on a specific date

POST /api/save	                                                       - Trigger manual data fetch from Bundesbank

