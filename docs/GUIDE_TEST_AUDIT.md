# 📝 Guide de Test du Système d'Audit

## 🎯 Objectif

Vérifier que le système d'audit trace correctement:
- ✅ Connexions/Déconnexions
- ✅ Actions sensibles (CREATE, UPDATE, DELETE)
- ✅ Modifications de permissions
- ✅ Qui a fait quoi et quand

---

## 📋 ÉTAPE 1: Vérifier la Table audit_logs (2 min)

### 1.1 Vérifier que la table existe

```sql
-- Connectez-vous à MySQL
mysql -u root -p
USE tricol_stock_db;

-- Voir la structure de la table
DESCRIBE audit_logs;
```

**Résultat attendu:**
```
+-------------+--------------+
| Field       | Type         |
+-------------+--------------+
| id          | bigint       |
| username    | varchar(50)  |
| action      | varchar(50)  |
| resource    | varchar(100) |
| resource_id | bigint       |
| old_value   | text         |
| new_value   | text         |
| ip_address  | varchar(45)  |
| timestamp   | datetime     |
+-------------+--------------+
```

---

## 🔐 ÉTAPE 2: Tester Audit des Connexions (10 min)

### 2.1 Login avec Admin

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/login
Body:
{
  "username": "admin",
  "password": "password"
}
```

### 2.2 Vérifier l'Audit Log

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'USER_LOGIN' 
ORDER BY timestamp DESC 
LIMIT 5;
```

**Résultat attendu:**
```
+----+----------+-------------+----------+-------------+
| id | username | action      | resource | timestamp   |
+----+----------+-------------+----------+-------------+
|  1 | admin    | USER_LOGIN  | User     | 2024-12-21  |
+----+----------+-------------+----------+-------------+
```

✅ **Vérifier:** username = admin, action = USER_LOGIN

---

### 2.3 Logout

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/logout
Headers: Authorization: Bearer {token}
```

### 2.4 Vérifier l'Audit Logout

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'USER_LOGOUT' 
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----+----------+--------------+----------+-------------+
| id | username | action       | resource | timestamp   |
+----+----------+--------------+----------+-------------+
|  2 | admin    | USER_LOGOUT  | User     | 2024-12-21  |
+----+----------+--------------+----------+-------------+
```

---

### 2.5 Tentative de Login Échouée

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/login
Body:
{
  "username": "admin",
  "password": "wrong_password"
}
```

### 2.6 Vérifier l'Audit Login Failed

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'USER_LOGIN_FAILED' 
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----+----------+--------------------+----------+
| id | username | action             | resource |
+----+----------+--------------------+----------+
|  3 | admin    | USER_LOGIN_FAILED  | User     |
+----+----------+--------------------+----------+
```

---

## 📝 ÉTAPE 3: Tester Audit des Actions CRUD (20 min)

### 3.1 Créer un Fournisseur

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs
Headers: Authorization: Bearer {token}
Body:
{
  "nom": "Test Audit",
  "adresse": "Casa",
  "telephone": "0522",
  "email": "audit@test.ma"
}
```

### 3.2 Vérifier l'Audit CREATE

**SQL:**
```sql
SELECT 
    username,
    action,
    resource,
    resource_id,
    new_value,
    timestamp
