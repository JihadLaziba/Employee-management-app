package com.ems.service;

import com.ems.common.PageableFactory;
import com.ems.common.exception.BusinessException;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.dto.EmployeeRequest;
import com.ems.dto.EmployeeResponse;
import com.ems.dto.PageResponse;
import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "firstName", "lastName", "email", "jobTitle", "department", "hireDate", "createdAt");

    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(String search, int page, int size, String sortBy, String direction) {
        var pageable = PageableFactory.of(page, size, sortBy, direction, SORTABLE_FIELDS, "createdAt");
        return PageResponse.from(
                employeeRepository.search(PageableFactory.normalizeSearch(search), pageable)
                        .map(EmployeeResponse::from));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return EmployeeResponse.from(findOrThrow(id));
    }

    // ------------------------------------------------------------------ écriture

    public EmployeeResponse create(EmployeeRequest req) {
        String email = normalizeEmail(req.email());
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            throw BusinessException.conflict("An employee with this email already exists");
        }

        Employee employee = new Employee();
        apply(employee, req, email);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeRequest req) {
        Employee employee = findOrThrow(id);
        String email = normalizeEmail(req.email());

        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw BusinessException.conflict("An employee with this email already exists");
        }

        apply(employee, req, email);
        return EmployeeResponse.from(employee);
    }

    public void delete(Long id) {
        Employee employee = findOrThrow(id);
        String photoKey = employee.getPhotoKey();
        String cvKey = employee.getCvKey();

        employeeRepository.delete(employee);
        employeeRepository.flush();

        // Les fichiers ne doivent pas rester orphelins dans MinIO
        storageService.deleteQuietly(photoKey);
        storageService.deleteQuietly(cvKey);
    }

    // ------------------------------------------------------------------ utilitaires

    /** Utilisé aussi par les services fichiers et contrat (étapes 6 et 7). */
    @Transactional(readOnly = true)
    public Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    private void apply(Employee e, EmployeeRequest req, String email) {
        e.setFirstName(req.firstName().trim());
        e.setLastName(req.lastName().trim());
        e.setEmail(email);
        e.setPhone(blankToNull(req.phone()));
        e.setJobTitle(req.jobTitle().trim());
        e.setDepartment(blankToNull(req.department()));
        e.setHireDate(req.hireDate());
        e.setSalary(req.salary());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
