package com.tricol.stock.repository;

import com.tricol.stock.entity.RoleApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleApp, Long> {
    
    Optional<RoleApp> findByName(String name);
}
