package com.dmz.airdnd.reservation.event;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.dmz.airdnd.accommodation.document.AccommodationDocument;
import com.dmz.airdnd.reservation.domain.Reservation;
import com.dmz.airdnd.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationIndexingListener {
	private final ReservationRepository reservationRepository;
	private final ElasticsearchRepository<AccommodationDocument, String> elasticsearchRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ReservationCreatedEvent event) {
		Reservation reservation = reservationRepository.findById(event.getReservationId()).orElseThrow();
		String accommodationId = reservation.getAccommodation().getId().toString();

		LocalDate today = LocalDate.now();
		List<LocalDate> fullWindow = today.datesUntil(today.plusMonths(6).plusDays(1))
			.toList();

		List<LocalDate> reservedDates = reservationRepository
			.findByAccommodationId(reservation.getAccommodation().getId()).stream()
			.flatMap(r -> r.getCheckInDate().datesUntil(r.getCheckOutDate()))
			.toList();

		List<LocalDate> availableDates = fullWindow.stream()
			.filter(d -> !reservedDates.contains(d))
			.collect(Collectors.toList());

		AccommodationDocument existing = elasticsearchRepository.findById(accommodationId)
			.orElseThrow(() -> new IllegalStateException("엘라스틱서치 문서를 찾을 수 없습니다: " + accommodationId));

		AccommodationDocument updated = existing.toBuilder()
			.availableDates(availableDates)
			.build();
		elasticsearchRepository.save(updated);
	}
}
