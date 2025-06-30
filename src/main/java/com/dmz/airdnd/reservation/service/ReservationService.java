package com.dmz.airdnd.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dmz.airdnd.accommodation.domain.Accommodation;
import com.dmz.airdnd.accommodation.repository.AccommodationRepository;
import com.dmz.airdnd.common.aop.RoleCheck;
import com.dmz.airdnd.common.auth.UserContext;
import com.dmz.airdnd.common.auth.dto.UserInfo;
import com.dmz.airdnd.common.exception.AccommodationNotFound;
import com.dmz.airdnd.common.exception.DuplicateReservationException;
import com.dmz.airdnd.common.exception.ErrorCode;
import com.dmz.airdnd.common.exception.LockInterruptedException;
import com.dmz.airdnd.common.exception.UserNotFoundException;
import com.dmz.airdnd.reservation.domain.Reservation;
import com.dmz.airdnd.reservation.dto.request.ReservationRequest;
import com.dmz.airdnd.reservation.dto.response.ReservationResponse;
import com.dmz.airdnd.reservation.event.ReservationCreatedEvent;
import com.dmz.airdnd.reservation.mapper.ReservationMapper;
import com.dmz.airdnd.reservation.repository.ReservationRepository;
import com.dmz.airdnd.user.domain.Role;
import com.dmz.airdnd.user.domain.User;
import com.dmz.airdnd.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationRepository reservationRepository;

	private final AccommodationRepository accommodationRepository;

	private final UserRepository userRepository;

	private final AvailabilityService availabilityService;

	private final RedisLockService redisLockService;

	private final ApplicationEventPublisher publisher;

	@Transactional
	@RoleCheck(Role.USER)
	public ReservationResponse booking(Long accommodationId, ReservationRequest request) {
		Accommodation accommodation = getAccommodation(accommodationId);
		User guest = getCurrentUser();

		Reservation reservation = ReservationMapper.toEntity(request, guest, accommodation);

		List<String> keys = createKeys(accommodationId, request);

		Reservation newReservation = reserveUnderLock(reservation, accommodation, keys);
		publisher.publishEvent(new ReservationCreatedEvent(newReservation.getId()));

		return ReservationMapper.toResponse(newReservation, accommodation);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected Reservation reserveUnderLock(Reservation reservation, Accommodation accommodation, List<String> keys) {
		AtomicReference<Reservation> saved = new AtomicReference<>(new Reservation());

		try {
			redisLockService.executeWithMultiLock(keys, () -> {
				saved.set(reservationRepository.save(reservation));
				availabilityService.saveReservationDates(accommodation, saved.get());
			});
		} catch (DataIntegrityViolationException e) {
			throw new DuplicateReservationException(ErrorCode.DUPLICATE_RESERVATION);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LockInterruptedException(ErrorCode.LOCK_INTERRUPTED);
		}

		return saved.get();
	}

	private Accommodation getAccommodation(Long accommodationId) {
		return accommodationRepository.findById(accommodationId)
			.orElseThrow(() -> new AccommodationNotFound(ErrorCode.ACCOMMODATION_NOT_FOUND));
	}

	private User getCurrentUser() {
		UserInfo currentUser = UserContext.get();
		return userRepository.findById(currentUser.getId())
			.orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
	}

	private List<String> createKeys(Long accommodationId, ReservationRequest request) {
		List<String> keys = new ArrayList<>();

		LocalDate checkIn = request.getCheckInDate();
		LocalDate checkOut = request.getCheckOutDate();

		for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
			String key = String.format("lock:reservation:%d:%s", accommodationId, date);
			keys.add(key);
		}

		return keys;
	}

}
