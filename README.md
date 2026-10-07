# AI Power Fitness – Plateforme de Suivi d'Activités avec IA

Application microservices pour le suivi d'activités fitness avec recommandations personnalisées générées par IA (architecture event-driven).

## 🏗️ Architecture

┌─────────────┐ ┌──────────────┐ ┌─────────────┐
│ Eureka Server│────▶│ Config Server │────▶│ API Gateway │
└─────────────┘ └──────────────┘ └──────┬──────┘
│
┌─────────────────────────────┼─────────────────────┐
▼ ▼ ▼
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ User Service │ │ Activity Service │ │ AI Service │
│ (PostgreSQL) │ │ (MongoDB) │ │ (Gemini API) │
└─────────────────┘ └────────┬──────────┘ └────────▲─────────┘
│ │
└──────── RabbitMQ ────┘
(Exchange Direct, Queue durable)

                 Authentification centralisée : Keycloak (OAuth2/OIDC, JWT)

## 🧩 Services

| Service | Rôle | Stack |
|---|---|---|
| **Eureka Server** | Service Discovery | Spring Cloud Netflix |
| **Config Server** | Configuration centralisée | Spring Cloud Config |
| **API Gateway** | Point d'entrée unique, routage | Spring Cloud Gateway |
| **User Service** | Gestion des utilisateurs | Spring Boot, PostgreSQL |
| **Activity Service** | Enregistrement des activités | Spring Boot, MongoDB |
| **AI Service** | Recommandations personnalisées | Spring Boot, Google Gemini API |

## 📨 Communication asynchrone

- **RabbitMQ** (Exchange Direct + Queue durable) découple l'**Activity Service** de l'**AI Service**.
- Flux : une activité enregistrée publie un événement → l'AI Service consomme le message → génère une recommandation via Gemini.

## 🔐 Sécurité

- **Keycloak** pour l'authentification centralisée (OAuth2/OIDC)
- Jetons **JWT** propagés via l'API Gateway vers les microservices

## 🛠️ Stack technique

**Backend**
- Spring Boot, Spring Cloud (Eureka, Config, Gateway)
- Spring Data JPA / Spring Data MongoDB
- RabbitMQ
- Keycloak (OAuth2/OIDC)
- Google Gemini API

**Frontend**
- React

**Bases de données**
- PostgreSQL (User Service)
- MongoDB (Activity Service)

## 🚀 Démarrage

### Prérequis
- Java 17+
- Maven
- Docker & Docker Compose (RabbitMQ, PostgreSQL, MongoDB, Keycloak)
- Node.js 18+ (frontend React)

### Lancement

```bash
# 1. Infrastructure (RabbitMQ, bases de données, Keycloak)
docker-compose up -d

# 2. Services (ordre important)
cd eureka-server && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd activity-service && mvn spring-boot:run
cd ai-service && mvn spring-boot:run

# 3. Frontend
cd frontend
npm install
npm run dev
```

### Variables d'environnement clés

```env
# AI Service
GEMINI_API_KEY=your_api_key

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672

# Keycloak
KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080
KEYCLOAK_REALM=fitness-ai
```

## 📂 Structure du projet

ai-power-fitness/
├── eureka-server/
├── config-server/
├── api-gateway/
├── user-service/
├── activity-service/
├── ai-service/
├── frontend/
└── docker-compose.yml


## 📌 Points clés d'architecture

- **Event-driven** : découplage Activity/AI via RabbitMQ pour éviter les appels synchrones bloquants vers l'API Gemini.
- **Scalabilité** : chaque service est indépendant et peut être scalé individuellement.
- **Résilience** : Eureka + Gateway permettent le load balancing et la tolérance aux pannes.