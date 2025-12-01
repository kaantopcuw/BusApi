package com.busapi.modules.sales.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.sales.enums.Gender;
import com.busapi.modules.sales.enums.TicketStatus;
import com.busapi.modules.voyage.entity.Trip;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tickets",
        indexes = {
                //@Index(name = "idx_pnr_code", columnList = "pnrCode", unique = true),
                @Index(name = "idx_ticket_trip", columnList = "trip_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_id", "seat_number"})
        }
)
@EqualsAndHashCode(callSuper = true)
public class Ticket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // YENİ: Bilet artık bir Siparişe bağlı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private TicketOrder order;

    // Bileti satın alan üye (Misafir ise null olabilir)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // --- YOLCU BİLGİLERİ ---
    @Column(nullable = false)
    private String passengerName;

    @Column(nullable = false)
    private String passengerSurname;

    @Column(nullable = false, length = 11)
    private String passengerTc;

    private String passengerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender passengerGender;

    @Column(nullable = false)
    private int seatNumber;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;
}