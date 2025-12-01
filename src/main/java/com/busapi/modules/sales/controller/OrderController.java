package com.busapi.modules.sales.controller;

import com.busapi.core.result.ApiResponse;
import com.busapi.modules.sales.dto.CreateOrderRequest;
import com.busapi.modules.sales.dto.OrderResponse;
import com.busapi.modules.sales.dto.SeatStatusResponse;
import com.busapi.modules.sales.service.OrderService;
import com.busapi.modules.sales.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final TicketService ticketService;

    // 1. Koltuk Durumlarını Getir (Okuma işlemi TicketService'den devam eder)
    @GetMapping("/trip/{tripId}/seats")
    public ApiResponse<List<SeatStatusResponse>> getSeatStatus(@PathVariable UUID tripId) {
        return ApiResponse.success(ticketService.getSeatStatus(tripId));
    }

    // 2. Sipariş Oluştur (Eski 'buyTicket' yerine bu geldi)
    // Artık TicketPurchaseRequest değil, CreateOrderRequest alıyoruz.
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request), "Sipariş başarıyla oluşturuldu.");
    }
}