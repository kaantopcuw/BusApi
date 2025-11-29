package com.busapi.modules.voyage.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.location.entity.District;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

// 2. DURAKLAR (Mola yerleri veya yolcu alma noktaları)
@Data
@Entity
@Table(name = "route_stops")
@EqualsAndHashCode(callSuper = true)
public class RouteStop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(nullable = false)
    private int stopOrder; // 1, 2, 3...

    private int kmFromStart; // Başlangıçtan ne kadar uzak? (Fiyatlama için)
    private int durationMinutesFromStart; // Başlangıçtan ne kadar sürer? (Varış saati hesabı için)
}