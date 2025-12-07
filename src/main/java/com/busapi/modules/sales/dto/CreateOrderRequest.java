package com.busapi.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    // Trip ID varsa bu yeterli
    private UUID tripId;

    // Trip ID yoksa bu ikisi ZORUNLU
    private UUID voyageId; // UUID
    private LocalDate tripDate;

    // Misafir kullanıcı için iletişim bilgileri
    @NotBlank
    private String contactEmail;

    @NotBlank
    private String contactPhone;

    @Valid
    @NotNull(message = "Ödeme bilgisi zorunludur")
    private PaymentInfoRequest paymentInfo;

    // Fatura (Opsiyonel olabilir ama şimdilik alalım)
    @Valid
    private BillingAddressRequest billingAddress;

    // Sepetteki biletler
    @NotEmpty(message = "En az bir bilet seçmelisiniz")
    @Valid
    private List<TicketRequestItem> tickets;
}

