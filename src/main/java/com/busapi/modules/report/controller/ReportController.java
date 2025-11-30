package com.busapi.modules.report.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.report.dto.CreateExpenseRequest;
import com.busapi.modules.report.dto.DashboardStatsResponse;
import com.busapi.modules.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Sadece Admin görebilir
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<DashboardStatsResponse> getDashboardStats() {
        return ApiResponse.success(reportService.getMonthlyStats());
    }

    // Gider Ekleme
    @PostMapping("/expenses")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Long> addExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return ApiResponse.success(reportService.addExpense(request), "Gider kaydedildi.");
    }
}