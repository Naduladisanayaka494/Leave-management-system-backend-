package com.ombits.leavemanagement.service;

import com.ombits.leavemanagement.dto.*;
import com.ombits.leavemanagement.entity.Employee;
import com.ombits.leavemanagement.entity.Role;
import com.ombits.leavemanagement.entity.User;
import com.ombits.leavemanagement.exception.BadRequestException;
import com.ombits.leavemanagement.exception.ResourceNotFoundException;
import com.ombits.leavemanagement.repository.EmployeeRepository;
import com.ombits.leavemanagement.repository.UserRepository;
import com.ombits.leavemanagement.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        Employee employee = employeeRepository.findByEmail(user.getEmail()).orElse(null);

        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole());
        EmployeeDto empDto = employee != null ?
                new EmployeeDto(employee.getId(), employee.getName(), employee.getEmail(), employee.getDepartment(), employee.getCreatedAt())
                : null;

        return new AuthResponse(jwt, userDto, empDto);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_EMPLOYEE;
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        user = userRepository.save(user);

        Employee employee = employeeRepository.findByEmail(request.getEmail()).orElse(null);
        if (employee == null) {
            employee = new Employee(request.getName(), request.getEmail(), request.getDepartment(), user);
            employee = employeeRepository.save(employee);
        } else {
            employee.setUser(user);
            employee = employeeRepository.save(employee);
        }

        String jwt = tokenProvider.generateTokenFromEmail(user.getEmail(), user.getId());

        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole());
        EmployeeDto empDto = new EmployeeDto(employee.getId(), employee.getName(), employee.getEmail(), employee.getDepartment(), employee.getCreatedAt());

        return new AuthResponse(jwt, userDto, empDto);
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Employee employee = employeeRepository.findByEmail(user.getEmail()).orElse(null);

        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole());
        EmployeeDto empDto = employee != null ?
                new EmployeeDto(employee.getId(), employee.getName(), employee.getEmail(), employee.getDepartment(), employee.getCreatedAt())
                : null;

        return new AuthResponse(null, userDto, empDto);
    }
}
