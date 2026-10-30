package com.ems.dto;

import com.ems.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String jobTitle,
        String department,
        LocalDate hireDate,
        BigDecimal salary,
        boolean hasPhoto,
        String photoUrl,
        boolean hasCv,
        String cvFileName,
        String cvUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EmployeeResponse from(Employee e) {
        boolean hasPhoto = e.getPhotoKey() != null;
        boolean hasCv = e.getCvKey() != null;
        String base = "/api/employees/" + e.getId();
        return new EmployeeResponse(
                e.getId(),
                e.getFirstName(),
                e.getLastName(),
                e.getFullName(),
                e.getEmail(),
                e.getPhone(),
                e.getJobTitle(),
                e.getDepartment(),
                e.getHireDate(),
                e.getSalary(),
                hasPhoto,
                hasPhoto ? base + "/photo" : null,
                hasCv,
                e.getCvFileName(),
                hasCv ? base + "/cv" : null,
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
