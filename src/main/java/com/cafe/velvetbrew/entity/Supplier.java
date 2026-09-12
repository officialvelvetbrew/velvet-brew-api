package com.cafe.velvetbrew.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers", uniqueConstraints = { @UniqueConstraint(name = "uk_suppliers_name", columnNames = "name") })
@Getter
@Setter
public class Supplier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "contact_person", length = 100)
	private String contactPerson;

	@Column(length = 30)
	private String phone;

	@Column(length = 150)
	private String email;

	@Column(length = 255)
	private String address;

	@Column(name = "tax_number", length = 50)
	private String taxNumber;

	@Column(nullable = false)
	private Boolean enabled = true;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}