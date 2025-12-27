package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApp implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = true)  // Changed: Now nullable for Keycloak users
    private String password;
    
    private String firstName;
    private String lastName;
    
    @Column(nullable = false)
    private boolean enabled = false;
    
    /**
     * Keycloak User ID - Links this user to their Keycloak account
     * When null, user authenticates locally (legacy mode)
     * When set, user authenticates via Keycloak (recommended)
     */
    @Column(name = "keycloak_user_id", unique = true)
    private String keycloakUserId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleApp> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserPermission> customPermissions = new HashSet<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this user is linked to Keycloak
     * @return true if user authenticates via Keycloak, false if local auth
     */
    public boolean isKeycloakUser() {
        return keycloakUserId != null && !keycloakUserId.isEmpty();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (RoleApp role : roles) {
            for (Permission permission : role.getDefaultPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        
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
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
