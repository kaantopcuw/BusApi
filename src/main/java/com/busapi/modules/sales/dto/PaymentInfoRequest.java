package com.busapi.modules.sales.dto;

import com.busapi.modules.sales.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentInfoRequest {
    @NotNull
    private PaymentType paymentType;

    private String transactionId; // Kredi kartı ise Iyzico ID, Nakit ise boş olabilir
}