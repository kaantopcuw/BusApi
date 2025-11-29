package com.busapi.modules.sales.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.sales.dto.SeatStatusResponse;
import com.busapi.modules.sales.dto.TicketPurchaseRequest;
import com.busapi.modules.sales.dto.TicketResponse;
import com.busapi.modules.sales.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SalesController {

    private final TicketService ticketService;

    // Bir seferin koltuk düzenini ve doluluk durumunu çek
    @GetMapping("/trip/{tripId}/seats")
    public ApiResponse<List<SeatStatusResponse>> getSeatStatus(@PathVariable Long tripId) {
        return ApiResponse.success(ticketService.getSeatStatus(tripId));
    }

    // Bilet Satın Al
    @PostMapping("/ticket")
    public ApiResponse<TicketResponse> buyTicket(@Valid @RequestBody TicketPurchaseRequest request) {
        return ApiResponse.success(ticketService.sellTicket(request), "Bilet satışı başarılı.");
    }
}