package com.tricol.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserApp user;
    
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
    
    @Column(nullable = false)
    private boolean granted;
    
    @ManyToOne
    @JoinColumn(name = "granted_by")
    private UserApp grantedBy;
    
    private LocalDateTime grantedAt;
    
    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}
