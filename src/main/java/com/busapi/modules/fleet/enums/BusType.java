package com.busapi.modules.fleet.enums;

public enum BusType {
    STANDARD_2_2("Standart (2+2)"),
    SUITE_2_1("Suit (2+1)");

    private final String label;

    BusType(String label) {
        this.label = label;
    }
}