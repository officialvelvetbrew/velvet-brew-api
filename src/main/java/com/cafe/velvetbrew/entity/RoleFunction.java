package com.cafe.velvetbrew.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A role's grant on a function, with its own create/read/update/delete
 * flags - this is what makes it possible for two roles to hold the same
 * function with different capabilities (e.g. STAFF can create/read/update
 * inventory items but not delete them, while ADMIN can do all four).
 */
@Entity
@Table(name = "role_functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private AppRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "function_id", nullable = false)
    private AppFunction function;

    @Builder.Default
    @Column(name = "can_create", nullable = false)
    private Boolean canCreate = false;

    @Builder.Default
    @Column(name = "can_read", nullable = false)
    private Boolean canRead = false;

    @Builder.Default
    @Column(name = "can_update", nullable = false)
    private Boolean canUpdate = false;

    @Builder.Default
    @Column(name = "can_delete", nullable = false)
    private Boolean canDelete = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
