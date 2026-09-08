# Concepts et Design Patterns - Projet Fitness

## 1. Idempotence

### Qu'est-ce que l'Idempotence ?

**Définition :** Une opération est idempotente si elle produit le même résultat quel que soit le nombre de fois qu'elle est exécutée.

**Mathématiquement :** `f(x) = f(f(x)) = f(f(f(x)))`

### Pourquoi l'Idempotence ?

**Problème :**
- **Réseau instable** : Un client peut renvoyer la même requête (timeout, retry)
- **RabbitMQ redelivery** : Un message peut être délivré plusieurs fois
- **Double clic utilisateur** : Le même formulaire soumis deux fois

**Conséquences sans idempotence :**
- Duplication de données (activités en double)
- Incohérence de l'état
- Erreurs métier (paiement en double)

### Quand utiliser l'Idempotence ?

**Obligatoire :**
- **Opérations de création** : POST qui crée des ressources
- **Message consumers** : RabbitMQ @RabbitListener
- **Payment processing** : Jamais de double paiement
- **External API calls** : Retry automatique

**Moins critique :**
- **Opérations de lecture** : GET naturellement idempotent
- **Opérations de mise à jour** : PUT idempotent par définition

### Idempotence dans ce Projet

#### RabbitMQ Consumer (AI Service)
**Problème :** Le même message Activity peut être délivré plusieurs fois

**Solution 1 : Idempotence par ID**
```java
@Service
public class ActivityMessageListener {
    
    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity) {
        // Vérifier si la recommandation existe déjà
        if (recommendationRepository.existsByActivityId(activity.getId())) {
            log.info("Recommendation already exists for activity: {}", activity.getId());
            return; // Idempotent : ne fait rien si déjà traité
        }
        
        // Générer et sauvegarder
        Recommendation recommendation = aiService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
    }
}
```

**Solution 2 : Idempotence par clé unique en base**
```java
@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    @Index(unique = true)
    Optional<Recommendation> findByActivityId(String activityId);
}
```

#### API Endpoints (User Service)
**Problème :** Double soumission du formulaire d'inscription

**Solution : Idempotence Key**
```java
@RestController
public class UserController {
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody RegistreRequest request
    ) {
        // Vérifier si cette clé a déjà été utilisée
        Optional<UserResponse> cached = idempotencyService.get(idempotencyKey);
        if (cached.isPresent()) {
            return ResponseEntity.ok(cached.get());
        }
        
        // Traitement normal
        UserResponse response = userService.registre(request);
        
        // Sauvegarder pour les futures requêtes
        idempotencyService.put(idempotencyKey, response, Duration.ofHours(1));
        
        return ResponseEntity.ok(response);
    }
}
```

#### Activity Service
**Problème :** Double création d'activité

**Solution : Validation d'unicité**
```java
@Service
public class ActivityServiceDefault {
    
    public ActivityResponse trackActivity(ActivityRequest request) {
        // Vérifier si une activité similaire existe déjà
        if (repository.existsByUserIdAndStartTimeAndType(
            request.getUserId(), 
            request.getStartTime(), 
            request.getType()
        )) {
            throw new DuplicateResourceException("Activity already exists");
        }
        
        // Création normale
        Activity activity = activityMapper.toEntity(request);
        return activityMapper.toResponse(repository.save(activity));
    }
}
```

### Best Practices Idempotence

- **Clé d'idempotence** : UUID généré par le client
- **Cache distribué** : Redis pour stocker les réponses idempotentes
- **TTL** : Expiration des clés (1-24 heures)
- **Base de données** : Contraintes d'unicité (unique index)
- **Logging** : Logger les opérations idempotentes pour debugging

---

## 2. Race Conditions

### Qu'est-ce qu'une Race Condition ?

**Définition :** Situation où le comportement du système dépend de l'ordre d'exécution des opérations concurrentes.

**Exemple classique :** Two threads checking and updating the same value

### Pourquoi les Race Conditions ?

