package com.ems.dto;

import com.ems.common.TimeUtils;
import com.ems.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

/**
 * Employé renvoyé au frontend.
 * Les clés MinIO internes ne sont pas exposées : le frontend reçoit
 * les URLs de l'API qui servent la photo et le CV (protégées par le JWT).
 */
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
        Instant createdAt,
        Instant updatedAt
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
                TimeUtils.toInstant(e.getCreatedAt()),
                TimeUtils.toInstant(e.getUpdatedAt()));
    }
}
