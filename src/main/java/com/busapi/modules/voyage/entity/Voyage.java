package com.busapi.modules.voyage.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.fleet.enums.BusType;
import com.busapi.modules.voyage.converter.DayOfWeekSetConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "voyages")
@EqualsAndHashCode(callSuper = true)
@SQLRestriction("is_deleted = false")
public class Voyage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusType busType;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Convert(converter = DayOfWeekSetConverter.class)
    @Column(name = "days_of_week", nullable = false)
    private Set<DayOfWeek> daysOfWeek = new HashSet<>();

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validTo;

    private boolean isActive = true;
}