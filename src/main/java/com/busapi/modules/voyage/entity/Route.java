package com.busapi.modules.voyage.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.location.entity.District;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// 1. GÜZERGAH TANIMI (Fiziksel Yol)
@Data
@Entity
@Table(name = "routes")
@EqualsAndHashCode(callSuper = true)
public class Route extends BaseEntity {

    @Column(nullable = false)
    private String name; // Örn: İstanbul - İzmir (Sahil Yolu)

    @ManyToOne
    @JoinColumn(name = "departure_district_id", nullable = false)
    private District departurePoint;

    @ManyToOne
    @JoinColumn(name = "arrival_district_id", nullable = false)
    private District arrivalPoint;

    // Ara duraklar
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC") // Çekerken sıraya diz
    @ToString.Exclude
    private List<RouteStop> stops = new ArrayList<>();
}