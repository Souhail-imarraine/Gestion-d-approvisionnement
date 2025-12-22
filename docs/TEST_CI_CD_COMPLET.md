# 🧪 Guide de Test Complet - CI/CD Pipeline

## 📋 Prérequis

- [ ] Compte GitHub
- [ ] Compte Docker Hub (gratuit)
- [ ] Git installé
- [ ] Docker installé (pour tests locaux)

---

## 🎯 Partie 1: Préparation (10 min)

### Étape 1.1: Créer un Token Docker Hub

```bash
1. Aller sur: https://hub.docker.com/settings/security
2. Cliquer "New Access Token"
3. Nom: github-actions
4. Permissions: Read, Write, Delete
5. Cliquer "Generate"
6. ⚠️ COPIER LE TOKEN (vous ne le reverrez plus!)
```

**Résultat attendu:**
```
Token: dckr_pat_xxxxxxxxxxxxxxxxxxxxx
```

---

### Étape 1.2: Créer un Repository GitHub

```bash
1. Aller sur: https://github.com/new
2. Repository name: tricol-stock
3. Description: Système de gestion d'approvisionnement Tricol
4. Public ou Private (au choix)
5. Cliquer "Create repository"
```

**Résultat attendu:**
```
✅ Repository créé: https://github.com/VOTRE_USERNAME/tricol-stock
```

---

### Étape 1.3: Configurer les Secrets GitHub

```bash
1. Aller sur votre repo: https://github.com/VOTRE_USERNAME/tricol-stock
2. Settings → Secrets and variables → Actions
3. Cliquer "New repository secret"

Secret 1:
- Name: DOCKER_USERNAME
- Value: votre_username_dockerhub
- Cliquer "Add secret"

Secret 2:
- Name: DOCKER_PASSWORD
- Value: le_token_copié_étape_1.1
- Cliquer "Add secret"
```

**Vérification:**
```
✅ 2 secrets configurés:
   - DOCKER_USERNAME
   - DOCKER_PASSWORD
```

---

## 🧪 Partie 2: Tests Locaux (15 min)

### Étape 2.1: Tester les Tests Unitaires

```bash
# Dans le dossier du projet
cd "C:\Users\LENOVO\Desktop\New folder\Gestion_approvisionnement"

# Exécuter les tests
mvn clean test
```

**Résultat attendu:**
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**❌ Si échec:**
```bash
# Voir les détails
mvn test -X

# Corriger les erreurs et relancer
mvn clean test
```

---

### Étape 2.2: Tester le Build Maven

```bash
# Build complet
mvn clean package -DskipTests
```

**Résultat attendu:**
```
[INFO] Building jar: target/tricol-stock-0.0.1-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

**Vérification:**
```bash
# Vérifier que le JAR existe
dir target\*.jar
```

---

### Étape 2.3: Tester Docker Build Local

```bash
# Build l'image Docker
docker build -t tricol-stock:test .
```

**Résultat attendu:**
```
[+] Building 120.5s (14/14) FINISHED
 => => naming to docker.io/library/tricol-stock:test
```

**Vérification:**
```bash
# Voir l'image
docker images | findstr tricol-stock

# Résultat attendu:
# tricol-stock   test   abc123   2 minutes ago   350MB
```

---

### Étape 2.4: Tester l'Image Docker

```bash
# Lancer le conteneur
docker run -d --name tricol-test -p 8081:8081 tricol-stock:test

# Attendre 30 secondes
timeout /t 30

# Voir les logs
docker logs tricol-test

# Tester l'API
curl http://localhost:8081/tricol-stock/api/auth/login

# Nettoyer
docker stop tricol-test
docker rm tricol-test
```

**Résultat attendu:**
```
✅ Logs montrent: "Started StockApplication"
✅ curl retourne une réponse (même si 401)
```

---

## 🚀 Partie 3: Push vers GitHub (5 min)

### Étape 3.1: Initialiser Git (si pas déjà fait)

```bash
# Vérifier si Git est initialisé
git status

