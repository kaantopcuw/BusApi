package com.busapi.modules.report.dto;

import com.busapi.modules.report.enums.ExpenseType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private ExpenseType expenseType;
}

