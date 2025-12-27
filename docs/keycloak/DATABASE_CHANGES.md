# Database Changes for Keycloak Integration

## Summary

The database has been modified to support **hybrid authentication** - users can authenticate either via Keycloak (recommended) or local database (legacy mode).

---

## ✅ Changes Applied

### 1. **New Liquibase Migration File**
**File:** `src/main/resources/db/changelog/017-add-keycloak-integration.xml`

This migration adds:
- ✅ `keycloak_user_id` column (VARCHAR(255), UNIQUE, NULLABLE)
- ✅ Index on `keycloak_user_id` for fast lookups
- ✅ Makes `password` column NULLABLE (no longer required for Keycloak users)

### 2. **Updated UserApp Entity**
**File:** `src/main/java/com/tricol/stock/entity/UserApp.java`

Added:
- ✅ `keycloakUserId` field
- ✅ `isKeycloakUser()` helper method
- ✅ Updated `@Column(nullable = true)` for password

### 3. **Updated UserRepository**
**File:** `src/main/java/com/tricol/stock/repository/UserRepository.java`

Added methods:
- ✅ `findByKeycloakUserId(String keycloakUserId)`
- ✅ `existsByKeycloakUserId(String keycloakUserId)`

### 4. **Updated KeycloakAuthService**
**File:** `src/main/java/com/tricol/stock/service/KeycloakAuthService.java`

Now creates **both**:
- ✅ User in Keycloak (for authentication)
- ✅ User in local database (linked via `keycloak_user_id`)

Added:
- ✅ `linkUserToKeycloak()` method for migrating existing users

---

## 📊 Database Schema Changes

### BEFORE (Old Schema)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Required
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### AFTER (New Schema with Keycloak Support)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NULL,  -- Now nullable
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT 0,
    keycloak_user_id VARCHAR(255) UNIQUE NULL,  -- NEW: Link to Keycloak
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_keycloak_user_id (keycloak_user_id)  -- NEW: Index
);
```

---

## 🔄 User Authentication Modes

### Mode 1: Keycloak Authentication (Recommended)
```java
UserApp user = UserApp.builder()
    .username("john")
    .email("john@example.com")
    .keycloakUserId("f47ac10b-58cc-4372-a567-0e02b2c3d479")
    .password(null)  // No local password
    .enabled(true)
    .build();
```

**Characteristics:**
- ✅ `keycloak_user_id` is SET
- ✅ `password` is NULL
- ✅ Authentication handled by Keycloak
- ✅ `isKeycloakUser()` returns true

### Mode 2: Local Authentication (Legacy)
```java
UserApp user = UserApp.builder()
    .username("olduser")
    .email("old@example.com")
    .keycloakUserId(null)  // Not linked to Keycloak
    .password("$2a$10$...")  // BCrypt password
    .enabled(true)
    .build();
```

**Characteristics:**
- ❌ `keycloak_user_id` is NULL
- ✅ `password` is SET
- ❌ Authentication uses local database
- ✅ `isKeycloakUser()` returns false

---

## 🚀 How It Works Now

### 1. **New User Registration**
When a user registers via `/api/v1/auth/register`:

```java
// 1. User created in Keycloak
String keycloakUserId = keycloak.createUser(...);

// 2. User created in local DB with link
UserApp localUser = UserApp.builder()
    .username("john")
    .keycloakUserId(keycloakUserId)  // Linked!
    .password(null)  // No local password
    .build();
userRepository.save(localUser);
```

**Result:**
- User in Keycloak: ID = `f47ac10b...`
- User in Database: `keycloak_user_id = f47ac10b...`

### 2. **Finding Users**
```java
// By username (works same as before)
UserApp user = userRepository.findByUsername("john");

// By Keycloak ID (new!)
UserApp user = userRepository.findByKeycloakUserId("f47ac10b...");

// Check authentication mode
if (user.isKeycloakUser()) {
    // This user authenticates via Keycloak
} else {
    // This user uses local authentication (legacy)
}
```

---

## 📋 Migration Strategy for Existing Users

You have **3 options** for handling existing users:

### Option 1: Migrate All Users to Keycloak (Recommended)
Create all existing users in Keycloak and link them:

```java
// For each existing user
UserApp user = userRepository.findByUsername("existinguser");

// Create in Keycloak
String keycloakUserId = keycloakAuthService.createUser(...);

// Link to local record
keycloakAuthService.linkUserToKeycloak(user.getUsername(), keycloakUserId);

