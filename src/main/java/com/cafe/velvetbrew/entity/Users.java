package com.cafe.velvetbrew.entity;


import com.cafe.velvetbrew.common.enums.AuthProvider;
import com.cafe.velvetbrew.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firebaseUid;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean enabled;

    private Boolean emailVerified;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}