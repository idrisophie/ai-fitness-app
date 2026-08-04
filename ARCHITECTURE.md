# Architecture du Projet AI Power Fitness

## Table des Matières
1. [Architecture Globale](#architecture-globale)
2. [Structure des Services](#structure-des-services)
3. [Design Patterns et Bonnes Pratiques](#design-patterns-et-bonnes-pratiques)
4. [Annotations Spring](#annotations-spring)
5. [DTOs et Mappers](#dtos-et-mappers)
6. [Injection de Dépendances](#injection-de-dépendances)
7. [Relations Controller-Service-Repository](#relations-controller-service-repository)
8. [Arborescence Complète](#arborescence-complète)

---

## Architecture Globale

Le projet suit une **architecture microservices** avec **Spring Cloud Netflix Eureka** pour la découverte de services.

```
┌─────────────────────────────────────┐
│         Eureka Server               │
│    (Service Discovery)              │
│         Port: 8761                  │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        │             │
┌───────▼────────┐  ┌─▼──────────────┐
│ Activity       │  │   User         │
│ Service       │  │   Service      │
│ Port: 8082     │  │   Port: 8081   │
│ MongoDB       │  │   PostgreSQL   │
└────────────────┘  └────────────────┘
```

### Communication Inter-Services
- **Eureka** : Enregistrement et découverte dynamique des services
- **WebClient** : Communication HTTP réactive entre services
- **Load Balancing** : `@LoadBalanced` pour distribution des charges

---

## Structure des Services

### 1. Eureka Server (`eureka/`)

**Rôle** : Service Discovery - Centralise l'enregistrement des microservices

**Configuration** : `application.yml`
```yaml
spring:
  application:
    name: eureka
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false  # Ne s'enregistre pas lui-même
    fetch-registry: false
```

**Package** : `org.idrisophie.eureka.eureka`

**Fichiers** :
- `EurekaApplication.java` : Point d'entrée avec `@EnableEurekaServer`

---

### 2. Activity Service (`activityservice/`)

**Base de données** : MongoDB
**Port** : 8082
**Package** : `org.idrisophie.fitness.activityService`

#### Structure des Packages

```
activityService/
├── config/
│   └── WebClientConfig.java          # Configuration WebClient
├── controllers/
│   └── ActivityController.java       # API REST
├── dto/
│   ├── ActivityRequest.java          # DTO entrée
│   └── ActivityResponse.java         # DTO sortie
├── exceptions/
│   ├── DuplicateResourceException.java
│   └── ResourceNotFoundException.java
├── mappers/
│   └── ActivityMapper.java          # MapStruct
├── models/
│   ├── Activity.java                 # Entity MongoDB
│   └── ActivityType.java             # Enum
├── repositories/
│   └── ActivityRepository.java       # MongoRepository
└── services/
    ├── ActivityService.java          # Interface
    ├── ActivityServiceDefault.java   # Implémentation
    └── UserValidationService.java    # Validation utilisateur
```

#### Flux de Données

```
Client HTTP 
    ↓
ActivityController (@RestController)
    ↓
ActivityServiceDefault (@Service)
    ↓
ActivityMapper (MapStruct)
    ↓
ActivityRepository (MongoRepository)
    ↓
MongoDB
```

---

### 3. User Service (`userservice/userservice/`)

**Base de données** : PostgreSQL
**Port** : 8081
**Package** : `org.idrisophie.fitness.userservice`

#### Structure des Packages

```
userservice/
├── controllers/
│   └── UserController.java          # API REST
├── dto/
│   ├── RegistreRequest.java         # DTO inscription
│   └── UserResponse.java            # DTO réponse
├── exceptions/
│   ├── DuplicateResourceException.java
│   └── ResourceNotFoundException.java
├── mappers/
│   └── UserMapper.java              # MapStruct
├── models/
│   ├── User.java                    # Entity JPA
│   └── UserRole.java                # Enum
├── repositories/
│   └── UserRepository.java          # JpaRepository
└── services/
    ├── UserService.java             # Interface
    └── UserServiceDefault.java      # Implémentation
```

#### Flux de Données

```
Client HTTP 
    ↓
UserController (@RestController)
    ↓
UserServiceDefault (@Service)
    ↓
UserMapper (MapStruct)
    ↓
UserRepository (JpaRepository)
    ↓
PostgreSQL
```

---

## Design Patterns et Bonnes Pratiques

### 1. Pattern Repository

**Utilisation** : Abstraction de l'accès aux données

```java
// ActivityRepository
public interface ActivityRepository extends MongoRepository<Activity, String> {
    List<Activity> findByUserId(String userId);
}

// UserRepository
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
}
```

**Avantages** :
- Séparation des responsabilités
- Méthodes CRUD automatiques
- Requêtes personnalisées par nom de méthode

---

### 2. Pattern Service Layer

**Utilisation** : Logique métier séparée du contrôleur

```java
// Interface
public interface ActivityService {
    ActivityResponse trackActivity(ActivityRequest request);
}

// Implémentation
@Service
@RequiredArgsConstructor
public class ActivityServiceDefault implements ActivityService {
    private final ActivityRepository repository;
    private final ActivityMapper activityMapper;
    
    public ActivityResponse trackActivity(ActivityRequest request){
        Activity activity = activityMapper.toEntity(request);
        Activity savedActivity = repository.save(activity);
        return activityMapper.toResponse(savedActivity);
    }
}
```

**Avantages** :
- Testabilité (mock facile)
- Réutilisabilité
- Séparation des préoccupations

---

### 3. Pattern DTO (Data Transfer Object)

**Utilisation** : Séparation des modèles de données internes et externes

```java
// DTO Request (données entrantes)
@Data
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}

// DTO Response (données sortantes)
@Data
public class ActivityResponse {
    private String id;
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> addtionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Avantages** :
- Contrôle des données exposées
- Validation des entrées
- Indépendance du modèle de base de données

---

### 4. Pattern Mapper (MapStruct)

**Utilisation** : Conversion automatique DTO ↔ Entity

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ActivityMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Activity toEntity(ActivityRequest request);
    
    @Mapping(source = "addtionalMetrics", target = "addtionalMetrics")
    ActivityResponse toResponse(Activity activity);
}
```

**Avantages** :
- Performance (compilation)
- Type-safe
- Maintenance facile

---

### 5. Pattern Exception Custom

**Utilisation** : Gestion d'erreurs métier spécifiques

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

**Avantages** :
- Gestion d'erreurs spécifique
- Messages d'erreur clairs
- Différenciation des types d'erreurs

---

## Annotations Spring

### Annotations de Configuration

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@SpringBootApplication` | Point d'entrée de l'application | Classe principale |
| `@Configuration` | Classe de configuration | `WebClientConfig` |
| `@Bean` | Déclaration d'un bean Spring | Méthodes de configuration |
| `@EnableEurekaServer` | Active le serveur Eureka | EurekaApplication |

### Annotations de Composants

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@RestController` | Contrôleur REST | `ActivityController` |
| `@Service` | Couche service | `ActivityServiceDefault` |
| `@Repository` | Couche repository | `UserRepository` |
| `@Component` | Composant générique | Non utilisé (remplacé par annotations spécifiques) |

### Annotations de Mapping Web

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@RequestMapping` | Mapping de base du contrôleur | `/api/activies` |
| `@GetMapping` | Endpoint GET | `getUserActivities()` |
| `@PostMapping` | Endpoint POST | `trackActivity()` |
| `@PathVariable` | Paramètre dans l'URL | `/{activityId}` |
| `@RequestBody` | Corps de la requête | `ActivityRequest` |
| `@RequestHeader` | Header HTTP | `X-User-ID` |
| `@Valid` | Validation des données | `RegistreRequest` |

### Annotations de Data

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@Entity` | Entity JPA | `User` |
| `@Table` | Table de base de données | `users` |
| `@Document` | Document MongoDB | `Activity` |
| `@Id` | Identifiant primaire | `id` |
| `@GeneratedValue` | Génération automatique | `UUID` |
| `@Column` | Configuration colonne | `unique`, `nullable` |
| `@Enumerated` | Enum en base de données | `UserRole` |
| `@CreatedDate` | Date de création automatique | `createdAt` |
| `@LastModifiedDate` | Date de modification automatique | `updatedAt` |

### Annotations Lombok

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@Data` | Getters, Setters, toString, equals, hashCode | Tous les modèles |
| `@Builder` | Pattern Builder | `Activity` |
| `@NoArgsConstructor` | Constructeur sans arguments | `User`, `Activity` |
| `@AllArgsConstructor` | Constructeur avec tous les arguments | `User`, `Activity` |
| `@RequiredArgsConstructor` | Constructeur avec champs finaux | Services |

### Annotations de Validation

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@NotBlank` | Champ non vide | `email`, `password` |
| `@Email` | Format email valide | `email` |
| `@Size` | Taille minimale/maximale | `password (min=6)` |

### Annotations MapStruct

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@Mapper` | Interface de mapping | `ActivityMapper` |
| `@Mapping` | Configuration de mapping spécifique | `ignore`, `source`, `target` |

### Annotations Cloud

| Annotation | Utilisation | Exemple |
|------------|-------------|---------|
| `@LoadBalanced` | Load balancing côté client | `WebClient.Builder` |

---

## DTOs et Mappers

### DTOs (Data Transfer Objects)

Les DTOs servent d'interface entre le client et le service, séparant le modèle de données interne de l'API exposée.

#### Activity Service DTOs

**ActivityRequest** (Entrée)
```java
@Data
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
```

**ActivityResponse** (Sortie)
```java
@Data
public class ActivityResponse {
    private String id;                    // Généré par MongoDB
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> addtionalMetrics;
    private LocalDateTime createdAt;      // Automatique
    private LocalDateTime updatedAt;      // Automatique
}
```

#### User Service DTOs

**RegistreRequest** (Entrée avec validation)
```java
@Data
public class RegistreRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be atleast of 6 characters")
    private String password;
    private String firstName;
    private String lastName;
}
```

**UserResponse** (Sortie)
```java
@Data
public class UserResponse {
    private String id;                   // Généré par UUID
    private String email;
    private String password;             // ⚠️ Devrait être masqué en production
    private String firstName;
    private String lastName;
    private UserRole role = UserRole.USER;
    private LocalDateTime createdAt;      // Automatique
    private LocalDateTime updatedAt;      // Automatique
}
```

### Mappers (MapStruct)

Les mappers convertissent automatiquement les DTOs en Entities et vice-versa.

#### ActivityMapper

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ActivityMapper {
    
    // Request → Entity (ignore les champs auto-générés)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Activity toEntity(ActivityRequest request);
    
    // Entity → Response
    @Mapping(source = "addtionalMetrics", target = "addtionalMetrics")
    ActivityResponse toResponse(Activity activity);
}
```

#### UserMapper

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    
    // Request → Entity (ignore les champs auto-générés)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegistreRequest request);
    
    // Entity → Response
    @Mapping(source = "password", target = "password")
    UserResponse toResponse(User user);
}
```

**Configuration MapStruct** :
- `componentModel = "spring"` : Génère un bean Spring injectable
- `nullValuePropertyMappingStrategy = IGNORE` : Ignore les valeurs nulles lors du mapping

---

## Injection de Dépendances

### Constructor Injection (Recommandée)

**Utilisation de `@RequiredArgsConstructor` (Lombok)**

```java
@Service
@RequiredArgsConstructor  // Génère le constructeur avec les champs finaux
public class ActivityServiceDefault implements ActivityService {
    private final ActivityRepository repository;
    private final ActivityMapper activityMapper;
    
    // Constructeur généré automatiquement :
    // public ActivityServiceDefault(ActivityRepository repository, ActivityMapper activityMapper) {
    //     this.repository = repository;
    //     this.activityMapper = activityMapper;
    // }
}
```

**Avantages** :
- Immutabilité (champs finaux)
- Testabilité facile
- Détection des dépendances manquantes à la compilation

### Field Injection (Déconseillée)

```java
@Service
public class UserServiceDefault implements UserService {
    @Autowired  // ⚠️ Déconseillé
    private UserRepository repository;
    
    @Autowired  // ⚠️ Déconseillé
    private UserMapper userMapper;
}
```

**Problèmes** :
- Difficile à tester
- Cache les dépendances
- Immutabilité impossible

### Bean Configuration

**WebClientConfig** : Création de beans WebClient

```java
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced  // Active le load balancing côté client
    public WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder webClientBuilder){
        return webClientBuilder
                .baseUrl("http://user-service")  // Nom du service Eureka
                .build();
    }
}
```

**Injection dans UserValidationService** :

```java
@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;  // Injecté automatiquement
}
```

---

## Relations Controller-Service-Repository

### Architecture en Couches

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (Gère les requêtes HTTP)                │
│  @RestController                         │
└──────────────┬──────────────────────────┘
               │ DTO
┌──────────────▼──────────────────────────┐
│         Service Layer                   │
│  (Logique métier)                       │
│  @Service                               │
└──────────────┬──────────────────────────┘
               │ Entity
┌──────────────▼──────────────────────────┐
│         Repository Layer                │
│  (Accès aux données)                    │
│  @Repository / extends JpaRepository    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Database                       │
│  (MongoDB / PostgreSQL)                 │
└─────────────────────────────────────────┘
```

### Exemple Complet : Activity Service

#### 1. Controller (Reçoit la requête)

```java
@RestController
@RequestMapping("/api/activies")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityServiceDefault activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request){
        return ResponseEntity.ok(activityService.trackActivity(request));
    }
}
```

#### 2. Service (Logique métier)

```java
@Service
@RequiredArgsConstructor
public class ActivityServiceDefault implements ActivityService {

    private final ActivityRepository repository;
    private final ActivityMapper activityMapper;
    
    public ActivityResponse trackActivity(ActivityRequest request){
        // Conversion DTO → Entity
        Activity activity = activityMapper.toEntity(request);
        
        // Sauvegarde en base
        Activity savedActivity = repository.save(activity);
        
        // Conversion Entity → DTO
        return activityMapper.toResponse(savedActivity);
    }
}
```

#### 3. Repository (Accès données)

```java
public interface ActivityRepository extends MongoRepository<Activity, String> {
    List<Activity> findByUserId(String userId);
}
```

### Exemple Complet : User Service

#### 1. Controller

```java
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registre(@Valid @RequestBody RegistreRequest request){
        return ResponseEntity.ok(userService.registre(request));
    }
}
```

#### 2. Service

```java
@Service
public class UserServiceDefault implements UserService {
    @Autowired
    private UserRepository repository;
    
    @Autowired
    private UserMapper userMapper;
    
    public UserResponse registre(RegistreRequest request){
        // Validation métier
        if(repository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email already exist!");
        }
        
        // Conversion et sauvegarde
        User user = userMapper.toEntity(request);
        User savedUser = repository.save(user);
        
        return userMapper.toResponse(savedUser);
    }
}
```

#### 3. Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
}
```

---

## Arborescence Complète

```
Ai-Power-Fitness-Application/
├── .gitignore
├── plan.md
├── notes.md
├── ARCHITECTURE.md                    # Ce document
│
├── activityservice/                   # Service d'activités
│   ├── pom.xml                        # Dépendances Maven
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── org/idrisophie/fitness/activityService/
│   │   │   │       ├── ActivityServiceApplication.java
│   │   │   │       ├── config/
│   │   │   │       │   └── WebClientConfig.java
│   │   │   │       ├── controllers/
│   │   │   │       │   └── ActivityController.java
│   │   │   │       ├── dto/
│   │   │   │       │   ├── ActivityRequest.java
│   │   │   │       │   └── ActivityResponse.java
│   │   │   │       ├── exceptions/
│   │   │   │       │   ├── DuplicateResourceException.java
│   │   │   │       │   └── ResourceNotFoundException.java
│   │   │   │       ├── mappers/
│   │   │   │       │   └── ActivityMapper.java
│   │   │   │       ├── models/
│   │   │   │       │   ├── Activity.java
│   │   │   │       │   └── ActivityType.java
│   │   │   │       ├── repositories/
│   │   │   │       │   └── ActivityRepository.java
│   │   │   │       └── services/
│   │   │   │           ├── ActivityService.java
│   │   │   │           ├── ActivityServiceDefault.java
│   │   │   │           └── UserValidationService.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│
├── eureka/                            # Service Discovery
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── org/idrisophie/eureka/eureka/
│   │   │   │       └── EurekaApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│
└── userservice/                       # Service utilisateurs
    └── userservice/
        ├── pom.xml
        ├── src/
        │   ├── main/
        │   │   ├── java/
        │   │   │   └── org/idrisophie/fitness/userservice/
        │   │   │       ├── UserserviceApplication.java
        │   │   │       ├── controllers/
        │   │   │       │   └── UserController.java
        │   │   │       ├── dto/
        │   │   │       │   ├── RegistreRequest.java
        │   │   │       │   └── UserResponse.java
        │   │   │       ├── exceptions/
        │   │   │       │   ├── DuplicateResourceException.java
        │   │   │       │   └── ResourceNotFoundException.java
        │   │   │       ├── mappers/
        │   │   │       │   └── UserMapper.java
        │   │   │       ├── models/
        │   │   │       │   ├── User.java
        │   │   │       │   └── UserRole.java
        │   │   │       ├── repositories/
        │   │   │       │   └── UserRepository.java
        │   │   │       └── services/
        │   │   │           ├── UserService.java
        │   │   │           └── UserServiceDefault.java
        │   │   └── resources/
        │   │       └── application.yml
        │   └── test/
```

---

## Résumé des Bonnes Pratiques Appliquées

### ✅ Appliquées
1. **Architecture en couches** : Controller → Service → Repository
2. **DTOs** : Séparation modèle interne / API
3. **MapStruct** : Mapping type-safe et performant
4. **Constructor Injection** : Via `@RequiredArgsConstructor`
5. **Exceptions personnalisées** : Gestion d'erreurs métier
6. **Validation** : Annotations Jakarta Validation
7. **Service Discovery** : Eureka pour microservices
8. **Load Balancing** : `@LoadBalanced` WebClient
9. **Lombok** : Réduction du boilerplate
10. **Interfaces Service** : Pour testabilité

### ❌ Supprimées (Redondantes)
1. **Factories** : Remplacées par MapStruct mappers
2. **Constructeurs manuels** : Remplacés par Lombok
3. **MongoConfig vide** : Configuration automatique Spring Boot

### 🔧 Corrigées
1. **UserValidationService** : Suppression constructeur en double
2. **WebClientConfig** : Ajout `@Bean`, correction nom service
3. **User model** : Ajout constructeurs Lombok
4. **Exceptions** : Utilisation cohérente des exceptions personnalisées

---

## Technologies Utilisées

- **Spring Boot 3.3.2** : Framework principal
- **Spring Cloud 2023.0.3** : Microservices
- **Spring Data JPA** : PostgreSQL
- **Spring Data MongoDB** : MongoDB
- **MapStruct 1.6.0** : Mapping DTO/Entity
- **Lombok** : Réduction boilerplate
- **Jakarta Validation** : Validation des données
- **Eureka Server** : Service Discovery
- **WebFlux** : WebClient réactif
- **Java 17** : Version Java
- **PostgreSQL** : Base de données User Service
- **MongoDB** : Base de données Activity Service