FROM audit_logs 
WHERE action = 'CREATE' AND resource = 'FOURNISSEUR'
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----------+--------+-------------+-------------+------------------+
| username | action | resource    | resource_id | new_value        |
+----------+--------+-------------+-------------+------------------+
| admin    | CREATE | FOURNISSEUR | 1           | {"nom":"Test...} |
+----------+--------+-------------+-------------+------------------+
```

✅ **Vérifier:**
- username = admin
- action = CREATE
- resource = FOURNISSEUR
- resource_id = ID du fournisseur créé
- new_value contient les données JSON

---

### 3.3 Modifier le Fournisseur

**Postman:**
```
Method: PUT
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs/1
Body:
{
  "nom": "Test Audit Updated",
  "adresse": "Rabat",
  "telephone": "0537",
  "email": "audit2@test.ma"
}
```

### 3.4 Vérifier l'Audit UPDATE

**SQL:**
```sql
SELECT 
    username,
    action,
    resource,
    resource_id,
    old_value,
    new_value,
    timestamp
FROM audit_logs 
WHERE action = 'UPDATE' AND resource = 'FOURNISSEUR'
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----------+--------+-------------+-------------+-------------------+-------------------+
| username | action | resource    | resource_id | old_value         | new_value         |
+----------+--------+-------------+-------------+-------------------+-------------------+
| admin    | UPDATE | FOURNISSEUR | 1           | {"nom":"Test...}  | {"nom":"Test...}  |
+----------+--------+-------------+-------------+-------------------+-------------------+
```

✅ **Vérifier:**
- old_value contient les anciennes données
- new_value contient les nouvelles données

---

### 3.5 Supprimer le Fournisseur

**Postman:**
```
Method: DELETE
URL: http://localhost:8081/tricol-stock/api/v1/fournisseurs/1
```

### 3.6 Vérifier l'Audit DELETE

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'DELETE' AND resource = 'FOURNISSEUR'
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----------+--------+-------------+-------------+-------------------+
| username | action | resource    | resource_id | old_value         |
+----------+--------+-------------+-------------+-------------------+
| admin    | DELETE | FOURNISSEUR | 1           | {"nom":"Test...}  |
+----------+--------+-------------+-------------+-------------------+
```

---

## 🔄 ÉTAPE 4: Tester Audit des Commandes (15 min)

### 4.1 Créer une Commande

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/v1/commandes
Body:
{
  "fournisseurId": 1,
  "dateCommande": "2024-12-21",
  "lignes": [...]
}
```

### 4.2 Vérifier l'Audit

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'CREATE' AND resource = 'COMMANDE'
ORDER BY timestamp DESC 
LIMIT 1;
```

---

### 4.3 Valider la Commande

**Postman:**
```
Method: PATCH
URL: http://localhost:8081/tricol-stock/api/v1/commandes/1/statut?statut=VALIDEE
```

### 4.4 Vérifier l'Audit VALIDATE

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'VALIDATE' AND resource = 'COMMANDE'
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----------+----------+----------+-------------+
| username | action   | resource | resource_id |
+----------+----------+----------+-------------+
| admin    | VALIDATE | COMMANDE | 1           |
+----------+----------+----------+-------------+
```

---

### 4.5 Réceptionner la Commande

**Postman:**
```
Method: PUT
URL: http://localhost:8081/tricol-stock/api/v1/commandes/1/reception
```

### 4.6 Vérifier l'Audit RECEPTION

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'RECEPTION' AND resource = 'COMMANDE'
ORDER BY timestamp DESC 
LIMIT 1;
```

---

## 👤 ÉTAPE 5: Tester Audit des Utilisateurs (15 min)

### 5.1 Créer un Nouvel Utilisateur

**Postman:**
```
Method: POST
URL: http://localhost:8081/tricol-stock/api/auth/register
Body:
{
  "username": "testaudit",
  "email": "testaudit@tricol.com",
  "password": "password123"
}
```

### 5.2 Vérifier l'Audit

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'USER_REGISTER'
ORDER BY timestamp DESC 
LIMIT 1;
```

**Résultat attendu:**
```
+----------+----------------+----------+
| username | action         | resource |
+----------+----------------+----------+
| testaudit| USER_REGISTER  | User     |
+----------+----------------+----------+
```

---

### 5.3 Modifier les Permissions (Simulation)

**SQL:**
```sql
-- Ajouter une permission personnalisée
INSERT INTO user_permissions (user_id, permission_id, granted)
SELECT u.id, p.id, false
FROM users u, permissions p
WHERE u.username = 'testaudit' AND p.name = 'CREATE_FOURNISSEUR';

-- Simuler l'audit (normalement fait par l'application)
INSERT INTO audit_logs (username, action, resource, resource_id, old_value, new_value, timestamp)
VALUES ('admin', 'PERMISSION_REVOKED', 'USER_PERMISSION', 
        (SELECT id FROM users WHERE username = 'testaudit'),
        '{"permission":"CREATE_FOURNISSEUR","granted":true}',
        '{"permission":"CREATE_FOURNISSEUR","granted":false}',
        NOW());
```

### 5.4 Vérifier l'Audit des Permissions

**SQL:**
```sql
SELECT * FROM audit_logs 
WHERE action = 'PERMISSION_REVOKED'
ORDER BY timestamp DESC 
LIMIT 1;
```

---

## 📊 ÉTAPE 6: Requêtes d'Analyse (10 min)

### 6.1 Voir Toutes les Actions d'un Utilisateur

**SQL:**
```sql
SELECT 
    action,
    resource,
    resource_id,
    timestamp
FROM audit_logs 
WHERE username = 'admin'
ORDER BY timestamp DESC;
```

---

### 6.2 Voir l'Historique d'une Ressource

**SQL:**
```sql
SELECT 
    username,
    action,
    old_value,
    new_value,
    timestamp
FROM audit_logs 
WHERE resource = 'FOURNISSEUR' AND resource_id = 1
ORDER BY timestamp ASC;
```

**Résultat attendu:**
```
+----------+--------+-------------+-------------+-------------+
| username | action | old_value   | new_value   | timestamp   |
+----------+--------+-------------+-------------+-------------+
| admin    | CREATE | NULL        | {...}       | 10:00:00    |
| admin    | UPDATE | {...}       | {...}       | 10:05:00    |
| admin    | DELETE | {...}       | NULL        | 10:10:00    |
+----------+--------+-------------+-------------+-------------+
```

---

### 6.3 Statistiques des Actions

**SQL:**
```sql
SELECT 
    action,
    COUNT(*) as nombre,
    COUNT(DISTINCT username) as nb_users
FROM audit_logs
GROUP BY action
ORDER BY nombre DESC;
```

**Résultat attendu:**
```
+-------------------+--------+----------+
| action            | nombre | nb_users |
+-------------------+--------+----------+
| USER_LOGIN        | 15     | 3        |
| CREATE            | 10     | 2        |
| UPDATE            | 8      | 2        |
| USER_LOGOUT       | 7      | 3        |
| DELETE            | 3      | 1        |
+-------------------+--------+----------+
```

---

### 6.4 Actions des Dernières 24h

**SQL:**
```sql
SELECT 
    username,
    action,
    resource,
    timestamp
