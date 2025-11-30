package com.busapi.modules.report.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.report.enums.ExpenseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "expenses")
@EqualsAndHashCode(callSuper = true)
public class Expense extends BaseEntity {

    @Column(nullable = false)
    private String title; // Örn: 34 ABC 123 Yakıt Fişi

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseType expenseType;
}