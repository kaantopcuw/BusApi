package com.busapi.modules.voyage.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.fleet.entity.Bus;
import com.busapi.modules.voyage.enums.TripStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 4. GERÇEKLEŞEN SEFER (Trip) - Bilet buna satılır
@Data
@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_date", columnList = "date")
})
@EqualsAndHashCode(callSuper = true)
public class Trip extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyage voyage;

    @Column(nullable = false)
    private LocalDate date; // Sefer tarihi

    private LocalDateTime departureDateTime; // Tam kalkış zamanı (Date + Time)

    @ManyToOne
    @JoinColumn(name = "bus_id") // O günkü atanan fiziksel otobüs
    private Bus bus;

    @Enumerated(EnumType.STRING)
    private TripStatus status;
}