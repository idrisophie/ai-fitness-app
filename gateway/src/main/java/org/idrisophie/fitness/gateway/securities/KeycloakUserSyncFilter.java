package org.idrisophie.fitness.gateway.securities;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.idrisophie.fitness.gateway.users.RegistreRequest;
import org.idrisophie.fitness.gateway.users.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegistreRequest registreRequest = getUserDetails(token);

        if(userId == null){
            userId = registreRequest.getKeycloakId();
        }
        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist) {
                            // Register User
                            if (registreRequest != null) {
                                return userService.registerUser(registreRequest)
                                        .then();
                            }
                            return Mono.empty();
                        } else {
                            log.info("User already exists, skipping sync.");
                            return Mono.empty();
                        }
                    })
                    .then(
                            Mono.defer(() ->
                                    chain.filter(
                                            exchange.mutate()
                                                    .request(request ->
                                                            request.header("X-User-ID", finalUserId)
                                                    )
                                                    .build()
                                    )
                            )
                    );
        }
        return chain.filter(exchange);
    }
    private RegistreRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token
                    .replaceFirst("(?i)^Bearer\\s+", "")
                    .trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            RegistreRequest registreRequest = new RegistreRequest();
            registreRequest.setEmail(claims.getStringClaim("email"));
            registreRequest.setKeycloakId(claims.getStringClaim("sub"));
            registreRequest.setPassword("dummy@1234");
            registreRequest.setFirstName(claims.getStringClaim("given_name"));
            registreRequest.setLastName(claims.getStringClaim("family_name"));
            return registreRequest;
        } catch (Exception e) {
            log.error("Unable to extract user information from Keycloak token", e);
            return null;
        }
    }
}