package com.busapi.modules.sales.dto;

import lombok.Data;

@Data
public class BillingAddressRequest {
    private String city;
    private String fullAddress;
    private String taxOffice;
    private String taxNumber;
}
