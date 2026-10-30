package com.ems.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Pattern(regexp = "^$|^[+0-9 ()\\-]{6,30}$", message = "Phone number format is invalid")
        String phone,

        @NotBlank(message = "Job title is required")
        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        @NotNull(message = "Hire date is required")
        LocalDate hireDate,

        @PositiveOrZero(message = "Salary must be positive")
        @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
        BigDecimal salary
) {}