**Problème :**
- **Concurrent requests** : Plusieurs requêtes simultanées
- **Distributed systems** : Pas de verrouillage global
- **Database isolation** : Level par défaut (READ_COMMITTED)

**Conséquences :**
- **Lost updates** : Une mise à jour écrase une autre
- **Dirty reads** : Lecture de données non validées
- **Inconsistent state** : État incohérent

### Quand les Race Conditions ?

**Risques élevés :**
- **Compteurs** : Likes, vues, balance
- **Inventaire** : Stock, réservations
- **Status changes** : Order processing
- **Concurrent writes** : Mises à jour simultanées

### Race Conditions dans ce Projet

#### User Service - Email Uniqueness
**Problème :** Deux utilisateurs s'inscrivent avec le même email simultanément

**Solution 1 : Database Unique Constraint**
```java
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class User {
    @Column(unique = true, nullable = false)
    private String email;
}
```

**Solution 2 : Optimistic Locking**
```java
@Entity
public class User {
    @Version
    private Long version; // JPA/Hibernate gère automatiquement
}
```

#### Activity Service - Concurrent Activity Creation
**Problème :** Deux activités créées pour le même utilisateur au même moment

**Solution : Pessimistic Locking**
```java
@Service
public class ActivityServiceDefault {
    
    @Transactional
    public ActivityResponse trackActivity(ActivityRequest request) {
        // Verrouiller l'utilisateur pendant la création
        User user = userRepository.findByIdWithLock(request.getUserId());
        
        // Vérifier les contraintes métier
        if (hasActiveActivity(user)) {
            throw new BusinessException("User has active activity");
        }
        
        // Créer l'activité
        Activity activity = activityMapper.toEntity(request);
        return activityMapper.toResponse(repository.save(activity));
    }
}

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") String id);
}
```

#### Recommendation Service - Concurrent Generation
**Problème :** Deux messages RabbitMQ pour la même activité

**Solution : Idempotence + Unique Index**
```java
@Document(collection = "recommendations")
@CompoundIndex(def = "{'activityId': 1}", unique = true)
public class Recommendation {
    @Indexed(unique = true)
    private String activityId;
}
```

### Patterns pour éviter les Race Conditions

#### 1. Optimistic Locking
**Quand :** Conflits rares, lecture fréquente
**Comment :** Version field, check before update

```java
@Entity
public class Product {
    @Version
    private Long version;
}

// Update
Product product = repository.findById(id);
product.setStock(newStock);
try {
    repository.save(product); // Échoue si version != version en base
} catch (OptimisticLockException e) {
    // Retry avec la nouvelle version
}
```

#### 2. Pessimistic Locking
**Quand :** Conflits fréquents, écriture critique
**Comment :** Verrouillage explicite en base

```java
@Transactional
public void updateInventory(String productId, int quantity) {
    Product product = repository.findByIdWithLock(productId);
    product.setStock(product.getStock() - quantity);
    repository.save(product);
}
```

#### 3. Distributed Lock (Redis)
**Quand :** Opérations cross-services
**Comment :** Redis SET NX EX

```java
@Service
public class DistributedLockService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    public boolean acquireLock(String key, long ttlSeconds) {
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent("lock:" + key, "locked", ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }
    
    public void releaseLock(String key) {
        redisTemplate.delete("lock:" + key);
    }
}
```

### Best Practices Race Conditions

- **Database constraints** : Première ligne de défense
- **Optimistic locking** : Par défaut, moins de contention
- **Pessimistic locking** : Pour les opérations critiques
- **Distributed locks** : Pour cross-service operations
- **Retry logic** : Gérer les conflits élégamment
- **Monitoring** : Détecter les lock contentions

---

## 3. Circuit Breaker Pattern

### Qu'est-ce que le Circuit Breaker ?

**Définition :** Pattern qui prévient les cascades d'échecs en arrêtant les appels à un service défaillant.

**Analogie :** Disjoncteur électrique qui s'ouvre en cas de surcharge.

