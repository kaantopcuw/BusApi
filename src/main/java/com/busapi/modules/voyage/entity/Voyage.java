package com.busapi.modules.voyage.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.fleet.enums.BusType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;

// 3. SEFER ŞABLONU (Zaman Çizelgesi)
@Data
@Entity
@Table(name = "voyages")
@EqualsAndHashCode(callSuper = true)
public class Voyage extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private LocalTime departureTime; // Kalkış Saati (Örn: 14:00)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusType busType; // Bu seferde hangi tip araç kullanılacak?

    private BigDecimal basePrice; // Taban fiyat
}