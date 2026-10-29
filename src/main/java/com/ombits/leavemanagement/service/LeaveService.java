package com.ombits.leavemanagement.service;

import com.ombits.leavemanagement.dto.CreateLeaveRequestDto;
import com.ombits.leavemanagement.dto.LeaveResponseDto;
import com.ombits.leavemanagement.dto.LeaveStatsDto;
import com.ombits.leavemanagement.dto.RejectLeaveRequestDto;
import com.ombits.leavemanagement.entity.Employee;
import com.ombits.leavemanagement.entity.LeaveRequest;
import com.ombits.leavemanagement.entity.LeaveStatus;
import com.ombits.leavemanagement.exception.BadRequestException;
import com.ombits.leavemanagement.exception.InvalidLeaveDateException;
import com.ombits.leavemanagement.exception.InvalidStatusTransitionException;
import com.ombits.leavemanagement.exception.ResourceNotFoundException;
import com.ombits.leavemanagement.repository.EmployeeRepository;
import com.ombits.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public LeaveResponseDto createLeave(CreateLeaveRequestDto request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        // Business Rule: End date cannot be before start date
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidLeaveDateException("End date (" + request.getEndDate() + 
                    ") cannot be before start date (" + request.getStartDate() + ")");
        }

        // Business Rule: Prevent overlapping leaves for the same employee
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate()
        );
        if (!overlappingLeaves.isEmpty()) {
            LeaveRequest existing = overlappingLeaves.get(0);
            throw new InvalidLeaveDateException("Employee already has an overlapping leave request (" + 
                    existing.getStatus() + ") between " + existing.getStartDate() + " and " + existing.getEndDate());
        }

        LeaveRequest leave = new LeaveRequest(
                employee,
                request.getLeaveType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getReason().trim()
        );

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return LeaveResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getAllLeaves(Long employeeId, LeaveStatus status) {
        List<LeaveRequest> leaves;

        if (employeeId != null && status != null) {
            leaves = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, status);
        } else if (employeeId != null) {
            leaves = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        } else if (status != null) {
            leaves = leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            leaves = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        }

        return leaves.stream()
                .map(LeaveResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveResponseDto getLeaveById(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        return LeaveResponseDto.fromEntity(leave);
    }

    @Transactional
    public LeaveResponseDto approveLeave(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        // Business Rule: Only PENDING leaves can be approved
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot approve leave request with id " + id + 
                    ". Current status is " + leave.getStatus() + ". Only PENDING requests can be approved.");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return LeaveResponseDto.fromEntity(saved);
    }

    @Transactional
    public LeaveResponseDto rejectLeave(Long id, RejectLeaveRequestDto request) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        // Business Rule: Only PENDING leaves can be rejected
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot reject leave request with id " + id + 
                    ". Current status is " + leave.getStatus() + ". Only PENDING requests can be rejected.");
        }

        // Business Rule: Rejection reason is strictly mandatory
        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is mandatory when rejecting a leave request");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(request.getRejectionReason().trim());
        leave.setReviewedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return LeaveResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public LeaveStatsDto getLeaveStats() {
        long totalEmployees = employeeRepository.count();
        long totalRequests = leaveRequestRepository.count();
        long pending = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByStatus(LeaveStatus.APPROVED);
        long rejected = leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);

        return new LeaveStatsDto(totalEmployees, totalRequests, pending, approved, rejected);
    }
}
