# 🚀 Guide Complet - Docker & CI/CD

## 📋 Table des Matières

1. [Tests Unitaires](#tests-unitaires)
2. [Docker](#docker)
3. [GitHub Actions CI/CD](#github-actions-cicd)

---

## 1️⃣ Tests Unitaires

### Fichier Créé
- `src/test/java/com/tricol/stock/service/AuthServiceTest.java`

### Tests Implémentés (8 tests)
✅ `testRegister_Success` - Inscription réussie  
✅ `testRegister_UsernameAlreadyExists` - Username déjà existant  
✅ `testLogin_Success` - Connexion réussie  
✅ `testLogin_InvalidCredentials` - Credentials invalides  
✅ `testRefreshToken_Success` - Refresh token valide  
✅ `testRefreshToken_ExpiredToken` - Token expiré  
✅ `testLogout_Success` - Déconnexion réussie  

### Exécuter les Tests

```bash
# Tous les tests
mvn test

# Tests d'authentification uniquement
mvn test -Dtest=AuthServiceTest

# Avec rapport de couverture
mvn test jacoco:report
```

### Résultat Attendu
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 2️⃣ Docker

### Fichiers Créés
- `Dockerfile` - Multi-stage build (Maven + JRE)
- `.dockerignore` - Exclure fichiers inutiles

### 🔨 Construction de l'Image

```bash
# Build l'image
docker build -t tricol-stock:latest .

# Build avec tag spécifique
docker build -t votre-username/tricol-stock:v1.0 .
```

### 🏃 Exécution du Conteneur

#### Option 1: Sans Base de Données (pour test)
```bash
docker run -d \
  --name tricol-stock \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  tricol-stock:latest
```

#### Option 2: Avec MySQL (Recommandé)
```bash
# 1. Créer un réseau Docker
docker network create tricol-network

# 2. Démarrer MySQL
docker run -d \
  --name tricol-mysql \
  --network tricol-network \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=tricol_stock_db \
  -p 3306:3306 \
  mysql:8.0

# 3. Démarrer l'application
docker run -d \
  --name tricol-stock \
  --network tricol-network \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://tricol-mysql:3306/tricol_stock_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  tricol-stock:latest
```

### 🐳 Docker Compose (Recommandé)

Créer `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: tricol-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: tricol_stock_db
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - tricol-network

  app:
    build: ..
    container_name: tricol-stock
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/tricol_stock_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql
    networks:
      - tricol-network

volumes:
  mysql-data:

networks:
  tricol-network:
```

**Démarrer avec Docker Compose:**
```bash
docker-compose up -d
```

### 📤 Push vers Docker Hub

```bash
# 1. Login Docker Hub
docker login

# 2. Tag l'image
docker tag tricol-stock:latest votre-username/tricol-stock:latest

# 3. Push l'image
docker push votre-username/tricol-stock:latest

# 4. Push avec version
docker tag tricol-stock:latest votre-username/tricol-stock:v1.0
docker push votre-username/tricol-stock:v1.0
```

### 🔍 Commandes Utiles

```bash
# Voir les logs
docker logs tricol-stock

# Logs en temps réel
docker logs -f tricol-stock

# Entrer dans le conteneur
docker exec -it tricol-stock sh

# Arrêter le conteneur
docker stop tricol-stock

# Supprimer le conteneur
docker rm tricol-stock

# Supprimer l'image
docker rmi tricol-stock:latest
```

---

## 3️⃣ GitHub Actions CI/CD

### Fichier Créé
- `.github/workflows/ci-cd.yml`

### Workflow Comprend

#### Job 1: Build
1. ✅ Checkout du code
2. ✅ Setup JDK 17
3. ✅ Build avec Maven
4. ✅ Exécution des tests
5. ✅ Upload de l'artifact

#### Job 2: Docker Build & Push
1. ✅ Checkout du code
2. ✅ Setup Docker Buildx
3. ✅ Login Docker Hub
4. ✅ Build de l'image Docker
5. ✅ Push vers Docker Hub

### 🔐 Configuration des Secrets GitHub

#### Étape 1: Créer un Token Docker Hub
1. Aller sur https://hub.docker.com/settings/security
2. Cliquer "New Access Token"
3. Nom: `github-actions`
4. Copier le token

#### Étape 2: Ajouter les Secrets dans GitHub
1. Aller sur votre repo GitHub
2. Settings → Secrets and variables → Actions
3. Cliquer "New repository secret"

**Ajouter ces 2 secrets:**

| Name | Value |
|------|-------|
| `DOCKER_USERNAME` | Votre username Docker Hub |
| `DOCKER_PASSWORD` | Le token créé à l'étape 1 |

### 🚀 Déclenchement du Workflow

Le workflow se déclenche automatiquement sur:
- ✅ Push sur `main` ou `master`
- ✅ Pull Request vers `main` ou `master`

**Déclencher manuellement:**
```bash
git add .
git commit -m "Add CI/CD pipeline"
git push origin main
```

### 📊 Vérifier l'Exécution

1. Aller sur GitHub → Votre repo
2. Onglet "Actions"
3. Voir le workflow en cours d'exécution
4. Cliquer pour voir les détails

### ✅ Résultat Attendu

```
✓ Build Application (2m 30s)
  ✓ Checkout code
  ✓ Set up JDK 17
  ✓ Build with Maven
  ✓ Run Tests
  ✓ Upload artifact

✓ Docker Build & Push (3m 15s)
  ✓ Checkout code
  ✓ Set up Docker Buildx
  ✓ Login to Docker Hub
  ✓ Build and push Docker image
```

### 🎯 Image Docker Disponible

Après le workflow, votre image sera disponible sur:
```
docker pull votre-username/tricol-stock:latest
docker pull votre-username/tricol-stock:sha-abc123
```

---

## 📝 Checklist Complète

### Tests Unitaires
- [x] AuthServiceTest.java créé
- [ ] Exécuter `mvn test`
- [ ] Vérifier que tous les tests passent

### Docker
- [x] Dockerfile créé
- [x] .dockerignore créé
- [ ] Build l'image: `docker build -t tricol-stock .`
- [ ] Tester localement: `docker run -p 8081:8081 tricol-stock`
- [ ] Login Docker Hub: `docker login`
- [ ] Tag l'image: `docker tag tricol-stock votre-username/tricol-stock`
- [ ] Push l'image: `docker push votre-username/tricol-stock`

### GitHub Actions
- [x] Workflow ci-cd.yml créé
- [ ] Créer token Docker Hub
- [ ] Ajouter DOCKER_USERNAME dans GitHub Secrets
- [ ] Ajouter DOCKER_PASSWORD dans GitHub Secrets
- [ ] Push le code sur GitHub
- [ ] Vérifier que le workflow s'exécute
- [ ] Vérifier l'image sur Docker Hub

---

## 🎯 Commandes Rapides

### Tests
```bash
mvn test
```

### Docker Local
```bash
docker build -t tricol-stock .
docker run -p 8081:8081 tricol-stock
```

### Docker Hub
```bash
docker login
docker tag tricol-stock votre-username/tricol-stock:latest
docker push votre-username/tricol-stock:latest
```

### Docker Compose
```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

### GitHub
```bash
git add .
git commit -m "Add Docker and CI/CD"
git push origin main
```

---

## 🔧 Troubleshooting

### Tests échouent
```bash
# Vérifier les dépendances
mvn dependency:tree

# Nettoyer et rebuild
mvn clean install
```

### Docker build échoue
```bash
# Vérifier les logs
docker build -t tricol-stock . --progress=plain

# Nettoyer le cache
docker builder prune
```

### GitHub Actions échoue
- Vérifier que les secrets sont bien configurés
- Vérifier les logs dans l'onglet Actions
- Vérifier que le Dockerfile est à la racine du projet

---

## 📞 Support

Si vous rencontrez des problèmes:
1. Vérifier les logs: `docker logs tricol-stock`
2. Vérifier les secrets GitHub
3. Vérifier que Docker Hub est accessible
4. Vérifier que les tests passent localement

**Votre projet est maintenant prêt pour la production!** 🚀
