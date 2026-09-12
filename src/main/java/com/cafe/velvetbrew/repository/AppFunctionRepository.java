package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.AppFunction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppFunctionRepository extends JpaRepository<AppFunction, Long> {

    Optional<AppFunction> findByFunctionCodeIgnoreCase(String functionCode);
}
