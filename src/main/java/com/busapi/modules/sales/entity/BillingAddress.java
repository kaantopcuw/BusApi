package com.busapi.modules.sales.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class BillingAddress {
    // Bireysel veya Kurumsal
    private String taxOffice; // Vergi Dairesi (Varsa)
    private String taxNumber; // VKN veya TCKN
    private String fullAddress;
    private String city;
}