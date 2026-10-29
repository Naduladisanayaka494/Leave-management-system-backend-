package com.ombits.leavemanagement.controller;

import com.ombits.leavemanagement.dto.CreateLeaveRequestDto;
import com.ombits.leavemanagement.dto.LeaveResponseDto;
import com.ombits.leavemanagement.dto.LeaveStatsDto;
import com.ombits.leavemanagement.dto.RejectLeaveRequestDto;
import com.ombits.leavemanagement.entity.LeaveStatus;
import com.ombits.leavemanagement.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Requests", description = "Endpoints for leave requests and administration")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @Operation(summary = "Submit a leave request")
    public ResponseEntity<LeaveResponseDto> createLeave(@Valid @RequestBody CreateLeaveRequestDto request) {
        LeaveResponseDto created = leaveService.createLeave(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all leave requests (supports optional filters by employeeId and status)")
    public ResponseEntity<List<LeaveResponseDto>> getAllLeaves(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status) {
        List<LeaveResponseDto> leaves = leaveService.getAllLeaves(employeeId, status);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID")
    public ResponseEntity<LeaveResponseDto> getLeaveById(@PathVariable Long id) {
        LeaveResponseDto leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(leave);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Approve pending leave request (Administrator only)")
    public ResponseEntity<LeaveResponseDto> approveLeave(@PathVariable Long id) {
        LeaveResponseDto approved = leaveService.approveLeave(id);
        return ResponseEntity.ok(approved);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Reject pending leave request with reason (Administrator only)")
    public ResponseEntity<LeaveResponseDto> rejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody RejectLeaveRequestDto request) {
        LeaveResponseDto rejected = leaveService.rejectLeave(id, request);
        return ResponseEntity.ok(rejected);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get leave summary statistics for dashboard")
    public ResponseEntity<LeaveStatsDto> getLeaveStats() {
        LeaveStatsDto stats = leaveService.getLeaveStats();
        return ResponseEntity.ok(stats);
    }
}
