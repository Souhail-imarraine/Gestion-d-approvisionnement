# ✅ Vérification Docker & Configuration

## 🔍 PROBLÈME DÉTECTÉ

Votre `application.properties` a une configuration mixte:
- ❌ URL: `jdbc:mysql://mysql:3306/...` (pour Docker)
- ❌ Password: vide (pour local)

---

## 🛠️ SOLUTION: Créer 2 Configurations

### Option 1: Configuration Locale (Développement)

**Fichier:** `src/main/resources/application.properties`

```properties
# Base de donnees - LOCAL
spring.datasource.url=jdbc:mysql://localhost:3306/tricol_stock_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Liquibase
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.enabled=true

# Serveur
server.port=8081
server.servlet.context-path=/tricol-stock

# Logging
logging.level.com.tricol=DEBUG
logging.level.org.springframework.security=DEBUG

# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

---

### Option 2: Configuration Docker

**Fichier:** `src/main/resources/application-docker.properties`

```properties
# Base de donnees - DOCKER
spring.datasource.url=jdbc:mysql://mysql:3306/tricol_stock_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Liquibase
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.enabled=true

# Serveur
server.port=8081
server.servlet.context-path=/tricol-stock

# Logging
logging.level.com.tricol=INFO
logging.level.org.springframework.security=WARN

# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

---

## 📝 ÉTAPE 1: Corriger application.properties (2 min)

### 1.1 Pour Développement Local

```properties
# Changer cette ligne:
spring.datasource.url=jdbc:mysql://mysql:3306/tricol_stock_db?createDatabaseIfNotExist=true

# En:
spring.datasource.url=jdbc:mysql://localhost:3306/tricol_stock_db?createDatabaseIfNotExist=true
```

### 1.2 Créer application-docker.properties

Créer le fichier avec la configuration Docker (voir Option 2 ci-dessus)

---

## 🐳 ÉTAPE 2: Vérifier docker-compose.yml (3 min)

### 2.1 Vérifier le Fichier

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: tricol-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root          # ⚠️ Doit correspondre à application-docker.properties
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
      SPRING_PROFILES_ACTIVE: docker     # ⚠️ Important!
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_stock_db?createDatabaseIfNotExist=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
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

**Points clés:**
- ✅ `MYSQL_ROOT_PASSWORD: root`
- ✅ `SPRING_PROFILES_ACTIVE: docker`
- ✅ `SPRING_DATASOURCE_PASSWORD: root`

---

## 🧪 ÉTAPE 3: Tester en Local (5 min)

### 3.1 Démarrer MySQL Local

```bash
# Vérifier que MySQL est démarré
mysql -u root -p

# Créer la base si nécessaire
CREATE DATABASE IF NOT EXISTS tricol_stock_db;
```

### 3.2 Démarrer l'Application

```bash
mvn spring-boot:run
```

### 3.3 Vérifier les Logs

**Logs attendus:**
```
Started StockApplication in X seconds
Liquibase Update Successful
HikariPool-1 - Start completed
```

**❌ Si erreur:**
```
Access denied for user 'root'@'localhost'
→ Vérifier le mot de passe MySQL

Communications link failure
→ MySQL n'est pas démarré

Unknown database 'tricol_stock_db'
→ Créer la base manuellement
```

### 3.4 Tester l'API

```bash
curl http://localhost:8081/tricol-stock/api/auth/login
```

**Résultat attendu:** Réponse JSON (même si 401)

---

## 🐳 ÉTAPE 4: Tester avec Docker (15 min)

### 4.1 Build l'Image

```bash
docker build -t tricol-stock:latest .
```

**Résultat attendu:**
```
[+] Building 120.5s (14/14) FINISHED
 => => naming to docker.io/library/tricol-stock:latest
```

**❌ Si erreur:**
```
ERROR: failed to solve: process "/bin/sh -c mvn clean package -DskipTests" did not complete successfully

→ Vérifier que Maven peut compiler:
mvn clean package -DskipTests
```

---

### 4.2 Démarrer avec Docker Compose

```bash
# Arrêter MySQL local d'abord!
# Puis:
docker-compose up -d
```

**Résultat attendu:**
```
[+] Running 3/3
 ✔ Network tricol-network    Created
 ✔ Container tricol-mysql     Started
 ✔ Container tricol-stock     Started
```

---

### 4.3 Vérifier les Conteneurs

```bash
docker ps
```

**Résultat attendu:**
```
CONTAINER ID   IMAGE          STATUS         PORTS
abc123         tricol-stock   Up 2 minutes   0.0.0.0:8081->8081/tcp
def456         mysql:8.0      Up 2 minutes   0.0.0.0:3306->3306/tcp
```

---

### 4.4 Vérifier les Logs MySQL

```bash
docker logs tricol-mysql
```