### Pourquoi le Circuit Breaker ?

**Problème :**
- **Cascading failures** : Un service down = tous les services down
- **Resource exhaustion** : Threads bloqués sur des appels timeout
- **Poor user experience** : Timeouts pour tous les utilisateurs

**Conséquences :**
- **System outage** : Panne complète
- **High latency** : Délais accumulés
- **Resource waste** : CPU/mémoire gaspillés

### Quand utiliser le Circuit Breaker ?

**Obligatoire :**
- **External API calls** : Gemini API, services tiers
- **Database connections** : Pool épuisé
- **Inter-service calls** : Services critiques
- **High latency operations** : Plusieurs secondes

### Circuit Breaker dans ce Projet

#### AI Service - Gemini API Calls
**Problème :** L'API Gemini peut être lente ou down

**Solution : Resilience4j Circuit Breaker**
```java
@Service
public class GeminiService {
    
    @CircuitBreaker(
        name = "geminiApi",
        fallbackMethod = "getAnswerFallback"
    )
    public String getAnswer(String question) {
        return webClient.post()
            .uri(geminiApiUrl + geminiAPiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
    
    public String getAnswerFallback(String question, Exception e) {
        log.error("Gemini API failed, using fallback", e);
        return "AI service temporarily unavailable. Please try again later.";
    }
}
```

**Configuration (application.yml) :**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      geminiApi:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
```

#### Activity Service - User Validation
**Problème :** User Service peut être down

**Solution : Circuit Breaker + Fallback**
```java
@Service
public class UserValidationService {
    
    @CircuitBreaker(
        name = "userService",
        fallbackMethod = "validateUserFallback"
    )
    public boolean validateUser(String userId) {
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
            "http://user-service/api/users/" + userId + "/validate",
            Boolean.class
        );
        return response.getBody();
    }
    
    public boolean validateUserFallback(String userId, Exception e) {
        log.error("User validation failed, allowing activity", e);
        return true; // Fallback : permettre l'activité
    }
}
```

### États du Circuit Breaker

1. **Closed** : Normal, appels passent
2. **Open** : Circuit ouvert, appels bloqués (fallback)
3. **Half-Open** : Test de récupération, un appel autorisé

### Best Practices Circuit Breaker

- **Fallback approprié** : Valeur par défaut ou message d'erreur
- **Monitoring** : Alertes quand circuit ouvert
- **Configuration tunée** : Thresholds adaptés au métier
- **Logging** : Logger les ouvertures/fermetures
- **Timeouts** : Combiner avec timeout pattern

---

## 4. Retry Pattern

### Qu'est-ce que le Retry Pattern ?

**Définition :** Réessayer automatiquement une opération qui a échoué.

### Pourquoi le Retry ?

**Problème :**
- **Transient failures** : Network glitches, timeouts
- **Temporary unavailability** : Service restart, maintenance
- **Rate limiting** : 429 Too Many Requests

**Conséquences sans retry :**
- **False failures** : Échec alors que retry aurait réussi
- **Poor UX** : Erreur pour un problème temporaire

### Quand utiliser le Retry ?

**Recommandé :**
- **Transient failures** : Network, timeouts
- **Idempotent operations** : GET, PUT (pas POST sans idempotence)
- **External APIs** : Third-party services

**Déconseillé :**
- **Permanent failures** : 404, 403, validation errors
- **Non-idempotent** : POST sans idempotency key
- **Critical failures** : Security issues

### Retry dans ce Projet

#### AI Service - Gemini API
**Problème :** API peut retourner 429 (rate limit) ou 503

**Solution : Resilience4j Retry**
```java
@Service
public class GeminiService {
    
