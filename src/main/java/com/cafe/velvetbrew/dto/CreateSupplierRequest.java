package com.cafe.velvetbrew.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSupplierRequest {

	@NotBlank(message = "Supplier name is required")
	@Size(max = 150, message = "Supplier name must not exceed 150 characters")
	private String name;

	@Size(max = 100, message = "Contact person must not exceed 100 characters")
	private String contactPerson;

	@Size(max = 30, message = "Phone must not exceed 30 characters")
	private String phone;

	@Email(message = "Invalid email address")
	@Size(max = 150)
	private String email;

	@Size(max = 255)
	private String address;

	@Size(max = 50)
	private String taxNumber;
}