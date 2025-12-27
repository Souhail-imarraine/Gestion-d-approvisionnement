# ⚠️ ACTION REQUIRED: Update Client Secret

## What You Need To Do:

After importing the realm in Keycloak, you got a **Client Secret**.

### Update application.properties:

1. Open: `src/main/resources/application.properties`

2. Find this line (around line 38):
```properties
keycloak.credentials.secret=CHANGE_THIS_TO_YOUR_CLIENT_SECRET
```

3. Replace `CHANGE_THIS_TO_YOUR_CLIENT_SECRET` with your actual secret from Keycloak

Example:
```properties
keycloak.credentials.secret=9xK7mP2nQ5rT8vW3yZ6aB4cD1eF0gH7j
```

4. **Save the file**

---

## How to Get the Secret from Keycloak:

1. Make sure **"tricol-stock"** is selected in top-left dropdown
2. Click **Clients** (left menu)
3. Click **tricol-stock-app**
4. Click **Credentials** tab
5. Copy the **Client Secret** value

---

## After Updating the Secret:

Continue to **STEP 4** in the main setup guide.