    @Retry(
        name = "geminiApi",
        fallbackMethod = "getAnswerFallback"
    )
    @CircuitBreaker(name = "geminiApi")
    public String getAnswer(String question) {
        return webClient.post()
            .uri(geminiApiUrl + geminiAPiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
```

**Configuration :**
```yaml
resilience4j:
  retry:
    instances:
      geminiApi:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - org.springframework.web.client.HttpClientErrorException$TooManyRequests
          - org.springframework.web.client.HttpServerErrorException$ServiceUnavailable
```

#### RabbitMQ - Message Publishing
**Problème :** RabbitMQ peut être temporairement indisponible

**Solution : Retry Template**
```java
@Service
public class ActivityServiceDefault {
    
    @Autowired
    private RetryTemplate retryTemplate;
    
    public ActivityResponse trackActivity(ActivityRequest request) {
        Activity savedActivity = repository.save(activityMapper.toEntity(request));
        
        retryTemplate.execute(context -> {
            rabbitTemplate.convertAndSend(exchange, routingkey, savedActivity);
            return null;
        });
        
        return activityMapper.toResponse(savedActivity);
    }
}
```

### Best Practices Retry

- **Exponential backoff** : 1s, 2s, 4s, 8s
- **Max attempts** : 3-5 retries
- **Idempotence** : Obligatoire pour les retries
- **Specific exceptions** : Ne retry que les transient failures
- **Circuit breaker** : Combiner retry + circuit breaker

---

## 5. Bulkhead Pattern

### Qu'est-ce que le Bulkhead ?

**Définition :** Isolation des ressources pour limiter l'impact des pannes.

**Analogie :** Cloisons étanches dans un navire.

### Pourquoi le Bulkhead ?

**Problème :**
- **Resource exhaustion** : Un service consomme toutes les ressources
- **Cascading failures** : Panne d'un service = panne globale
- **No isolation** : Partage de thread pool

### Quand utiliser le Bulkhead ?

**Recommandé :**
- **Resource-intensive operations** : AI processing, file uploads
- **External API calls** : Rate limiting
- **Different priorities** : Critical vs non-critical operations

### Bulkhead dans ce Projet

#### AI Service - Resource Isolation
**Problème :** Trop de requêtes IA = CPU/memory exhaustion

**Solution : Resilience4j Bulkhead**
```java
@Service
public class ActivityAIService {
    
    @Bulkhead(
        name = "aiProcessing",
        fallbackMethod = "generateRecommendationFallback"
    )
    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        return processAiResponse(activity, aiResponse);
    }
    
    public Recommendation generateRecommendationFallback(Activity activity, Exception e) {
        log.error("AI processing bulkhead full", e);
        return createDefaultRecommendation(activity);
    }
}
```

**Configuration :**
```yaml
resilience4j:
  bulkhead:
    instances:
      aiProcessing:
        max-concurrent-calls: 10
        max-wait-duration: 1s
```

### Types de Bulkhead

1. **Semaphore Bulkhead** : Limite le nombre de concurrent calls
2. **ThreadPool Bulkhead** : Thread pool isolé

### Best Practices Bulkhead

- **Resource limits** : Basé sur la capacité réelle
- **Monitoring** : Alertes si bulkhead saturated
- **Fallback** : Que faire si bulkhead full
- **Priority** : Bulkhead pour opérations critiques

---

## 6. Saga Pattern

### Qu'est-ce que le Saga ?

**Définition :** Pattern pour gérer les transactions distribuées sans 2PC (Two-Phase Commit).

### Pourquoi le Saga ?

**Problème :**
- **Distributed transactions** : ACID impossible cross-services
- **2PC limitations** : Bloquant, complexe, peu scalable
- **Partial failures** : Un service échoue, les autres ont déjà commit

**Conséquences :**
- **Inconsistent state** : Données incohérentes
- **Business errors** : Commande payée mais non livrée

### Quand utiliser le Saga ?

**Obligatoire :**
- **Distributed transactions** : Multi-service operations
- **Long-running transactions** : Minutes/heures
- **Eventual consistency** : Acceptable délai de cohérence

### Types de Saga

#### 1. Choreography-Based Saga
**Comment :** Chaque service émet des événements

**Exemple :**
```
Order Service → OrderCreated Event
Inventory Service → InventoryReserved Event
Payment Service → PaymentProcessed Event
Shipping Service → OrderShipped Event
```

#### 2. Orchestration-Based Saga
**Comment :** Un orchestrator coordonne

**Exemple :**
```
Orchestrator → Order Service (call)
Orchestrator → Inventory Service (call)
Orchestrator → Payment Service (call)
Orchestrator → Shipping Service (call)
```

### Saga dans ce Projet

**Scénario :** Création d'activité avec recommandation IA

**Choreography-Based :**
```java
// Activity Service
@Service
public class ActivityServiceDefault {
    public ActivityResponse trackActivity(ActivityRequest request) {
        Activity activity = repository.save(activityMapper.toEntity(request));
        
        // Événement : ActivityCreated
        rabbitTemplate.convertAndSend("activity.exchange", "activity.created", activity);
        
        return activityMapper.toResponse(activity);
    }
}

// AI Service
@Service
public class ActivityMessageListener {
    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity) {
        try {
            Recommendation recommendation = aiService.generateRecommendation(activity);
            recommendationRepository.save(recommendation);
            
            // Événement : RecommendationGenerated
            rabbitTemplate.convertAndSend("recommendation.exchange", "recommendation.generated", recommendation);
        } catch (Exception e) {
            // Compensation : ActivityFailed
            rabbitTemplate.convertAndSend("activity.exchange", "activity.failed", activity.getId());
        }
    }
}
```

### Compensation Transactions

**Si échec :** Annuler les étapes précédentes

```java
@Service
public class CompensationService {
    
