-- Vérifier si l'utilisateur admin existe
SELECT * FROM users WHERE username = 'admin';

-- Vérifier tous les utilisateurs
SELECT id, username, email, enabled FROM users;

-- Vérifier les rôles
SELECT * FROM roles;

-- Vérifier les permissions
SELECT COUNT(*) as total_permissions FROM permissions;

-- Vérifier les rôles de l'admin
SELECT u.username, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- Si l'admin n'existe pas, le créer manuellement
-- Mot de passe: "password" (BCrypt hash)
INSERT INTO users (username, email, password, first_name, last_name, enabled)
VALUES ('admin', 'admin@tricol.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Tricol', true);

-- Assigner le rôle ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
