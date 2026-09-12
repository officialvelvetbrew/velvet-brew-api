package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.FunctionUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FunctionUrlRepository extends JpaRepository<FunctionUrl, Long> {

    List<FunctionUrl> findByActiveTrue();
}