    @RabbitListener(queues = "compensation.queue")
    public void handleCompensation(String activityId) {
        // Supprimer l'activité
        activityRepository.deleteById(activityId);
        
        // Notifier l'utilisateur
        notificationService.notifyFailure(activityId);
    }
}
```

### Best Practices Saga

- **Idempotence** : Chaque step doit être idempotent
- **Compensation** : Définir les compensations
- **Timeout** : Limiter la durée des sagas
- **Logging** : Tracer chaque étape
- **Monitoring** : Détecter les sagas orphelines

---

## 7. Eventual Consistency

### Qu'est-ce que l'Eventual Consistency ?

**Définition :** Le système garantit que si aucune nouvelle mise à jour n'est faite, éventuellement tous les accès aux données retourneront la dernière valeur écrite.

### Pourquoi Eventual Consistency ?

**Problème :**
- **Distributed systems** : ACID impossible
- **Performance** : Strong consistency = slow
- **Availability** : CAP theorem

**Trade-off :** Consistency vs Availability vs Partition tolerance (CAP)

### Quand Eventual Consistency ?

**Acceptable :**
- **Social media** : Likes, comments
- **Analytics** : Views, metrics
- **Recommendations** : AI suggestions
- **Notifications** : Emails, push

**Inacceptable :**
- **Financial** : Balance, transactions
- **Inventory** : Stock critique
- **Security** : Permissions

### Eventual Consistency dans ce Projet

**Scénario :** Activité créée → Recommandation générée

```java
// T0 : Activity créée
Activity activity = activityService.trackActivity(request);
// activity existe, recommendation pas encore

// T+5s : Recommandation générée
Recommendation recommendation = recommendationService.getActivityRecommendation(activity.getId());
// recommendation existe maintenant
```

**Stratégies pour gérer l'incohérence temporaire :**

1. **Polling** : Client interroge périodiquement
2. **WebSocket** : Notification temps réel
3. **Optimistic UI** : Afficher "En cours de traitement"

```java
@RestController
public class RecommendationController {
    
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getActivityRecommendation(@PathVariable String activityId) {
        Optional<Recommendation> recommendation = recommendationRepository.findByActivityId(activityId);
        
        if (recommendation.isPresent()) {
            return ResponseEntity.ok(recommendation.get());
        } else {
            return ResponseEntity.accepted()
                .body(Map.of("status", "processing", "message", "Recommendation being generated"));
        }
    }
}
```

### Best Practices Eventual Consistency

- **Communication** : Informer les utilisateurs du délai
- **Monitoring** : Mesurer le temps de cohérence
- **Compensation** : Gérer les échecs
- **Idempotence** : Permettre les retries

---

## 8. Dead Letter Queue (DLQ)

### Qu'est-ce que la DLQ ?

**Définition :** Queue spéciale pour les messages qui n'ont pas pu être traités.

### Pourquoi la DLQ ?

**Problème :**
- **Poison messages** : Messages qui causent des erreurs répétées
- **Infinite retry** : Boucle infinie de retries
- **Message loss** : Suppression sans analyse

**Conséquences :**
- **Blocked queue** : Messages bloqués
- **Resource waste** : CPU/memory gaspillés
- **Data loss** : Messages perdus

### Quand utiliser la DLQ ?

**Obligatoire :**
- **Message queues** : RabbitMQ, Kafka
- **Critical data** : Messages business-critical
- **Retry logic** : Avec max attempts

### DLQ dans ce Projet

**Configuration RabbitMQ :**
```java
@Configuration
public class RabbitmqConfig {
    
