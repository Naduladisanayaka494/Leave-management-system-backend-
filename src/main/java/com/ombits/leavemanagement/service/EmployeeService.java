package com.ombits.leavemanagement.service;

import com.ombits.leavemanagement.dto.CreateEmployeeRequest;
import com.ombits.leavemanagement.dto.EmployeeDto;
import com.ombits.leavemanagement.entity.Employee;
import com.ombits.leavemanagement.entity.User;
import com.ombits.leavemanagement.exception.BadRequestException;
import com.ombits.leavemanagement.exception.ResourceNotFoundException;
import com.ombits.leavemanagement.repository.EmployeeRepository;
import com.ombits.leavemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An employee with email already exists: " + request.getEmail());
        }

        Employee employee = new Employee(
                request.getName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getDepartment().trim()
        );

        // Link existing user if one already exists for this email
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
        if (user != null) {
            employee.setUser(user);
        }

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));
        return mapToDto(employee);
    }

    private EmployeeDto mapToDto(Employee employee) {
        return new EmployeeDto(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getCreatedAt()
        );
    }
}
