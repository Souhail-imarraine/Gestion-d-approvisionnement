# AuthController OAuth2/Keycloak Migration - Changes Made

## 📋 Summary of Changes

Your `AuthController` has been successfully updated to work with **Keycloak OAuth2** instead of custom JWT authentication.

---

## 🔄 What Changed in AuthController

### ❌ REMOVED Endpoints:
1. **POST /api/v1/auth/login** - Now handled by Keycloak
2. **POST /api/v1/auth/refresh** - Now handled by Keycloak

### ✅ UPDATED Endpoints:

#### 1. **POST /api/v1/auth/register**
- **Before:** Created user in database with encoded password
- **After:** Creates user in Keycloak realm
- **Usage:** Same request format, but user is now in Keycloak

#### 2. **POST /api/v1/auth/logout**
- **Before:** Used `Authentication` parameter, deleted refresh tokens from database
- **After:** Uses `@AuthenticationPrincipal Jwt`, logs audit only
- **Note:** Actual logout handled by Keycloak

### ✅ NEW Endpoints:

#### 3. **GET /api/v1/auth/me**
- Returns current user info from JWT token
- Shows: userId, username, email, roles, scopes, token expiration
- **Example Response:**
```json
{
  "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "username": "admin",
  "email": "admin@tricol.com",
  "emailVerified": true,
  "name": "Admin User",
  "firstName": "Admin",
  "lastName": "User",
  "roles": ["ADMIN", "USER"],
  "scopes": ["openid", "profile", "email"],
  "tokenIssuedAt": "2024-12-26T10:30:00Z",
  "tokenExpiresAt": "2024-12-26T10:45:00Z"
}
```

#### 4. **GET /api/v1/auth/health**
- Public endpoint to check auth system status
- No authentication required

#### 5. **GET /api/v1/auth/config**
- Returns Keycloak configuration for frontend
- Provides URLs for token, logout, userinfo endpoints

---

## 📝 Files Created/Modified

### 1. ✅ **AuthController.java** (Modified)
**Location:** `src/main/java/com/tricol/stock/controller/AuthController.java`

**Key Changes:**
- Uses `@AuthenticationPrincipal Jwt` instead of `Authentication`
- Removed dependency on `AuthService`
- Added dependency on `KeycloakAuthService`
- Extracts user info from JWT claims instead of database

### 2. ✅ **KeycloakAuthService.java** (Created)
**Location:** `src/main/java/com/tricol/stock/service/KeycloakAuthService.java`

**Purpose:**
- Manages user registration in Keycloak
- Uses Keycloak Admin Client API
- Assigns default roles to new users
- Validates username/email uniqueness in Keycloak

---

## 🔧 How to Use the New AuthController

### 1. **Register a New User**
```bash
POST http://localhost:8081/tricol-stock/api/v1/auth/register
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "userId": "abc123...",
  "username": "john",
  "email": "john@example.com",
  "message": "User registered successfully",
  "info": "Please login using Keycloak token endpoint"
}
```

### 2. **Login (Get Token from Keycloak)**
```bash
POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=tricol-stock-app
&client_secret=YOUR_CLIENT_SECRET
&username=john
&password=SecurePass123!
&grant_type=password
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cC...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cC...",
  "token_type": "Bearer"
}
```

### 3. **Get Current User Info**
```bash
GET http://localhost:8081/tricol-stock/api/v1/auth/me
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cC...
```

### 4. **Use Protected Endpoints**
```bash
GET http://localhost:8081/tricol-stock/api/v1/produits
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cC...
```

### 5. **Logout**
```bash
# First, call your app's logout (for audit logging)
POST http://localhost:8081/tricol-stock/api/v1/auth/logout
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cC...

# Then, revoke token with Keycloak
POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/logout
Content-Type: application/x-www-form-urlencoded

client_id=tricol-stock-app
&client_secret=YOUR_CLIENT_SECRET
&refresh_token=eyJhbGciOiJIUzI1NiIsInR5cC...
```

---

## ⚙️ Next Steps to Complete Integration

### Step 1: Update application.properties
Add Keycloak configuration (if not already done):

