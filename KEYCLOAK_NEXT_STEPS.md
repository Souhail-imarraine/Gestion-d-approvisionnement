# 🎯 KEYCLOAK SETUP - DO THIS NOW

You're currently at: `http://127.0.0.1:8080/admin/master/console/`

## 📍 Step-by-Step Instructions (Follow Exactly)

### **ACTION 1: Switch from Master to tricol-stock Realm**

1. **Look at the TOP-LEFT corner** of the Keycloak admin page
2. You'll see a dropdown that currently says **"Master"** with a down arrow
3. **Click on it**
4. Select **"Add realm"** (or "Create Realm")

### **ACTION 2: Import the Pre-configured Realm**

A form will appear:

1. Look for **"Resource file"** or **"Import"** section
2. Click the **"Select file"** or **"Browse"** button
3. Navigate to this location on your computer:
   ```
   C:\Users\LENOVO\Desktop\New folder\Gestion_approvisionnement\docs\keycloak\test-realm-export.json
   ```
4. Select the file and click **"Open"**
5. Click **"Create"** button at the bottom

**✅ Wait a few seconds - Keycloak will import everything**

### **ACTION 3: Verify the Import**

After import completes:

1. The **top-left dropdown** should now show **"tricol-stock"** instead of "Master"
2. If not, click the dropdown and select **"tricol-stock"**

### **ACTION 4: Get the Client Secret**

Now that you're in the tricol-stock realm:

1. Look at the **left sidebar menu**
2. Click **"Clients"** (has an icon that looks like puzzle pieces)
3. You'll see a list - click on **"tricol-stock-app"**
4. At the top of the page, you'll see several tabs - click **"Credentials"**
5. You'll see **"Client Secret"** with a long string like: `a9f7d2e5-3b8c-4a1e-9d6f-2c8b7a4e1f3d`
6. **Click the copy icon** next to it (or select and copy the text)

### **ACTION 5: Paste the Secret into Your Project**

Open your project in your code editor:

1. Open file: `src\main\resources\application.properties`
2. Find line 38 (approximately):
   ```properties
   keycloak.credentials.secret=CHANGE_THIS_TO_YOUR_CLIENT_SECRET
   ```
3. Replace `CHANGE_THIS_TO_YOUR_CLIENT_SECRET` with the secret you copied
4. Example result:
   ```properties
   keycloak.credentials.secret=a9f7d2e5-3b8c-4a1e-9d6f-2c8b7a4e1f3d
   ```
5. **Save the file** (Ctrl+S)

---

## ✅ After Completing These Actions

You'll have:
- ✅ Realm: **tricol-stock** created
- ✅ Client: **tricol-stock-app** configured
- ✅ Roles: **ADMIN, MANAGER, USER, GUEST** ready
- ✅ Test users: **admin, manager, user** created
- ✅ Client secret: **Updated in application.properties**

---

## 🚀 Then You Can:

### **Disable Old SecurityConfig:**

```bash
cd "C:\Users\LENOVO\Desktop\New folder\Gestion_approvisionnement\src\main\java\com\tricol\stock\config"
ren SecurityConfig.java SecurityConfig.java.OLD
```

### **Start Your Application:**

```bash
cd "C:\Users\LENOVO\Desktop\New folder\Gestion_approvisionnement"
mvn clean compile
mvn spring-boot:run
```

---

## 🆘 If You Can't Find the test-realm-export.json File

The file should be at:
```
C:\Users\LENOVO\Desktop\New folder\Gestion_approvisionnement\docs\keycloak\test-realm-export.json
```

If it doesn't exist, **let me know** and I'll create it for you right now.

---

**Current Status:** ⏸️ Waiting for you to complete Actions 1-5

**Next:** After you paste the client secret, we'll start your Spring Boot app!

