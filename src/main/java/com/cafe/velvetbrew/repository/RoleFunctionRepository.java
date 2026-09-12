package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.RoleFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RoleFunctionRepository extends JpaRepository<RoleFunction, Long> {

    @Query("""
            SELECT rf FROM RoleFunction rf
            WHERE rf.role.id IN :roleIds AND rf.function.id IN :functionIds
            """)
    List<RoleFunction> findByRoleIdInAndFunctionIdIn(
            @Param("roleIds") Collection<Long> roleIds,
            @Param("functionIds") Collection<Long> functionIds);

    @Query("""
            SELECT rf FROM RoleFunction rf
            WHERE rf.role.id IN :roleIds
            """)
    List<RoleFunction> findByRoleIdIn(@Param("roleIds") Collection<Long> roleIds);
}