# Si erreur "not a git repository":
git init
git add .
git commit -m "Initial commit with CI/CD"
```

---

### Étape 3.2: Lier au Repository GitHub

```bash
# Remplacer VOTRE_USERNAME par votre username GitHub
git remote add origin https://github.com/VOTRE_USERNAME/tricol-stock.git

# Vérifier
git remote -v
```

**Résultat attendu:**
```
origin  https://github.com/VOTRE_USERNAME/tricol-stock.git (fetch)
origin  https://github.com/VOTRE_USERNAME/tricol-stock.git (push)
```

---

### Étape 3.3: Push le Code

```bash
# Vérifier la branche actuelle
git branch

# Si vous êtes sur Junit_testing:
git push -u origin Junit_testing

# Si vous êtes sur main/master:
git push -u origin main
```

**Résultat attendu:**
```
Enumerating objects: 150, done.
Writing objects: 100% (150/150), 50.00 KiB | 5.00 MiB/s, done.
To https://github.com/VOTRE_USERNAME/tricol-stock.git
 * [new branch]      Junit_testing -> Junit_testing
```

---

## 🔍 Partie 4: Vérifier GitHub Actions (10 min)

### Étape 4.1: Accéder à GitHub Actions

```bash
1. Aller sur: https://github.com/VOTRE_USERNAME/tricol-stock
2. Cliquer sur l'onglet "Actions"
3. Vous devriez voir un workflow en cours
```

**Résultat attendu:**
```
⚙️ CI/CD Pipeline
#1 · Junit_testing
⏳ In progress...
```

---

### Étape 4.2: Suivre l'Exécution

```bash
1. Cliquer sur le workflow en cours
2. Vous verrez 2 jobs:

Job 1: Build Application
├─ ⏳ Checkout code
├─ ⏳ Set up JDK 17
├─ ⏳ Build with Maven
├─ ⏳ Run Tests
└─ ⏳ Upload artifact

Job 2: Docker Build & Push (attend Job 1)
├─ ⏳ Checkout code
├─ ⏳ Set up Docker Buildx
├─ ⏳ Login to Docker Hub
├─ ⏳ Extract metadata
└─ ⏳ Build and push Docker image
```

**Temps estimé:** 5-8 minutes

---

### Étape 4.3: Vérifier les Logs

```bash
1. Cliquer sur "Build Application"
2. Cliquer sur "Run Tests"
3. Vérifier la sortie:
```

**Résultat attendu:**
```
Run mvn test
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

```bash
4. Cliquer sur "Docker Build & Push"
5. Cliquer sur "Build and push Docker image"
6. Vérifier la sortie:
```

**Résultat attendu:**
```
#14 pushing layers
#14 pushing manifest for docker.io/VOTRE_USERNAME/tricol-stock:latest
#14 DONE
```

---

### Étape 4.4: Résultat Final

**✅ Succès:**
```
✅ Build Application (2m 30s)
✅ Docker Build & Push (3m 45s)

Total: 6m 15s
```

**❌ Échec - Diagnostics:**

| Erreur | Cause | Solution |
|--------|-------|----------|
| Tests failed | Tests unitaires échouent | `mvn test` localement |
| Docker login failed | Secrets incorrects | Vérifier DOCKER_USERNAME/PASSWORD |
| Permission denied | Token Docker invalide | Recréer le token |
| Image not found | Nom d'image incorrect | Vérifier workflow YAML |

---

## 🐳 Partie 5: Vérifier Docker Hub (5 min)

### Étape 5.1: Accéder à Docker Hub

```bash
1. Aller sur: https://hub.docker.com
2. Login avec votre compte
3. Cliquer sur "Repositories"
```

**Résultat attendu:**
```
✅ Repository visible: votre_username/tricol-stock
```

---

### Étape 5.2: Vérifier les Tags

```bash
1. Cliquer sur "tricol-stock"
2. Onglet "Tags"
```

**Résultat attendu:**
```
Tags disponibles:
├─ latest (il y a 2 minutes)
├─ Junit_testing (il y a 2 minutes)
└─ sha-abc1234 (il y a 2 minutes)

Size: ~350 MB
```

---

### Étape 5.3: Voir les Détails

