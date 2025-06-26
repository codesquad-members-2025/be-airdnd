package com.team3.airdnd.reservation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team3.airdnd.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	boolean existsByAccommodationId(Long accommodationId);
	Optional<Reservation> findByOrderId(String orderId);
}