**Logs attendus:**
```
[Server] /usr/sbin/mysqld: ready for connections
[Server] port: 3306  MySQL Community Server - GPL
```

**❌ Si erreur:**
```
[ERROR] [MY-010735] Can't open the mysql.plugin table
→ Supprimer le volume et recréer:
docker-compose down -v
docker-compose up -d
```

---

### 4.5 Vérifier les Logs Application

```bash
docker logs tricol-stock -f
```

**Logs attendus:**
```
Starting StockApplication...
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Liquibase: Successfully acquired change log lock
Liquibase: Update has been successful
Started StockApplication in 45.123 seconds
```

**❌ Si erreur:**
```
Communications link failure
→ MySQL n'est pas prêt, attendre 30 secondes

Access denied for user 'root'@'172.x.x.x'
→ Vérifier MYSQL_ROOT_PASSWORD dans docker-compose.yml

Unknown database 'tricol_stock_db'
→ Vérifier MYSQL_DATABASE dans docker-compose.yml
```

---

### 4.6 Tester l'API Docker

```bash
curl http://localhost:8081/tricol-stock/api/auth/login
```

**Résultat attendu:** Réponse JSON

---

## 🔍 ÉTAPE 5: Vérifier la Base de Données (5 min)

### 5.1 Se Connecter à MySQL Docker

```bash
docker exec -it tricol-mysql mysql -u root -proot
```

### 5.2 Vérifier les Tables

```sql
USE tricol_stock_db;

SHOW TABLES;
```

**Résultat attendu:**
```
+---------------------------+
| Tables_in_tricol_stock_db |
+---------------------------+
| audit_logs                |
| bons_sortie               |
| commandes                 |
| fournisseurs              |
| lignes_bon_sortie         |
| lignes_commande           |
| lots_stock                |
| mouvements_stock          |
| permissions               |
| produits                  |
| refresh_tokens            |
| role_permissions          |
| roles                     |
| user_permissions          |
| user_roles                |
| users                     |
+---------------------------+
16 rows in set
```

---

### 5.3 Vérifier l'Admin

```sql
SELECT username, email, enabled FROM users WHERE username = 'admin';
```

**Résultat attendu:**
```
+----------+-------------------+---------+
| username | email             | enabled |
+----------+-------------------+---------+
| admin    | admin@tricol.com  |       1 |
+----------+-------------------+---------+
```

---

### 5.4 Vérifier les Rôles

```sql
SELECT name FROM roles;
```

**Résultat attendu:**
```
+--------------------+
| name               |
+--------------------+
| ADMIN              |
| RESPONSABLE_ACHATS |
| MAGASINIER         |
| CHEF_ATELIER       |
+--------------------+
```

---

### 5.5 Vérifier les Permissions

```sql
SELECT COUNT(*) as total FROM permissions;
```

**Résultat attendu:**
```
+-------+
| total |
+-------+
|    19 |
+-------+
```

---

## ✅ ÉTAPE 6: Checklist Finale

### Configuration
- [ ] application.properties → localhost (local)
- [ ] application-docker.properties créé
- [ ] docker-compose.yml → password: root
- [ ] Dockerfile existe

### Tests Locaux
- [ ] MySQL local démarré
- [ ] `mvn spring-boot:run` fonctionne
- [ ] Application démarre sans erreur
- [ ] API répond sur http://localhost:8081

### Tests Docker
- [ ] `docker build` réussit
- [ ] `docker-compose up` démarre les 2 conteneurs
- [ ] Logs MySQL OK
- [ ] Logs App OK
- [ ] API répond sur http://localhost:8081
- [ ] 16 tables créées
- [ ] Admin existe
- [ ] 4 rôles créés
- [ ] 19 permissions créées

---

## 🎯 RÉSULTAT FINAL

Si toutes les cases sont cochées:

```
╔═══════════════════════════════════════════╗
║  ✅ CONFIGURATION 100% CORRECTE          ║
║                                           ║
║  ✓ Local: Fonctionne                     ║
║  ✓ Docker: Fonctionne                    ║
║  ✓ Base de données: OK                   ║
║  ✓ Liquibase: OK                         ║
║  ✓ API: Accessible                       ║
║                                           ║
║  PRÊT POUR LA PRODUCTION! 🚀             ║
╚═══════════════════════════════════════════╝
```

---

## 🆘 Commandes de Dépannage

```bash
# Voir tous les conteneurs
docker ps -a

# Voir les logs
docker logs tricol-stock
docker logs tricol-mysql

# Redémarrer
docker-compose restart

# Tout supprimer et recréer
docker-compose down -v
docker-compose up -d

# Entrer dans le conteneur
docker exec -it tricol-stock sh
docker exec -it tricol-mysql bash

# Vérifier le réseau
docker network inspect tricol-network
```

---

**Temps total estimé:** 30 minutes
