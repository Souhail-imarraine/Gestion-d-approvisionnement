# Guide de Dépannage - Problème de Connexion

## Problème: "Invalid credentials" lors du login

### Causes Possibles

1. ✅ **L'utilisateur admin n'existe pas en base de données**
2. ✅ **L'utilisateur existe mais n'est pas activé (enabled=false)**
3. ✅ **Le mot de passe ne correspond pas**
4. ✅ **Liquibase n'a pas été exécuté**

---

## Solution 1: Vérifier si l'utilisateur admin existe

### Étape 1: Connectez-vous à MySQL
```bash
mysql -u root -p
```

### Étape 2: Sélectionnez la base de données
```sql
USE tricol_stock;
```

### Étape 3: Vérifiez si l'utilisateur admin existe
```sql
SELECT id, username, email, enabled FROM users WHERE username = 'admin';
```

**Résultat attendu:**
```
+----+----------+-------------------+---------+
| id | username | email             | enabled |
+----+----------+-------------------+---------+
|  1 | admin    | admin@tricol.com  |       1 |
+----+----------+-------------------+---------+
```

---

## Solution 2: Si l'utilisateur n'existe pas, le créer manuellement

### Créer l'utilisateur admin
```sql
-- Mot de passe: "password" (déjà hashé avec BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, enabled)
VALUES ('admin', 'admin@tricol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Tricol', true);
```

### Assigner le rôle ADMIN
```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
```

### Vérifier que le rôle a été assigné
```sql
SELECT u.username, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';
```

---

## Solution 3: Si l'utilisateur existe mais enabled=false

### Activer l'utilisateur
```sql
UPDATE users SET enabled = true WHERE username = 'admin';
```

### Vérifier
```sql
SELECT username, enabled FROM users WHERE username = 'admin';
```

---

## Solution 4: Vérifier que Liquibase a créé les tables

### Vérifier les tables de sécurité
```sql
SHOW TABLES LIKE '%user%';
SHOW TABLES LIKE '%role%';
SHOW TABLES LIKE '%permission%';
```

**Résultat attendu:**
```
+---------------------------+
| Tables_in_tricol_stock    |
+---------------------------+
| users                     |
| roles                     |
| permissions               |
| user_roles                |
| user_permissions          |
| role_permissions          |
| refresh_tokens            |
| audit_logs                |
+---------------------------+
```

### Si les tables n'existent pas, forcer Liquibase
```bash
# Arrêter l'application
# Supprimer la table de tracking Liquibase
mysql -u root -p tricol_stock -e "DROP TABLE IF EXISTS DATABASECHANGELOG;"
mysql -u root -p tricol_stock -e "DROP TABLE IF EXISTS DATABASECHANGELOGLOCK;"

# Redémarrer l'application
mvn spring-boot:run
```

---

## Solution 5: Tester avec un nouveau mot de passe

### Générer un nouveau hash BCrypt

**Option A: Utiliser un outil en ligne**
- Aller sur: https://bcrypt-generator.com/
- Entrer: `password`
- Rounds: 10
- Copier le hash généré

**Option B: Utiliser Java**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password");
        System.out.println(hash);
    }
}
```

### Mettre à jour le mot de passe
```sql
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';
```

---

## Solution 6: Script SQL Complet de Réinitialisation

Exécutez ce script pour tout réinitialiser:

```sql
-- 1. Supprimer l'ancien admin s'il existe
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username = 'admin');
DELETE FROM users WHERE username = 'admin';

-- 2. Créer l'utilisateur admin
INSERT INTO users (username, email, password, first_name, last_name, enabled)
VALUES ('admin', 'admin@tricol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Tricol', true);

-- 3. Assigner le rôle ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';

-- 4. Vérifier
SELECT u.id, u.username, u.email, u.enabled, r.name as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';
```

---

## Solution 7: Vérifier les logs de l'application

### Activer les logs de debug pour Spring Security

**application.properties:**
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.tricol.stock=DEBUG
```

### Redémarrer l'application et vérifier les logs
```bash
mvn spring-boot:run
```

Cherchez dans les logs:
- `User not found: admin` → L'utilisateur n'existe pas
- `Bad credentials` → Le mot de passe est incorrect
- `User account is disabled` → enabled=false

---

## Test Final

### Avec curl
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

### Avec Postman
1. Méthode: POST
2. URL: `http://localhost:8080/api/auth/login`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "username": "admin",
  "password": "password"
}
```

### Réponse attendue (succès)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "username": "admin",
  "email": "admin@tricol.com"
}
```

---

## Checklist de Vérification

- [ ] La base de données `tricol_stock` existe
- [ ] Les tables de sécurité existent (users, roles, permissions, etc.)
- [ ] L'utilisateur `admin` existe dans la table `users`
- [ ] Le champ `enabled` est à `true` pour l'admin
- [ ] L'admin a le rôle `ADMIN` dans `user_roles`
- [ ] Le mot de passe est le hash BCrypt correct
- [ ] L'application démarre sans erreur
- [ ] Le port 8080 est disponible

---

## Informations de Connexion par Défaut

| Champ | Valeur |
|-------|--------|
| **Username** | admin |
| **Password** | password |
| **Email** | admin@tricol.com |
| **Rôle** | ADMIN |
| **Enabled** | true |
| **Hash BCrypt** | $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy |

---

## Contact Support

Si le problème persiste après avoir suivi toutes ces étapes:

1. Vérifiez les logs de l'application
2. Vérifiez que MySQL est démarré
3. Vérifiez la configuration dans `application.properties`
4. Assurez-vous que le port 8080 n'est pas utilisé par une autre application
