package com.ombits.leavemanagement.config;

import com.ombits.leavemanagement.entity.*;
import com.ombits.leavemanagement.repository.EmployeeRepository;
import com.ombits.leavemanagement.repository.LeaveRequestRepository;
import com.ombits.leavemanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      EmployeeRepository employeeRepository,
                      LeaveRequestRepository leaveRequestRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // 1. Seed Administrator
            User adminUser = new User("admin@ombits.com", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN);
            adminUser = userRepository.save(adminUser);

            Employee adminEmployee = new Employee("Nadula Dissanayake", "admin@ombits.com", "Executive", adminUser);
            employeeRepository.save(adminEmployee);

            // 2. Seed Employee 1: Kasun Perera
            User emp1User = new User("kasun@ombits.com", passwordEncoder.encode("employee123"), Role.ROLE_EMPLOYEE);
            emp1User = userRepository.save(emp1User);

            Employee emp1 = new Employee("Kasun Perera", "kasun@ombits.com", "Engineering", emp1User);
            emp1 = employeeRepository.save(emp1);

            // 3. Seed Employee 2: Dilani Jayawardena
            User emp2User = new User("dilani@ombits.com", passwordEncoder.encode("employee123"), Role.ROLE_EMPLOYEE);
            emp2User = userRepository.save(emp2User);

            Employee emp2 = new Employee("Dilani Jayawardena", "dilani@ombits.com", "Human Resources", emp2User);
            emp2 = employeeRepository.save(emp2);

            // 4. Seed Employee 3: Nuwan Fernando
            User emp3User = new User("nuwan@ombits.com", passwordEncoder.encode("employee123"), Role.ROLE_EMPLOYEE);
            emp3User = userRepository.save(emp3User);

            Employee emp3 = new Employee("Nuwan Fernando", "nuwan@ombits.com", "Product Design", emp3User);
            emp3 = employeeRepository.save(emp3);

            // 5. Seed Employee 4: Chamari Bandara
            User emp4User = new User("chamari@ombits.com", passwordEncoder.encode("employee123"), Role.ROLE_EMPLOYEE);
            emp4User = userRepository.save(emp4User);

            Employee emp4 = new Employee("Chamari Bandara", "chamari@ombits.com", "Quality Assurance", emp4User);
            employeeRepository.save(emp4);

            // 6. Seed Initial Leave Requests
            LocalDate today = LocalDate.now();
            LeaveRequest pendingLeave = new LeaveRequest(
                    emp1,
                    LeaveType.CASUAL,
                    today.plusDays(3),
                    today.plusDays(5),
                    "Family event and personal commitments in Kandy."
            );
            leaveRequestRepository.save(pendingLeave);

            LeaveRequest approvedLeave = new LeaveRequest(
                    emp2,
                    LeaveType.MEDICAL,
                    today.minusDays(7),
                    today.minusDays(5),
                    "Doctor recommended recovery following routine dental surgery."
            );
            approvedLeave.setStatus(LeaveStatus.APPROVED);
            approvedLeave.setReviewedAt(LocalDateTime.now().minusDays(8));
            leaveRequestRepository.save(approvedLeave);

            LeaveRequest rejectedLeave = new LeaveRequest(
                    emp3,
                    LeaveType.ANNUAL,
                    today.plusDays(10),
                    today.plusDays(15),
                    "Annual vacation trip to Nuwara Eliya."
            );
            rejectedLeave.setStatus(LeaveStatus.REJECTED);
            rejectedLeave.setRejectionReason("Requested dates overlap with mandatory Q4 sprint delivery milestone.");
            rejectedLeave.setReviewedAt(LocalDateTime.now().minusDays(1));
            leaveRequestRepository.save(rejectedLeave);

            System.out.println(">>> Demo data successfully seeded: Admin (admin@ombits.com - Nadula Dissanayake) & Employees (Kasun Perera, Dilani Jayawardena, Nuwan Fernando, Chamari Bandara)");
        }
    }
}
