# JWT Implementation Deep Dive - Part 2: Your Project Implementation

## Table of Contents
1. [Project Architecture Overview](#architecture)
2. [JWT Generation Flow in Your Code](#generation-flow)
3. [JWT Validation Flow in Your Code](#validation-flow)
4. [Complete Request Flow](#complete-flow)
5. [Code Mapping](#code-mapping)

---

## 1. Project Architecture Overview

### Your JWT Components

```
┌─────────────────────────────────────────────────────────────┐
│                    YOUR PROJECT STRUCTURE                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │  AuthController  │────────>│   AuthService    │         │
│  │  /api/v1/auth    │         │  - register()    │         │
│  │  - /login        │         │  - login()       │         │
│  │  - /register     │         │  - refreshToken()│         │
│  │  - /refresh      │         │  - logout()      │         │
│  │  - /logout       │         └────────┬─────────┘         │
│  └──────────────────┘                  │                    │
│                                        │                    │
│                              ┌─────────▼─────────┐          │
│                              │   JwtService      │          │
│                              │  - generateToken()│          │
│                              │  - validateToken()│          │
│                              │  - extractClaims()│          │
│                              └─────────┬─────────┘          │
│                                        │                    │
│  ┌──────────────────────────────────┐ │                    │
│  │  JwtAuthenticationFilter         │ │                    │
│  │  - doFilterInternal()            │◄┘                    │
│  │  - Intercepts ALL requests       │                      │
│  │  - Validates JWT before controller│                     │
│  └──────────────────────────────────┘                      │
│                                                              │
│  ┌──────────────────────────────────┐                      │
│  │  SecurityConfig                  │                      │
│  │  - Configures security rules     │                      │
│  │  - Adds JWT filter               │                      │
│  │  - STATELESS session             │                      │
│  └──────────────────────────────────┘                      │
│                                                              │
│  ┌──────────────────────────────────┐                      │
│  │  Database Entities               │                      │
│  │  - UserApp (implements UserDetails)                     │
│  │  - RefreshToken                  │                      │
│  │  - RoleApp                       │                      │
│  │  - Permission                    │                      │
│  └──────────────────────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. JWT Generation Flow in Your Code

### SCENARIO: User Login

#### Step 1: AuthController receives request
**File**: `AuthController.java` (Line 36-46)

```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    try {
        AuthResponse response = authService.login(request);
        auditService.log("USER_LOGIN", "User", request.getUsername());
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        auditService.log("USER_LOGIN_FAILED", "User", request.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(MessageResponse.of("Invalid credentials"));
    }
}
```

**What happens here:**
```
Client sends: POST /api/v1/auth/login
Body: { "username": "ahmed", "password": "123456" }
         ↓
AuthController.login() is called
         ↓
Calls authService.login(request)
```

---

#### Step 2: AuthService validates credentials
**File**: `AuthService.java` (Line 59-82)

```java
@Transactional
public AuthResponse login(LoginRequest request) {
    // STEP 2.1: Authenticate user
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    
    // STEP 2.2: Get user details
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    UserApp user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    
    // STEP 2.3: Generate JWT tokens
    String accessToken = jwtService.generateToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // STEP 2.4: Save refresh token to database
    saveRefreshToken(user, refreshToken);

    // STEP 2.5: Return response
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(900000L)  // 15 minutes
        .username(user.getUsername())
        .email(user.getEmail())
        .build();
}
```

**What happens here:**
```
1. authenticationManager validates username/password
   - Checks password hash in database
   - If wrong → throws BadCredentialsException
   
2. Loads UserApp from database
   - Gets user roles and permissions
   
3. Calls jwtService.generateToken()
   - Creates access token (15 min)
   
4. Calls jwtService.generateRefreshToken()
   - Creates refresh token (7 days)
   
5. Saves refresh token to database
   - For later validation
   
6. Returns both tokens to client
```

---

#### Step 3: JwtService generates access token
**File**: `JwtService.java` (Line 33-37)

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return buildToken(claims, userDetails.getUsername(), expiration);
}
```

**File**: `JwtService.java` (Line 43-51)

```java
private String buildToken(Map<String, Object> claims, String subject, Long expiration) {
    return Jwts.builder()
        .setClaims(claims)                                    // Empty claims (can add roles here)
        .setSubject(subject)                                  // Username: "ahmed"
        .setIssuedAt(new Date())                             // Current time
        .setExpiration(new Date(System.currentTimeMillis() + expiration))  // +15 min
        .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sign with secret
        .compact();                                           // Build final token
}
```

**What happens here:**
```
Input: UserDetails (username="ahmed")
       expiration=900000 (15 minutes)

Process:
1. Create claims map (empty in your code)
2. Set subject = "ahmed"
3. Set issuedAt = 2024-01-15 10:00:00
4. Set expiration = 2024-01-15 10:15:00
5. Sign with HS256 + secret key
6. Encode to Base64

Output: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhaG1lZCIsImlhdCI6MTcwNTMxNTIwMCwiZXhwIjoxNzA1MzE2MTAwfQ.signature
```

---

#### Step 4: Signing Key Generation
**File**: `JwtService.java` (Line 27-30)

```java
private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

**What happens here:**
```
secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
         ↓
Base64 decode to bytes
         ↓
Create HMAC-SHA256 key
         ↓
Used for signing JWT
```

---

## 3. JWT Validation Flow in Your Code

### SCENARIO: User accesses protected endpoint

#### Step 1: Request arrives at server
```
Client sends: GET /api/v1/produits
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

#### Step 2: JwtAuthenticationFilter intercepts request
**File**: `JwtAuthenticationFilter.java` (Line 28-61)

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
    
    // STEP 2.1: Extract Authorization header
    final String authHeader = request.getHeader("Authorization");
    
    // STEP 2.2: Check if header exists and starts with "Bearer "
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);  // Skip JWT validation
        return;
    }
    
    try {
        // STEP 2.3: Extract JWT token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        
        // STEP 2.4: Extract username from token
        final String username = jwtService.extractUsername(jwt);
        
        // STEP 2.5: Check if user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // STEP 2.6: Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // STEP 2.7: Validate token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // STEP 2.8: Create authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()  // Roles & Permissions
                );
                
                // STEP 2.9: Set authentication in SecurityContext
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    } catch (Exception e) {
        logger.error("Cannot set user authentication: {}", e);
    }
    
    // STEP 2.10: Continue to next filter/controller
    filterChain.doFilter(request, response);
}
```

**What happens here:**
```
1. Get "Authorization" header
   → "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

2. Check if starts with "Bearer "
   → Yes, continue
   → No, skip JWT validation

3. Extract token (remove "Bearer ")
   → "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

4. Extract username from token
   → Calls jwtService.extractUsername(jwt)
   → Returns "ahmed"

5. Check if already authenticated
   → SecurityContext is empty, continue

6. Load user from database
   → userDetailsService.loadUserByUsername("ahmed")
   → Returns UserApp with roles & permissions

7. Validate token
   → jwtService.isTokenValid(jwt, userDetails)
   → Checks signature & expiration

8. Create authentication object
   → UsernamePasswordAuthenticationToken
   → Contains user + authorities

9. Set in SecurityContext
   → Spring Security now knows user is authenticated

10. Continue to controller
    → Request proceeds to ProduitController
```

---

#### Step 3: JwtService extracts username
**File**: `JwtService.java` (Line 53-55)

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}
```

**File**: `JwtService.java` (Line 61-64)

```java
public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}
```

**File**: `JwtService.java` (Line 66-72)

```java
private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
}
```

**What happens here:**
```
Input: JWT token

Process:
1. Parse JWT using secret key
2. Verify signature (if tampered, throws exception)
3. Extract all claims (subject, iat, exp, etc.)
4. Return specific claim (subject = username)

Output: "ahmed"
```

---

#### Step 4: JwtService validates token
**File**: `JwtService.java` (Line 74-77)

```java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}
```

**File**: `JwtService.java` (Line 79-81)

```java
private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
```

**What happens here:**
```
Validation checks:

1. Username matches?
   Token username: "ahmed"
   Database username: "ahmed"
   ✅ Match

2. Token not expired?
   Token exp: 2024-01-15 10:15:00
   Current time: 2024-01-15 10:10:00
   ✅ Not expired

Result: Token is VALID
```

---

#### Step 5: UserApp provides authorities
**File**: `UserApp.java` (Line 62-79)

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    
    // Add role permissions
    for (RoleApp role : roles) {
        for (Permission permission : role.getDefaultPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
        }
    }
    
    // Add/remove custom permissions
    for (UserPermission userPerm : customPermissions) {
        String permName = userPerm.getPermission().getName();
        if (userPerm.isGranted()) {
            authorities.add(new SimpleGrantedAuthority(permName));
        } else {
            authorities.removeIf(auth -> auth.getAuthority().equals(permName));
        }
    }
    
    return authorities;
}
```

**What happens here:**
```
User "ahmed" has:
- Role: ADMIN
- Role permissions: [VIEW_PRODUIT, CREATE_PRODUIT, UPDATE_PRODUIT, DELETE_PRODUIT, ...]
- Custom permissions: [SPECIAL_ACCESS: granted]

