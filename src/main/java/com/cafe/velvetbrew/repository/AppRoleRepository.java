package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByRoleCodeIgnoreCase(String roleCode);
}
