# JWT Implementation Deep Dive - Part 1: Theory & Internal Workings

## Table of Contents
1. [What is JWT?](#what-is-jwt)
2. [JWT Structure Deep Dive](#jwt-structure)
3. [How JWT Works Internally](#how-jwt-works)
4. [Cryptographic Signing Process](#signing-process)
5. [JWT Lifecycle](#jwt-lifecycle)

---

## 1. What is JWT?

**JWT (JSON Web Token)** is a compact, URL-safe token format for securely transmitting information between parties as a JSON object.

### Why JWT?
- **Stateless**: Server doesn't need to store session data
- **Scalable**: Works across multiple servers
- **Self-contained**: Token contains all user information
- **Secure**: Digitally signed to prevent tampering

### JWT vs Session-Based Auth

```
Traditional Session:
┌────────┐                    ┌────────┐
│ Client │ ─── Login ────────>│ Server │
│        │                    │        │
│        │<── Session ID ─────│ Stores │
│        │                    │ in DB  │
│        │                    └────────┘
│ Stores │
│ Cookie │
└────────┘

JWT:
┌────────┐                    ┌────────┐
│ Client │ ─── Login ────────>│ Server │
│        │                    │        │
│        │<── JWT Token ──────│ No DB  │
│        │                    │ Storage│
│ Stores │                    └────────┘
│ Token  │
└────────┘
```

---

## 2. JWT Structure Deep Dive

### JWT Format
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhaG1lZCIsImlhdCI6MTYxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
│                                      │                                    │
│         HEADER (Base64)              │      PAYLOAD (Base64)              │    SIGNATURE
```

### 1. Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- **alg**: Algorithm used for signing (HS256, RS256, etc.)
- **typ**: Token type (always "JWT")

**Base64 Encoded**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9`

### 2. Payload (Claims)
```json
{
  "sub": "ahmed",
  "iat": 1616239022,
  "exp": 1616242622,
  "roles": ["ADMIN", "USER"]
}
```

**Standard Claims:**
- **sub** (subject): User identifier
- **iat** (issued at): Token creation timestamp
- **exp** (expiration): Token expiry timestamp
- **iss** (issuer): Who created the token
- **aud** (audience): Who should receive the token

**Custom Claims:**
- **roles**: User roles
- **permissions**: User permissions
- **email**: User email

**Base64 Encoded**: `eyJzdWIiOiJhaG1lZCIsImlhdCI6MTYxNjIzOTAyMn0`

### 3. Signature
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

**Purpose**: Ensures token hasn't been tampered with

---

## 3. How JWT Works Internally

### Step-by-Step Token Generation

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: Create Header                                       │
├─────────────────────────────────────────────────────────────┤
│ Input:  { "alg": "HS256", "typ": "JWT" }                   │
│ Process: JSON → Base64URL Encoding                          │
│ Output: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9               │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: Create Payload                                      │
├─────────────────────────────────────────────────────────────┤
│ Input:  { "sub": "ahmed", "iat": 1616239022, "exp": ... }  │
│ Process: JSON → Base64URL Encoding                          │
│ Output: eyJzdWIiOiJhaG1lZCIsImlhdCI6MTYxNjIzOTAyMn0       │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: Create Signature                                    │
├─────────────────────────────────────────────────────────────┤
│ Input:  header.payload + secret_key                         │
│ Process: HMAC-SHA256 Hashing                                │
│ Output: SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c        │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: Combine All Parts                                   │
├─────────────────────────────────────────────────────────────┤
│ Final JWT: header.payload.signature                         │
└─────────────────────────────────────────────────────────────┘
```

### Token Validation Process

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: Split Token                                         │
├─────────────────────────────────────────────────────────────┤
│ Input:  eyJhbGc...eyJzdWI...SflKxw                         │
│ Split by "."                                                 │
│ Output: [header, payload, signature]                        │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: Decode Header & Payload                             │
├─────────────────────────────────────────────────────────────┤
│ Base64URL Decode → JSON Objects                             │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: Verify Signature                                    │
├─────────────────────────────────────────────────────────────┤
│ Recreate signature using header + payload + secret          │
│ Compare with received signature                             │
│ If match → Token is valid                                   │
│ If not match → Token is tampered                            │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: Check Expiration                                    │
├─────────────────────────────────────────────────────────────┤
│ Compare exp claim with current time                         │
│ If exp > now → Token is valid                               │
│ If exp < now → Token is expired                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Cryptographic Signing Process

### HS256 (HMAC with SHA-256)

**HMAC** = Hash-based Message Authentication Code

```
┌──────────────────────────────────────────────────────────┐
│ Input Data                                               │
├──────────────────────────────────────────────────────────┤
│ Message: "eyJhbGc...eyJzdWI..."                         │
│ Secret Key: "mySecretKey123"                             │
└──────────────────────────────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│ HMAC-SHA256 Algorithm                                    │
├──────────────────────────────────────────────────────────┤
│ 1. Pad the secret key                                    │
│ 2. XOR with ipad (inner padding)                         │
│ 3. Append message                                        │
│ 4. Hash with SHA-256                                     │
│ 5. XOR key with opad (outer padding)                     │
│ 6. Append previous hash                                  │
│ 7. Hash again with SHA-256                               │
└──────────────────────────────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│ Output: Digital Signature                                │
├──────────────────────────────────────────────────────────┤
│ SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c            │
└──────────────────────────────────────────────────────────┘
```

### Why Signature is Important?

**Without Signature:**
```
Attacker can modify payload:
{ "sub": "hacker", "roles": ["ADMIN"] }
↓
Server accepts it ❌ SECURITY BREACH!
```

**With Signature:**
```
Attacker modifies payload:
{ "sub": "hacker", "roles": ["ADMIN"] }
↓
Signature doesn't match ✅ REJECTED!
```

---

## 5. JWT Lifecycle

### Complete Authentication Flow

```
┌─────────┐                                    ┌─────────┐
│ Client  │                                    │ Server  │
└────┬────┘                                    └────┬────┘
     │                                              │
     │ 1. POST /login                               │
     │    {username, password}                      │
     ├─────────────────────────────────────────────>│
     │                                              │
     │                                              │ 2. Validate credentials
     │                                              │    (Check DB)
     │                                              │
     │                                              │ 3. Generate JWT
     │                                              │    - Create header
     │                                              │    - Create payload
     │                                              │    - Sign with secret
     │                                              │
     │ 4. Return JWT                                │
     │<─────────────────────────────────────────────┤
     │    {accessToken, refreshToken}               │
     │                                              │
     │ 5. Store tokens                              │
     │    (localStorage/memory)                     │
     │                                              │
     │ 6. GET /api/products                         │
     │    Header: Authorization: Bearer <JWT>       │
     ├─────────────────────────────────────────────>│
     │                                              │
     │                                              │ 7. Extract JWT from header
     │                                              │
     │                                              │ 8. Validate JWT
     │                                              │    - Verify signature
     │                                              │    - Check expiration
     │                                              │    - Extract claims
     │                                              │
     │                                              │ 9. Load user from DB
     │                                              │    (using username from JWT)
     │                                              │
     │                                              │ 10. Check permissions
     │                                              │
     │ 11. Return protected data                    │
     │<─────────────────────────────────────────────┤
     │    [products...]                             │
     │                                              │
```

### Token Expiration & Refresh Flow

```
┌─────────┐                                    ┌─────────┐
│ Client  │                                    │ Server  │
└────┬────┘                                    └────┬────┘
     │                                              │
     │ 1. GET /api/products                         │
     │    Authorization: Bearer <expired_token>     │
     ├─────────────────────────────────────────────>│
     │                                              │
     │                                              │ 2. Validate token
     │                                              │    → Token expired!
     │                                              │
     │ 3. 401 Unauthorized                          │
     │<─────────────────────────────────────────────┤
     │    {error: "Token expired"}                  │
     │                                              │
     │ 4. POST /auth/refresh                        │
     │    {refreshToken}                            │
     ├─────────────────────────────────────────────>│
     │                                              │
     │                                              │ 5. Validate refresh token
     │                                              │    - Check DB
     │                                              │    - Check expiration
     │                                              │
     │                                              │ 6. Generate new access token
     │                                              │
     │ 7. Return new access token                   │
     │<─────────────────────────────────────────────┤
     │    {accessToken}                             │
     │                                              │
     │ 8. Retry original request                    │
     │    Authorization: Bearer <new_token>         │
     ├─────────────────────────────────────────────>│
     │                                              │
     │ 9. Success                                   │
     │<─────────────────────────────────────────────┤
     │    [products...]                             │
     │                                              │
```

### Security Considerations

**1. Token Storage**
```
✅ GOOD:
- Memory (most secure, lost on refresh)
- HttpOnly Cookie (protected from XSS)

⚠️ RISKY:
- localStorage (vulnerable to XSS)
- sessionStorage (vulnerable to XSS)
```

**2. Token Expiration**
```
Access Token:  15 minutes (short-lived)
Refresh Token: 7 days (long-lived)

Why?
- Short access token → Less damage if stolen
- Long refresh token → Better UX (less re-login)
```

**3. Secret Key Security**
```
❌ BAD:
jwt.secret=123456

✅ GOOD:
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

Requirements:
- Minimum 256 bits (32 characters)
- Random and unpredictable
- Stored in environment variables
- Never committed to Git
```

---

## Next: Part 2 - Implementation in Your Project

In Part 2, we'll map every concept to your actual code:
- Where JWT is generated in your project
- How validation happens in JwtAuthenticationFilter
- Step-by-step flow through your code
- Comparison with theory