Result authorities:
["VIEW_PRODUIT", "CREATE_PRODUIT", "UPDATE_PRODUIT", "DELETE_PRODUIT", ..., "SPECIAL_ACCESS"]
```

---

## 4. Complete Request Flow

### Full Flow Diagram

```
┌──────────┐
│  CLIENT  │
└────┬─────┘
     │
     │ 1. POST /api/v1/auth/login
     │    Body: {username: "ahmed", password: "123456"}
     │
     ▼
┌─────────────────────┐
│  AuthController     │
│  Line 36-46         │
└────┬────────────────┘
     │
     │ 2. authService.login(request)
     │
     ▼
┌─────────────────────┐
│  AuthService        │
│  Line 59-82         │
├─────────────────────┤
│ 3. Authenticate     │
│    credentials      │
│                     │
│ 4. Load UserApp     │
│    from DB          │
└────┬────────────────┘
     │
     │ 5. jwtService.generateToken(userDetails)
     │
     ▼
┌─────────────────────┐
│  JwtService         │
│  Line 33-51         │
├─────────────────────┤
│ 6. Build JWT:       │
│    - Header         │
│    - Payload        │
│    - Signature      │
└────┬────────────────┘
     │
     │ 7. Return JWT tokens
     │
     ▼
┌──────────┐
│  CLIENT  │ Store tokens
└────┬─────┘
     │
     │ 8. GET /api/v1/produits
     │    Header: Authorization: Bearer <JWT>
     │
     ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
