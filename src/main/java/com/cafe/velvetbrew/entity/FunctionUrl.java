package com.cafe.velvetbrew.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "function_urls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "function_id", nullable = false)
    private AppFunction function;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

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

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
