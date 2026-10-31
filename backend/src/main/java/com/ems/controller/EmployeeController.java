package com.ems.controller;

import com.ems.service.AuditService;
import com.ems.enums.AuditAction;
import com.ems.dto.EmployeeRequest;
import com.ems.dto.EmployeeResponse;
import com.ems.dto.PageResponse;
import com.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD des employés.
 * Chaque action exige la permission correspondante (vérifiée côté backend).
 * Un Super Admin possède automatiquement toutes les permissions.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuditService auditService;

    /** GET /api/employees?search=&page=0&size=10&sortBy=createdAt&direction=desc */
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_READ')")
    public PageResponse<EmployeeResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return employeeService.search(search, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_READ')")
    public EmployeeResponse get(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_CREATE')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.create(request);
        auditService.record(AuditAction.EMPLOYEE_CREATED, AuditService.EMPLOYEE, created.id(), created.fullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_UPDATE')")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse updated = employeeService.update(id, request);
        auditService.record(AuditAction.EMPLOYEE_UPDATED, AuditService.EMPLOYEE, updated.id(), updated.fullName());
        return updated;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String name = employeeService.getById(id).fullName();
        employeeService.delete(id);
        auditService.record(AuditAction.EMPLOYEE_DELETED, AuditService.EMPLOYEE, id, name);
        return ResponseEntity.noContent().build();
    }
}