│ Line 28-61              │
├─────────────────────────┤
│ 9. Extract JWT          │
│                         │
│ 10. Extract username    │
│     from JWT            │
└────┬────────────────────┘
     │
     │ 11. jwtService.extractUsername(jwt)
     │
     ▼
┌─────────────────────┐
│  JwtService         │
│  Line 53-72         │
├─────────────────────┤
│ 12. Parse JWT       │
│                     │
│ 13. Verify signature│
│                     │
│ 14. Extract claims  │
└────┬────────────────┘
     │
     │ 15. Return "ahmed"
     │
     ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
├─────────────────────────┤
│ 16. Load user from DB   │
└────┬────────────────────┘
     │
     │ 17. userDetailsService.loadUserByUsername("ahmed")
     │
     ▼
┌─────────────────────┐
│  UserApp (DB)       │
│  Line 62-79         │
├─────────────────────┤
│ 18. Get authorities │
│     (roles +        │
│      permissions)   │
└────┬────────────────┘
     │
     │ 19. Return UserDetails with authorities
     │
     ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
├─────────────────────────┤
│ 20. Validate token      │
└────┬────────────────────┘
     │
     │ 21. jwtService.isTokenValid(jwt, userDetails)
     │
     ▼
┌─────────────────────┐
│  JwtService         │
│  Line 74-81         │
├─────────────────────┤
│ 22. Check username  │
│     matches         │
│                     │
│ 23. Check not       │
│     expired         │
└────┬────────────────┘
     │
     │ 24. Return true (valid)
     │
     ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
