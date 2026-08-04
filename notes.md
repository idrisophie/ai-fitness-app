### Spring cloud Netflix Eureka
Un composant de Service Discovery.Dans une archietcture microservice dynamics, eureka
evite de hardcoder les adresses IP/Ports en agissant comme un annuaire tele centralise.
## Client-Side Service Discovery
Eureka utilise un modele ou les clients interrogent l'annuaire puis gerent eux-memes l'appel aux instances.
1. Eureka Server:Le serveur central qui maintient la base de donnees des services actifs en memoire
2. Eureka Client: Les microservices qui s'enregistrent aupres d'Eureka au demarrage et telecharge la liste du registre
pour faire du load balancing cote client via Spring Cloud LoadBalancer

### API Gateway
Une architecture microservices avec Spring Boot, Spring Cloud Gateway agit comme le point d'entree unique pour toutes
les requetes externes des clients
Elle resout le problemes du decouplage: les utilisateurs n'ont pas a connaitre l'existence ou les adresses de 50 
microservices differents.
La Gateway se place devan vos microservices et travaille main dans la main avec eureka
-----------------------------------------------------------------
