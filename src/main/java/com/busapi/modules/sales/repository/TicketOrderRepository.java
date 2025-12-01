package com.busapi.modules.sales.repository;

import com.busapi.modules.sales.entity.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {
    Optional<TicketOrder> findByOrderPnr(String orderPnr);
}