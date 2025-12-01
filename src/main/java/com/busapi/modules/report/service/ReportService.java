package com.busapi.modules.report.service;

import com.busapi.modules.report.dto.CreateExpenseRequest;
import com.busapi.modules.report.dto.DashboardStatsResponse;
import com.busapi.modules.report.entity.Expense;
import com.busapi.modules.report.repository.ExpenseRepository;
import com.busapi.modules.sales.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public UUID addExpense(CreateExpenseRequest request) {
        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setExpenseType(request.getExpenseType());

        return expenseRepository.save(expense).getId();
    }

    // Aylık Rapor Getir (Dashboard için)
    public DashboardStatsResponse getMonthlyStats() {
        // Bu ayın başı ve sonu
        LocalDate now = LocalDate.now();
        LocalDate startOfDate = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfDate = now.with(TemporalAdjusters.lastDayOfMonth());

        // TicketRepository LocalDateTime kullanıyor (createdAt), Expense ise LocalDate.
        // Dönüşüm yapmamız lazım.
        LocalDateTime startDateTime = startOfDate.atStartOfDay();
        LocalDateTime endDateTime = endOfDate.atTime(LocalTime.MAX);

        // 1. Toplam Ciro (TicketRepository'den)
        BigDecimal revenue = ticketRepository.calculateTotalRevenue(startDateTime, endDateTime);

        // 2. Toplam Gider (ExpenseRepository'den)
        BigDecimal expense = expenseRepository.calculateTotalExpense(startOfDate, endOfDate);

        // 3. Satış Adedi
        long ticketCount = ticketRepository.countSoldTickets(startDateTime, endDateTime);

        // 4. Net Kar
        BigDecimal netProfit = revenue.subtract(expense);

        return DashboardStatsResponse.builder()
                .period(now.getMonth().name() + " " + now.getYear())
                .totalRevenue(revenue)
                .totalExpense(expense)
                .netProfit(netProfit)
                .totalTicketsSold(ticketCount)
                .build();
    }
}