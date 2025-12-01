package com.busapi.modules.sales.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    private String orderPnr;
    private String totalPrice;
    private String contactEmail;
    private List<TicketResponse> tickets;
}
