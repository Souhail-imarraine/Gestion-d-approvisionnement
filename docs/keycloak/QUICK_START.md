# Keycloak Quick Start Guide

This is a condensed version for getting Keycloak running quickly. For detailed explanation, see [KEYCLOAK_INTEGRATION_GUIDE.md](../KEYCLOAK_INTEGRATION_GUIDE.md).

## 1. Start Keycloak (5 minutes)

### Add to docker-compose.yml

```yaml
  keycloak:
    image: quay.io/keycloak/keycloak:23.0.0
    container_name: tricol-keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: mysql
      KC_DB_URL: jdbc:mysql://mysql:3306/keycloak_db?createDatabaseIfNotExist=true
      KC_DB_USERNAME: root
      KC_DB_PASSWORD: root
      KC_HOSTNAME_STRICT: false
      KC_HTTP_ENABLED: true
      KC_HOSTNAME_STRICT_HTTPS: false
    ports:
      - "8180:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - tricol-network
    command:
      - start-dev
```

### Start Keycloak

```bash
docker-compose up -d keycloak
docker-compose logs -f keycloak
```

Wait for: "Keycloak 23.0.0 started"

## 2. Configure Keycloak (10 minutes)

### Access Admin Console
- URL: http://localhost:8180
- User: admin / admin

### Import Test Realm (Easiest Way)

1. Click **Add realm**
2. Click **Select file** → Choose `docs/keycloak/test-realm-export.json`
3. Click **Create**

**OR** Manual Setup:

1. **Create Realm**: `tricol-stock`
2. **Create Client**: `tricol-stock-app`
   - Access Type: `confidential`
   - Valid Redirect URIs: `http://localhost:8081/tricol-stock/*`
   - Save and copy the **Secret** from Credentials tab
3. **Create Roles**: `ADMIN`, `MANAGER`, `USER`, `GUEST`
4. **Create Test User**:
   - Username: `admin`
   - Email: `admin@tricol.com`
   - Set password in Credentials tab
   - Assign `ADMIN` role

## 3. Update Spring Boot (15 minutes)

### Update pom.xml

Add these dependencies:

```xml
<!-- Add -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Keycloak Admin Client -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>23.0.0</version>
</dependency>
```

### Update application.properties

```properties
# Replace JWT config with:
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-stock
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/tricol-stock/protocol/openid-connect/certs

keycloak.auth-server-url=http://localhost:8180
keycloak.realm=tricol-stock
keycloak.resource=tricol-stock-app
keycloak.credentials.secret=YOUR_CLIENT_SECRET_HERE
```

### Create KeycloakSecurityConfig.java

Replace `SecurityConfig.java` content with:

```java
package com.tricol.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class KeycloakSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return Collections.emptySet();
            }
            
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
        });
        return converter;
    }
}
```

## 4. Test (5 minutes)

### Get Token

```bash
curl -X POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=tricol-stock-app" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password"
```

Copy the `access_token` from response.

### Test API

```bash
curl -X GET http://localhost:8081/tricol-stock/api/v1/produits \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Common Issues

### 1. Token validation fails
- Check `issuer-uri` in application.properties
- Ensure Keycloak is accessible: `curl http://localhost:8180/realms/tricol-stock`

### 2. No roles in token
- Verify role assignment in Keycloak UI
- Check token at jwt.io

### 3. 401 Unauthorized
- Token might be expired (default 5 min)
- Get a new token

## Next Steps

1. ✅ Read full guide: [KEYCLOAK_INTEGRATION_GUIDE.md](../KEYCLOAK_INTEGRATION_GUIDE.md)
2. ✅ Configure password policies
3. ✅ Setup client scopes for permissions
4. ✅ Migrate existing users
5. ✅ Enable MFA for admin
6. ✅ Setup HTTPS for production

## Quick Reference

| Item | Value |
|------|-------|
| Keycloak Admin | http://localhost:8180 |
| Admin User | admin / admin |
| Realm | tricol-stock |
| Client ID | tricol-stock-app |
| Token Endpoint | http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token |
| JWKS Endpoint | http://localhost:8180/realms/tricol-stock/protocol/openid-connect/certs |