    @Bean
    public Queue activityQueue() {
        return QueueBuilder.durable("activity.queue")
            .withArgument("x-dead-letter-exchange", "activity.dlx")
            .withArgument("x-dead-letter-routing-key", "activity.dlq")
            .build();
    }
    
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("activity.dlx");
    }
    
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("activity.dlq").build();
    }
    
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("activity.dlq");
    }
}
```

**DLQ Consumer :**
```java
@Service
public class DeadLetterQueueHandler {
    
    @RabbitListener(queues = "activity.dlq")
    public void handleDeadLetter(Message message) {
        try {
            Activity activity = objectMapper.readValue(message.getBody(), Activity.class);
            
            // Logger pour analyse
            log.error("Message sent to DLQ: {}", activity.getId());
            
            // Notification équipe
            alertService.notifyTeam("DLQ message", activity);
            
            // Manuel retry ou suppression
        } catch (Exception e) {
            log.error("Failed to process DLQ message", e);
        }
    }
}
```

### Best Practices DLQ

- **Analysis** : Analyser les messages en DLQ
- **Alerting** : Notifier l'équipe
- **Manual retry** : Outil de retry manuel
- **TTL** : Expiration des messages DLQ

---

## 9. Rate Limiting

### Qu'est-ce que le Rate Limiting ?

**Définition :** Limiter le nombre de requêtes qu'un client peut faire.

### Pourquoi le Rate Limiting ?

**Problème :**
- **Abuse** : API abuse, DDoS
- **Cost** : External API costs (Gemini API)
- **Fairness** : Un utilisateur ne doit pas monopoliser les ressources

### Quand utiliser le Rate Limiting ?

**Obligatoire :**
- **Public APIs** : Exposées sur internet
- **External APIs** : Coût par appel
- **Resource-intensive** : Opérations coûteuses

### Rate Limiting dans ce Projet

#### API Gateway - Rate Limiting
**Configuration :**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ai-service
          uri: lb://AI-SERVICE
          predicates:
            - Path=/api/recommendations/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

#### AI Service - Gemini API Rate Limiting
```java
@Service
public class GeminiService {
    
    @RateLimiter(name = "geminiApi", fallbackMethod = "getAnswerFallback")
    public String getAnswer(String question) {
        // Appel Gemini API
    }
}
```

**Configuration :**
```yaml
resilience4j:
  ratelimiter:
    instances:
      geminiApi:
        limit-for-period: 10
        limit-refresh-period: 1s
        timeout-duration: 3s