FROM audit_logs
WHERE timestamp > NOW() - INTERVAL 24 HOUR
ORDER BY timestamp DESC;
```

---

### 6.5 Utilisateurs les Plus Actifs

**SQL:**
```sql
SELECT 
    username,
    COUNT(*) as nb_actions,
    MIN(timestamp) as premiere_action,
    MAX(timestamp) as derniere_action
FROM audit_logs
GROUP BY username
ORDER BY nb_actions DESC;
```

---

### 6.6 Actions par Ressource

**SQL:**
```sql
SELECT 
    resource,
    action,
    COUNT(*) as nombre
FROM audit_logs
GROUP BY resource, action
ORDER BY resource, nombre DESC;
```

---

## 🔍 ÉTAPE 7: Vérifier l'IP Address (5 min)

### 7.1 Voir les IPs des Connexions

**SQL:**
```sql
SELECT 
    username,
    action,
    ip_address,
    timestamp
FROM audit_logs
WHERE action IN ('USER_LOGIN', 'USER_LOGOUT')
ORDER BY timestamp DESC
LIMIT 10;
```

**Résultat attendu:**
```
+----------+-------------+-------------+-------------+
| username | action      | ip_address  | timestamp   |
+----------+-------------+-------------+-------------+
| admin    | USER_LOGOUT | 127.0.0.1   | 10:30:00    |
| admin    | USER_LOGIN  | 127.0.0.1   | 10:00:00    |
+----------+-------------+-------------+-------------+
```

---

## ✅ ÉTAPE 8: Checklist de Vérification

### Audit des Connexions
- [ ] USER_LOGIN enregistré ✅
- [ ] USER_LOGOUT enregistré ✅
- [ ] USER_LOGIN_FAILED enregistré ✅
- [ ] USER_REGISTER enregistré ✅
- [ ] IP address capturée ✅

### Audit des Actions CRUD
- [ ] CREATE enregistré avec new_value ✅
- [ ] UPDATE enregistré avec old_value et new_value ✅
- [ ] DELETE enregistré avec old_value ✅
- [ ] Username correct ✅
- [ ] Timestamp correct ✅

### Audit des Actions Spéciales
- [ ] VALIDATE commande enregistré ✅
- [ ] RECEPTION commande enregistré ✅
- [ ] Modifications permissions enregistrées ✅

### Requêtes d'Analyse
- [ ] Historique par utilisateur fonctionne ✅
- [ ] Historique par ressource fonctionne ✅
- [ ] Statistiques fonctionnent ✅
- [ ] Filtres par date fonctionnent ✅

---

## 📊 ÉTAPE 9: Dashboard Audit (Bonus)

### 9.1 Créer une Vue Synthétique

**SQL:**
```sql
-- Actions du jour
SELECT 
    DATE(timestamp) as date,
    action,
    COUNT(*) as nombre
FROM audit_logs
WHERE DATE(timestamp) = CURDATE()
GROUP BY DATE(timestamp), action;

-- Top 5 utilisateurs actifs
SELECT 
    username,
    COUNT(*) as actions
FROM audit_logs
WHERE timestamp > NOW() - INTERVAL 7 DAY
GROUP BY username
ORDER BY actions DESC
LIMIT 5;

-- Ressources les plus modifiées
SELECT 
    resource,
    COUNT(*) as modifications
FROM audit_logs
WHERE action IN ('CREATE', 'UPDATE', 'DELETE')
GROUP BY resource
ORDER BY modifications DESC;
```

---

## 🎉 RÉSULTAT FINAL

Si toutes les vérifications passent:

```
╔═══════════════════════════════════════════╗
║  ✅ SYSTÈME D'AUDIT 100% FONCTIONNEL     ║
║                                           ║
║  ✓ Connexions/Déconnexions tracées       ║
║  ✓ Actions CRUD tracées                  ║
║  ✓ Modifications permissions tracées     ║
║  ✓ Qui, Quoi, Quand enregistré           ║
║  ✓ IP Address capturée                   ║
║  ✓ Historique complet disponible         ║
║                                           ║
║  L'AUDIT EST OPÉRATIONNEL! 📝            ║
╚═══════════════════════════════════════════╝
```

---

## 📝 Commandes Rapides

```sql
-- Voir tous les logs
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 20;

-- Logs d'un utilisateur
SELECT * FROM audit_logs WHERE username = 'admin' ORDER BY timestamp DESC;

-- Logs d'aujourd'hui
SELECT * FROM audit_logs WHERE DATE(timestamp) = CURDATE();

-- Compter les actions
SELECT action, COUNT(*) FROM audit_logs GROUP BY action;

-- Nettoyer les vieux logs (> 90 jours)
DELETE FROM audit_logs WHERE timestamp < NOW() - INTERVAL 90 DAY;
```

**Temps total estimé:** 1 heure 30 minutes
