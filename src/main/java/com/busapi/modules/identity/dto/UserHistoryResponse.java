package com.busapi.modules.identity.dto;

import com.busapi.modules.sales.dto.TicketResponse;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UserHistoryResponse {
    private BigDecimal currentPoints;
    private List<TicketResponse> pastTickets;
}