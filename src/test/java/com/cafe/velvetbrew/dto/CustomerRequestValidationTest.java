package com.cafe.velvetbrew.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRequestValidationTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void passesWhenNeitherMobileNorEmailProvided() {
        CustomerRequest request = request("Sam Patel", null, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void passesWhenOnlyEmailProvided() {
        CustomerRequest request = request("Jane Smith", null, "jane.smith@example.com");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void passesWhenOnlyMobileProvided() {
        CustomerRequest request = request("Alex Kim", "9123456789", null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void passesWhenBothMobileAndEmailProvided() {
        CustomerRequest request = request("John Doe", "9876543210", "john.doe@example.com");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void passesWhenBothAreBlankStrings() {
        CustomerRequest request = request("Sam Patel", "", "");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void failsWhenEmailFormatIsInvalid() {
        CustomerRequest request = request("Jane Smith", null, "not-an-email");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void failsWhenMobileFormatIsInvalid() {
        CustomerRequest request = request("Alex Kim", "abc123", null);

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("mobile");
    }

    @Test
    void failsWhenFullNameIsBlankRegardlessOfContactDetails() {
        CustomerRequest request = request("", null, null);

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("fullName");
    }

    private static CustomerRequest request(String fullName, String mobile, String email) {
        CustomerRequest request = new CustomerRequest();
        request.setFullName(fullName);
        request.setMobile(mobile);
        request.setEmail(email);
        return request;
    }
}
