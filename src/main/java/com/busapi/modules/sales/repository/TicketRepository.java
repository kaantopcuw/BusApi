package com.busapi.modules.sales.repository;

import com.busapi.modules.sales.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Bir seferdeki satılmış (ve iptal edilmemiş) tüm biletler
    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId AND t.status <> 'CANCELLED'")
    List<Ticket> findActiveTicketsByTripId(Long tripId);

    // Belirli bir seferde belirli bir koltuk dolu mu?
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Ticket t WHERE t.trip.id = :tripId AND t.seatNumber = :seatNumber AND t.status <> 'CANCELLED'")
    boolean isSeatOccupied(Long tripId, int seatNumber);

    Optional<Ticket> findByPnrCode(String pnrCode);

    // Belirli tarih aralığındaki toplam ciro (Satılan biletlerin fiyat toplamı)
    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.status = 'SOLD' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal calculateTotalRevenue(LocalDateTime start, LocalDateTime end);

    // Belirli tarih aralığındaki toplam bilet adedi
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'SOLD' AND t.createdAt BETWEEN :start AND :end")
    long countSoldTickets(LocalDateTime start, LocalDateTime end);
}