├─────────────────────────┤
│ 25. Create auth token   │
│                         │
│ 26. Set in              │
│     SecurityContext     │
│                         │
│ 27. Continue filter     │
│     chain               │
└────┬────────────────────┘
     │
     ▼
┌─────────────────────┐
│  SecurityConfig     │
│  Line 30-43         │
├─────────────────────┤
│ 28. Check endpoint  │
│     authorization   │
└────┬────────────────┘
     │
     ▼
┌─────────────────────┐
│ ProduitController   │
├─────────────────────┤
│ 29. @PreAuthorize   │
│     check           │
│                     │
│ 30. Execute method  │
└────┬────────────────┘
     │
     │ 31. Return products
     │
     ▼
┌──────────┐
│  CLIENT  │
└──────────┘
```

---

## 5. Code Mapping: Theory vs Your Implementation

### Token Generation

| Theory Step | Your Code Location | Method |
|-------------|-------------------|--------|
| Create Header | `JwtService.java:43-51` | `Jwts.builder()` |
| Create Payload | `JwtService.java:45-47` | `.setSubject()`, `.setIssuedAt()`, `.setExpiration()` |
| Sign Token | `JwtService.java:48` | `.signWith(getSigningKey(), HS256)` |
| Get Secret Key | `JwtService.java:27-30` | `getSigningKey()` |
| Encode Token | `JwtService.java:49` | `.compact()` |

### Token Validation

| Theory Step | Your Code Location | Method |
|-------------|-------------------|--------|
| Extract Token | `JwtAuthenticationFilter.java:38` | `authHeader.substring(7)` |
| Parse Token | `JwtService.java:66-72` | `Jwts.parser().parseClaimsJws()` |
| Verify Signature | `JwtService.java:67` | `.setSigningKey(getSigningKey())` |
| Extract Claims | `JwtService.java:61-64` | `extractClaim()` |
| Check Expiration | `JwtService.java:79-81` | `isTokenExpired()` |
| Validate Username | `JwtService.java:74-77` | `isTokenValid()` |

### Authentication Flow

| Theory Step | Your Code Location | Method |
|-------------|-------------------|--------|
| Receive Login | `AuthController.java:36` | `login()` |
| Validate Credentials | `AuthService.java:61-63` | `authenticationManager.authenticate()` |
| Generate JWT | `AuthService.java:69-70` | `jwtService.generateToken()` |
| Save Refresh Token | `AuthService.java:72` | `saveRefreshToken()` |
| Return Tokens | `AuthService.java:74-81` | `AuthResponse.builder()` |

### Request Interception

| Theory Step | Your Code Location | Method |
|-------------|-------------------|--------|
| Intercept Request | `JwtAuthenticationFilter.java:28` | `doFilterInternal()` |
| Extract Header | `JwtAuthenticationFilter.java:34` | `request.getHeader("Authorization")` |
| Validate Token | `JwtAuthenticationFilter.java:46` | `jwtService.isTokenValid()` |
| Load User | `JwtAuthenticationFilter.java:44` | `userDetailsService.loadUserByUsername()` |
| Set Authentication | `JwtAuthenticationFilter.java:54` | `SecurityContextHolder.setAuthentication()` |
| Continue Chain | `JwtAuthenticationFilter.java:60` | `filterChain.doFilter()` |

---

## Summary

Your project implements JWT authentication with:

1. **JwtService**: Generates and validates JWT tokens
2. **JwtAuthenticationFilter**: Intercepts requests and validates tokens
3. **AuthService**: Handles login/register/refresh logic
4. **SecurityConfig**: Configures Spring Security with JWT
5. **UserApp**: Provides user details and authorities

Every request goes through the filter, gets validated, and user is authenticated before reaching the controller.
