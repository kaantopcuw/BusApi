package com.busapi.modules.report.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsResponse {
    // Dönem bilgisi (Örn: Bu Ay)
    private String period;

    // Temel Metrikler
    private BigDecimal totalRevenue;    // Ciro
    private BigDecimal totalExpense;    // Gider
    private BigDecimal netProfit;       // Net Kar (Ciro - Gider)
    private long totalTicketsSold;      // Satılan Bilet

    // Grafik için günlük/aylık kırılım eklenebilir ama şimdilik basit tutalım.
}