// Result: user.keycloakUserId is set, user.password is null
```

### Option 2: Gradual Migration
Keep existing users as-is, new users use Keycloak:

```java
// Existing users continue with local auth
// - keycloak_user_id = NULL
// - password = encrypted value

// New users use Keycloak
// - keycloak_user_id = Keycloak UUID
// - password = NULL
```

### Option 3: Hybrid Mode
Allow both authentication methods permanently.

---

## 🛠️ Running the Migration

### Step 1: Apply Database Changes
```bash
# Liquibase will automatically apply the migration
mvn spring-boot:run
```

**What happens:**
1. Column `keycloak_user_id` added to `users` table
2. Index created on `keycloak_user_id`
3. `password` column made nullable
4. Existing users unchanged (all fields keep current values)

### Step 2: Verify Migration
```bash
# Check the migration was applied
mysql -u root -p tricol_stock_db

mysql> DESCRIBE users;
```

Expected output:
```
+------------------+--------------+------+-----+-------------------+
| Field            | Type         | Null | Key | Default           |
+------------------+--------------+------+-----+-------------------+
| id               | bigint       | NO   | PRI | NULL              |
| username         | varchar(50)  | NO   | UNI | NULL              |
| email            | varchar(100) | NO   | UNI | NULL              |
| password         | varchar(255) | YES  |     | NULL              |  ← Now nullable
| first_name       | varchar(50)  | YES  |     | NULL              |
| last_name        | varchar(50)  | YES  |     | NULL              |
| enabled          | tinyint(1)   | NO   |     | 0                 |
| keycloak_user_id | varchar(255) | YES  | UNI | NULL              |  ← New field
| created_at       | timestamp    | YES  |     | CURRENT_TIMESTAMP |
| updated_at       | timestamp    | YES  |     | CURRENT_TIMESTAMP |
+------------------+--------------+------+-----+-------------------+
```

---

## 📝 Example Usage

### Register New User (Creates in Both Keycloak and DB)
```bash
POST /api/v1/auth/register
{
  "username": "newuser",
  "email": "new@example.com",
  "password": "SecurePass123!",
  "firstName": "New",
  "lastName": "User"
}
```

**Response:**
```json
{
  "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",  // Keycloak ID
  "localUserId": 15,  // Database ID
  "username": "newuser",
  "email": "new@example.com",
  "message": "User registered successfully"
}
```

**Database record created:**
```sql
INSERT INTO users (username, email, keycloak_user_id, password, enabled)
VALUES ('newuser', 'new@example.com', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', NULL, 1);
```

### Check User Authentication Mode
```java
UserApp user = userRepository.findByUsername("newuser");

System.out.println(user.isKeycloakUser());  // true
System.out.println(user.getKeycloakUserId());  // "f47ac10b-58cc..."
System.out.println(user.getPassword());  // null
```

---

## 🔍 Benefits of This Approach

### 1. **Zero Downtime Migration**
- Existing users continue working
- New users automatically use Keycloak
- No breaking changes

### 2. **Flexible Migration Path**
- Migrate users one-by-one or in batches
- Test with subset of users first
- Rollback capability

### 3. **App-Specific Data Retained**
- User roles/permissions still in database
- Audit logs still work
- Business logic unchanged

### 4. **Best of Both Worlds**
- Keycloak handles authentication
- Local DB stores app-specific data
- Fast local queries for user info

---

## ⚠️ Important Notes

### 1. **Password Field**
- For Keycloak users: password is NULL (stored in Keycloak)
- For legacy users: password is BCrypt hash (local auth)
- Don't try to authenticate Keycloak users locally

### 2. **Keycloak User ID**
- This is a UUID from Keycloak (e.g., `f47ac10b-58cc-4372-a567-0e02b2c3d479`)
- NOT the same as your database `id` field
- Used to link local record to Keycloak account

### 3. **Existing Users**
- Migration does NOT change existing user data
- All existing users will have `keycloak_user_id = NULL`
- They can continue using local authentication until migrated

### 4. **Rollback**
If you need to rollback:
```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

This will:
- Remove `keycloak_user_id` column
- Remove index
- Make `password` NOT NULL again

---

## 📚 Next Steps

1. ✅ Database changes applied automatically on next startup
2. ⏭️ Test user registration with Keycloak
3. ⏭️ Migrate existing users (optional)
4. ⏭️ Update SecurityConfig to use Keycloak
5. ⏭️ Test authentication flow

---

**Migration File:** `017-add-keycloak-integration.xml`  
**Status:** ✅ Ready to apply  
**Breaking Changes:** None (backward compatible)

