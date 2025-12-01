package com.busapi.modules.sales.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.identity.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "ticket_orders")
public class TicketOrder extends BaseEntity {

    // Satın alımı yapan kişi (Üye ise User, değilse NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id")
    private User buyer;

    // Misafir ise iletişim bilgileri buraya (Üye ise User'dan da alınabilir ama snapshot olarak burada durması iyidir)
    private String contactEmail;
    private String contactPhone;

    // Tüm biletler için ortak PNR (Rezervasyon Kodu)
    @Column(nullable = false, unique = true)
    private String orderPnr;

    // Finansal Bilgiler
    @Column(nullable = false)
    private BigDecimal totalAmount;

    private String paymentTransactionId; // Iyzico/Stripe ID

    // Fatura Bilgileri (Embeddable)
    @Embedded
    private BillingAddress billingAddress;

    // Siparişe bağlı biletler
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();
}