```bash
1. Cliquer sur "latest"
2. Vérifier:
   - Digest: sha256:xxxxx
   - OS/Arch: linux/amd64
   - Pushed: il y a X minutes
```

---

## 🧪 Partie 6: Test Final - Pull & Run (10 min)

### Étape 6.1: Pull l'Image depuis Docker Hub

```bash
# Supprimer l'image locale (si existe)
docker rmi votre_username/tricol-stock:latest

# Pull depuis Docker Hub
docker pull votre_username/tricol-stock:latest
```

**Résultat attendu:**
```
latest: Pulling from votre_username/tricol-stock
abc123: Pull complete
def456: Pull complete
Status: Downloaded newer image for votre_username/tricol-stock:latest
```

---

### Étape 6.2: Lancer avec Docker Compose

```bash
# Démarrer MySQL + App
docker-compose up -d

# Voir les logs
docker-compose logs -f app
```

**Résultat attendu:**
```
tricol-stock  | Started StockApplication in 45.123 seconds
```

---

### Étape 6.3: Tester l'API

```bash
# Test 1: Login
curl -X POST http://localhost:8081/tricol-stock/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"password\"}"
```

**Résultat attendu:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

```bash
# Test 2: Créer Fournisseur (avec le token)
curl -X POST http://localhost:8081/tricol-stock/api/v1/fournisseurs \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"nom\":\"Test\",\"adresse\":\"Casa\",\"telephone\":\"0522\",\"email\":\"test@test.ma\"}"
```

**Résultat attendu:**
```json
{
  "id": 1,
  "nom": "Test",
  "adresse": "Casa",
  "telephone": "0522",
  "email": "test@test.ma"
}
```

---

## ✅ Checklist Finale

### Préparation
- [ ] Token Docker Hub créé
- [ ] Repository GitHub créé
- [ ] Secrets GitHub configurés (DOCKER_USERNAME, DOCKER_PASSWORD)

### Tests Locaux
- [ ] `mvn test` → 8 tests passent
- [ ] `mvn package` → JAR créé
- [ ] `docker build` → Image créée
- [ ] `docker run` → Conteneur démarre

### GitHub Actions
- [ ] Code pushé vers GitHub
- [ ] Workflow déclenché automatiquement
- [ ] Job "Build Application" ✅
- [ ] Job "Docker Build & Push" ✅
- [ ] Durée totale: 5-8 minutes

### Docker Hub
- [ ] Image visible sur Docker Hub
- [ ] Tags présents: latest, branche, sha
- [ ] Taille: ~350 MB

### Test Final
- [ ] `docker pull` fonctionne
- [ ] `docker-compose up` démarre
- [ ] API Login fonctionne
- [ ] API CRUD fonctionne

---

## 🎉 Résultat Final

Si toutes les cases sont cochées ✅:

```
🎉 FÉLICITATIONS! 🎉

Votre CI/CD Pipeline est 100% fonctionnel!

Maintenant, à chaque push:
1. Tests automatiques
2. Build automatique
3. Docker image automatique
4. Push vers Docker Hub automatique

Votre application est prête pour la production! 🚀
```

---

## 📊 Temps Total Estimé

| Partie | Temps |
|--------|-------|
| 1. Préparation | 10 min |
| 2. Tests Locaux | 15 min |
| 3. Push GitHub | 5 min |
| 4. GitHub Actions | 10 min |
| 5. Docker Hub | 5 min |
| 6. Test Final | 10 min |
| **TOTAL** | **55 min** |

---

## 🆘 Support

**Problèmes courants:**

```bash
# Tests échouent
mvn clean test -X

# Docker build échoue
docker build --no-cache -t tricol-stock .

# GitHub Actions échoue
# → Vérifier les logs dans Actions
# → Vérifier les secrets

# Docker Hub push échoue
# → Vérifier le token
# → Vérifier les permissions
```

**Besoin d'aide?**
- Logs GitHub Actions: https://github.com/VOTRE_USERNAME/tricol-stock/actions
- Logs Docker: `docker logs tricol-stock`
- Logs App: `docker-compose logs -f`

---

**Bonne chance! 🚀**
