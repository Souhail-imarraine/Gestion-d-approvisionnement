package com.tricol.stock.service;

import com.tricol.stock.entity.AuditLog;
import com.tricol.stock.entity.UserApp;
import com.tricol.stock.repository.AuditLogRepository;
import com.tricol.stock.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void log(String action, String resource, String resourceId) {
        log(action, resource, resourceId, null, null);
    }
    
    @Transactional
    public void log(String action, String resource, String resourceId, String oldValue, String newValue) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        
        String username = authentication.getName();
        UserApp user = userRepository.findByUsername(username).orElse(null);
        
        String ipAddress = getClientIpAddress();
        
        AuditLog auditLog = AuditLog.builder()
            .user(user)
            .action(action)
            .resource(resource)
            .resourceId(resourceId)
            .oldValue(oldValue)
            .newValue(newValue)
            .ipAddress(ipAddress)
            .build();
        
        auditLogRepository.save(auditLog);
    }
    
    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
}