```properties
# Keycloak Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/tricol-stock
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/realms/tricol-stock/protocol/openid-connect/certs

keycloak.auth-server-url=http://localhost:8180
keycloak.realm=tricol-stock
keycloak.resource=tricol-stock-app
keycloak.credentials.secret=YOUR_CLIENT_SECRET_FROM_KEYCLOAK
```

### Step 2: Create KeycloakSecurityConfig.java
Replace your current `SecurityConfig.java` with Keycloak-compatible configuration.

### Step 3: Update Other Controllers
Update any controllers that use `Authentication` to use `@AuthenticationPrincipal Jwt`:

**Before:**
```java
@GetMapping("/produits")
public List<ProduitDTO> getAll(Authentication auth) {
    String username = auth.getName();
    // ...
}
```

**After:**
```java
@GetMapping("/produits")
public List<ProduitDTO> getAll(@AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getClaimAsString("preferred_username");
    // ...
}
```

### Step 4: Update AuditService
If your `AuditService` uses username, update it to accept Keycloak user info:

```java
// Extract username from JWT
String username = jwt.getClaimAsString("preferred_username");
String userId = jwt.getSubject();
auditService.log("ACTION", "Entity", username);
```

---

## 🧪 Testing the Changes

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Start Keycloak (if not running)
```bash
docker-compose up -d keycloak
```

### 3. Configure Keycloak
- Access: http://localhost:8180
- Login: admin / admin
- Import realm or create manually (see guides)

### 4. Start Your Application
```bash
mvn spring-boot:run
```

### 5. Test Registration
```bash
curl -X POST http://localhost:8081/tricol-stock/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 6. Test Login (via Keycloak)
```bash
curl -X POST http://localhost:8180/realms/tricol-stock/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=tricol-stock-app" \
  -d "client_secret=YOUR_SECRET" \
  -d "username=testuser" \
  -d "password=Test123!" \
  -d "grant_type=password"
```

### 7. Test /me Endpoint
```bash
curl -X GET http://localhost:8081/tricol-stock/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🔍 Key Differences from Old Implementation

| Aspect | Old (Custom JWT) | New (Keycloak OAuth2) |
|--------|-----------------|----------------------|
| **Login** | POST /api/v1/auth/login | POST Keycloak token endpoint |
| **User Storage** | Database (users table) | Keycloak (with optional DB sync) |
| **Token Generation** | JwtService in your app | Keycloak server |
| **Token Validation** | JwtAuthenticationFilter | Spring OAuth2 Resource Server |
| **User Info** | Database query | JWT claims |
| **Refresh Token** | POST /api/v1/auth/refresh | POST Keycloak token endpoint |
| **Password Storage** | BCrypt in database | Keycloak (more secure) |
| **Role Management** | Database tables | Keycloak realm roles |

---

## 📚 Related Documentation

For complete integration guide:
1. **KEYCLOAK_INTEGRATION_GUIDE.md** - Full integration steps
2. **QUICK_START.md** - 30-minute setup guide
3. **MIGRATION_COMPARISON.md** - Side-by-side comparison

---

## ⚠️ Important Notes

1. **Client Secret:** Make sure to get the client secret from Keycloak Admin Console:
   - Clients → tricol-stock-app → Credentials tab

2. **Default Role:** New users are automatically assigned the "USER" role. Make sure this role exists in Keycloak.

3. **Email Verification:** Currently set to `false`. Enable email verification in Keycloak for production.

4. **Backward Compatibility:** Old /login and /refresh endpoints are removed. Update your frontend/API clients.

5. **Database Users:** Existing users in database won't work until migrated to Keycloak.

---

## 🆘 Troubleshooting

### Error: "Keycloak not properly configured"
**Solution:** Check application.properties has correct Keycloak settings

### Error: "Cannot resolve symbol 'KeycloakAuthService'"
**Solution:** Run `mvn clean compile` to rebuild the project

### Error: "Failed to create user"
**Solution:** 
- Ensure Keycloak is running
- Check client secret is correct
- Verify USER role exists in Keycloak realm

---

**Last Updated:** December 26, 2025  
**Status:** ✅ AuthController migration complete