```

### Types de Rate Limiting

1. **Token Bucket** : Burst allowance
2. **Leaky Bucket** : Constant rate
3. **Fixed Window** : Count per time window
4. **Sliding Window** : More accurate

### Best Practices Rate Limiting

- **Per user** : Rate limiting par utilisateur
- **Per IP** : Protection contre DDoS
- **Graceful degradation** : Message d'erreur clair
- **Monitoring** : Alertes si limit atteintes

---

## 10. Caching Strategy

### Qu'est-ce que le Caching ?

**Définition :** Stocker des résultats coûteux pour éviter les recalculs.

### Pourquoi le Caching ?

**Problème :**
- **Latency** : Database/API calls sont lents
- **Load** : Trop de requêtes sur la base
- **Cost** : External API costs

### Quand utiliser le Caching ?

**Recommandé :**
- **Read-heavy** : Beaucoup de lectures, peu d'écritures
- **Expensive operations** : Calculs complexes, API calls
- **Immutable data** : Données qui changent rarement

### Caching dans ce Projet

#### User Service - User Profile Cache
```java
@Service
public class UserServiceDefault {
    
    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUserProfile(String userId) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }
    
    @CacheEvict(value = "users", key = "#userId")
    public void updateUser(String userId, UpdateUserRequest request) {
        // Update logic
    }
}
```

#### AI Service - Recommendation Cache
```java
@Service
public class RecommendationService {
    
    @Cacheable(value = "recommendations", key = "#activityId")
    public Recommendation getActivityRecommendation(String activityId) {
        return recommendationRepository.findByActivityId(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));
    }
}
```

### Cache Eviction Strategies

1. **Time-based** : TTL (Time To Live)
2. **Event-based** : Evict on update
3. **Size-based** : LRU (Least Recently Used)

### Best Practices Caching

- **Cache invalidation** : Critical
- **Distributed cache** : Redis pour multi-instance
- **Monitoring** : Cache hit/miss ratio
- **Fallback** : Comportement si cache down

---

## 11. Distributed Tracing

### Qu'est-ce que le Distributed Tracing ?

**Définition** : Suivre une requête à travers plusieurs microservices.

### Pourquoi Distributed Tracing ?

**Problème :**
- **Debugging difficile** : Où est l'erreur dans une chaîne de services ?
- **Performance** : Quel service est lent ?
- **Correlation** : Relier les logs entre services

### Distributed Tracing dans ce Projet

**Spring Cloud Sleuth + Zipkin :**

**Dépendances :**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

**Configuration :**
```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://localhost:9411
  application:
    name: user-service
```

**Trace ID dans les logs :**
```
2024-09-07 15:30:00.123 INFO [user-service,4a1b2c3d4e5f,1a2b3c4d5e6f] 12345 --- ...
```

### Best Practices Distributed Tracing

- **Correlation ID** : Propager entre services
- **Sampling** : Ne pas tracer 100% des requêtes
- **Sensitive data** : Ne pas tracer de données sensibles

---

## 12. Summary - Patterns à Implémenter

### Priorité Haute (Immédiat)

1. **Idempotence** : RabbitMQ consumers (AI Service)
2. **Race Conditions** : Unique constraints en base
3. **Circuit Breaker** : Gemini API calls
4. **Retry** : External API calls
5. **DLQ** : RabbitMQ dead letter queue

### Priorité Moyenne (Court terme)

6. **Rate Limiting** : API Gateway
7. **Caching** : User profiles, recommendations
8. **Distributed Tracing** : Sleuth + Zipkin
9. **Bulkhead** : AI processing isolation

### Priorité Basse (Long terme)

10. **Saga Pattern** : Si transactions distribuées complexes
11. **Eventual Consistency** : Documentation + monitoring
12. **Optimistic Locking** : Si conflits fréquents

### Checklist d'Implémentation

- [ ] Idempotence sur tous les @RabbitListener
- [ ] Unique constraints sur email, keycloakId
- [ ] Circuit Breaker sur Gemini API
- [ ] Retry avec exponential backoff
- [ ] DLQ configurée pour toutes les queues
- [ ] Rate limiting sur API Gateway
- [ ] Cache Redis pour user profiles
- [ ] Distributed tracing avec Sleuth
- [ ] Monitoring des circuit breakers
- [ ] Alertes sur DLQ messages
