# 🐳 Comment Docker Utilise la Configuration

## 📋 3 Méthodes pour Configurer Docker

---

## ✅ MÉTHODE 1: Variables d'Environnement (docker-compose.yml) ⭐ RECOMMANDÉ

### Comment ça marche:

```yaml
app:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_stock_db
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: root
```

**Spring Boot lit automatiquement ces variables!**

### Priorité:
```
Variables d'environnement > application.properties
```

### Avantages:
- ✅ Pas besoin de application-docker.properties
- ✅ Configuration centralisée dans docker-compose.yml
- ✅ Facile à changer sans rebuild
- ✅ Sécurisé (pas de mot de passe dans le code)

### Votre docker-compose.yml CORRIGÉ:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: tricol-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root          # ⚠️ IMPORTANT!
      MYSQL_DATABASE: tricol_stock_db
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - tricol-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  app:
    build: .
    container_name: tricol-stock
    ports:
      - "8081:8081"
    environment:
      # Ces variables REMPLACENT application.properties
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_stock_db?createDatabaseIfNotExist=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root   # ⚠️ IMPORTANT!
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - tricol-network

volumes:
  mysql-data:

networks:
  tricol-network:
    driver: bridge
```

**Avec cette méthode, vous n'avez PAS besoin de application-docker.properties!**

---

## ✅ MÉTHODE 2: Spring Profiles (application-docker.properties)

### Comment ça marche:

1. **Créer** `application-docker.properties`:
```properties
spring.datasource.url=jdbc:mysql://mysql:3306/tricol_stock_db
spring.datasource.username=root
spring.datasource.password=root
```

2. **Activer le profil** dans docker-compose.yml:
```yaml
app:
  environment:
    SPRING_PROFILES_ACTIVE: docker  # Active application-docker.properties
```

### Priorité:
```
application-docker.properties > application.properties
```

### docker-compose.yml avec Profil:

```yaml
app:
  build: .
  environment:
    SPRING_PROFILES_ACTIVE: docker  # Utilise application-docker.properties
  depends_on:
    mysql:
      condition: service_healthy
```

---

## ✅ MÉTHODE 3: Dockerfile avec ENV

### Comment ça marche:

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# Définir les variables par défaut
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/tricol_stock_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🎯 QUELLE MÉTHODE CHOISIR?

### Pour Votre Projet: MÉTHODE 1 ⭐

**Pourquoi?**
- ✅ Plus simple
- ✅ Tout dans docker-compose.yml
- ✅ Pas de fichier supplémentaire
- ✅ Facile à modifier

### Configuration Finale:

**1. application.properties** (pour local):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tricol_stock_db
spring.datasource.username=root
spring.datasource.password=
```

**2. docker-compose.yml** (pour Docker):
```yaml
app:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_stock_db
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: root
```

**C'est tout!** Pas besoin de application-docker.properties.

---

## 🧪 Comment Tester

### Test 1: Vérifier les Variables

```bash
# Démarrer Docker
docker-compose up -d

# Voir les variables d'environnement
docker exec tricol-stock env | grep SPRING
```

**Résultat attendu:**
```
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/tricol_stock_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
```

---

### Test 2: Vérifier la Connexion MySQL

```bash
# Entrer dans le conteneur app
docker exec -it tricol-stock sh

# Tester la connexion (si mysql-client installé)
ping mysql
```

**Résultat attendu:**
```
PING mysql (172.x.x.x): 56 data bytes
64 bytes from 172.x.x.x: seq=0 ttl=64 time=0.123 ms
```

---

### Test 3: Vérifier les Logs

```bash
docker logs tricol-stock | grep -i datasource
```

**Résultat attendu:**
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@...
HikariPool-1 - Start completed.
```

---

## 🔄 Ordre de Priorité Spring Boot

```
1. Variables d'environnement (docker-compose.yml)
   ↓
2. application-{profile}.properties (ex: application-docker.properties)
   ↓
3. application.properties
```

**Exemple:**

Si vous avez:
- `application.properties`: password = ""
- `docker-compose.yml`: SPRING_DATASOURCE_PASSWORD = "root"

**Spring Boot utilisera:** `root` (priorité aux variables d'environnement)

---

## 📝 Résumé

### Votre Configuration Actuelle (CORRIGÉE):

```
Local (mvn spring-boot:run):
├─ application.properties
│  └─ url: localhost:3306
│  └─ password: (vide)

Docker (docker-compose up):
├─ docker-compose.yml
│  └─ SPRING_DATASOURCE_URL: mysql:3306
│  └─ SPRING_DATASOURCE_PASSWORD: root
└─ Les variables ENV remplacent application.properties
```

### Fichiers Nécessaires:

- ✅ `application.properties` (pour local)
- ✅ `docker-compose.yml` (avec variables ENV)
- ❌ `application-docker.properties` (PAS NÉCESSAIRE avec Méthode 1)

---

## 🎉 Conclusion

**Vous n'avez PAS besoin de application-docker.properties!**

Votre `docker-compose.yml` (maintenant corrigé) suffit car:
1. Les variables `SPRING_DATASOURCE_*` remplacent `application.properties`
2. Spring Boot les lit automatiquement
3. C'est plus simple et plus flexible

**Testez maintenant:**
```bash
docker-compose up -d
docker logs tricol-stock -f
```

Si vous voyez "Started StockApplication", c'est bon! ✅
