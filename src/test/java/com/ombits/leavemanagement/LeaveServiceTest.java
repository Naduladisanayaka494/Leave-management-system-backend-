package com.ombits.leavemanagement;

import com.ombits.leavemanagement.dto.CreateLeaveRequestDto;
import com.ombits.leavemanagement.dto.LeaveResponseDto;
import com.ombits.leavemanagement.dto.RejectLeaveRequestDto;
import com.ombits.leavemanagement.entity.Employee;
import com.ombits.leavemanagement.entity.LeaveRequest;
import com.ombits.leavemanagement.entity.LeaveStatus;
import com.ombits.leavemanagement.entity.LeaveType;
import com.ombits.leavemanagement.exception.BadRequestException;
import com.ombits.leavemanagement.exception.InvalidLeaveDateException;
import com.ombits.leavemanagement.exception.InvalidStatusTransitionException;
import com.ombits.leavemanagement.repository.EmployeeRepository;
import com.ombits.leavemanagement.repository.LeaveRequestRepository;
import com.ombits.leavemanagement.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("Kavindi Senaratne", "kavindi@ombits.com", "Engineering");
        employee.setId(1L);
    }

    @Test
    void testCreateLeave_Success() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);
        CreateLeaveRequestDto dto = new CreateLeaveRequestDto(1L, LeaveType.ANNUAL, start, end, "Summer family trip");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findOverlappingLeaves(1L, start, end)).thenReturn(Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        LeaveResponseDto result = leaveService.createLeave(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(4L, result.getTotalDays());
        assertEquals(LeaveType.ANNUAL, result.getLeaveType());
    }

    @Test
    void testCreateLeave_EndDateBeforeStartDate_ThrowsInvalidLeaveDateException() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2); // End before start!
        CreateLeaveRequestDto dto = new CreateLeaveRequestDto(1L, LeaveType.CASUAL, start, end, "Invalid date range");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        InvalidLeaveDateException exception = assertThrows(InvalidLeaveDateException.class, () -> {
            leaveService.createLeave(dto);
        });

        assertTrue(exception.getMessage().contains("cannot be before start date"));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void testCreateLeave_OverlappingDates_ThrowsInvalidLeaveDateException() {
        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = LocalDate.now().plusDays(6);
        CreateLeaveRequestDto dto = new CreateLeaveRequestDto(1L, LeaveType.MEDICAL, start, end, "Doctor appointment");

        LeaveRequest existing = new LeaveRequest(employee, LeaveType.ANNUAL, start, end, "Existing");
        existing.setId(5L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findOverlappingLeaves(1L, start, end)).thenReturn(List.of(existing));

        InvalidLeaveDateException exception = assertThrows(InvalidLeaveDateException.class, () -> {
            leaveService.createLeave(dto);
        });

        assertTrue(exception.getMessage().contains("overlapping leave request"));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void testApproveLeave_Success() {
        LeaveRequest pending = new LeaveRequest(employee, LeaveType.ANNUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Personal");
        pending.setId(20L);
        pending.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(20L)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        LeaveResponseDto result = leaveService.approveLeave(20L);

        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertNotNull(result.getReviewedAt());
    }

    @Test
    void testApproveLeave_AlreadyApproved_ThrowsInvalidStatusTransitionException() {
        LeaveRequest approved = new LeaveRequest(employee, LeaveType.ANNUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Personal");
        approved.setId(20L);
        approved.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(20L)).thenReturn(Optional.of(approved));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            leaveService.approveLeave(20L);
        });
    }

    @Test
    void testRejectLeave_Success() {
        LeaveRequest pending = new LeaveRequest(employee, LeaveType.CASUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Personal");
        pending.setId(30L);
        pending.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(30L)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        RejectLeaveRequestDto rejectDto = new RejectLeaveRequestDto("Critical release scheduled on these dates");
        LeaveResponseDto result = leaveService.rejectLeave(30L, rejectDto);

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
        assertEquals("Critical release scheduled on these dates", result.getRejectionReason());
        assertNotNull(result.getReviewedAt());
    }

    @Test
    void testRejectLeave_MissingReason_ThrowsBadRequestException() {
        LeaveRequest pending = new LeaveRequest(employee, LeaveType.CASUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Personal");
        pending.setId(30L);
        pending.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(30L)).thenReturn(Optional.of(pending));

        RejectLeaveRequestDto emptyReasonDto = new RejectLeaveRequestDto("   ");

        assertThrows(BadRequestException.class, () -> {
            leaveService.rejectLeave(30L, emptyReasonDto);
        });
    }
}
