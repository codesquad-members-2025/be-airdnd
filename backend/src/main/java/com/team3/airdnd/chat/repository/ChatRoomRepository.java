package com.team3.airdnd.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team3.airdnd.chat.domain.ChatRoom;
import com.team3.airdnd.reservation.domain.Reservation;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	Optional<ChatRoom> findByReservationId(Long reservationId);

	boolean existsByReservation(Reservation reservation);

	@Query("""
		    SELECT cr FROM ChatRoom cr
		    WHERE cr.guest.id = :userId OR cr.host.id = :userId
		""")
	List<ChatRoom> findByUser(@Param("userId") Long userId);
}
