package com.team3.airdnd.reservation.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team3.airdnd.accommodation.domain.Accommodation;
import com.team3.airdnd.accommodation.repository.AccommodationRepository;
import com.team3.airdnd.chat.service.ChatService;
import com.team3.airdnd.global.exception.CommonException;
import com.team3.airdnd.global.exception.ErrorCode;
import com.team3.airdnd.payment.repository.PaymentRepository;
import com.team3.airdnd.reservation.domain.Reservation;
import com.team3.airdnd.reservation.domain.ReservedDate;
import com.team3.airdnd.reservation.dto.ReservationRequestDto;
import com.team3.airdnd.reservation.dto.ReservationResponseDto;
import com.team3.airdnd.reservation.repository.ReservationRepository;
import com.team3.airdnd.reservation.repository.ReservedDateRepository;
import com.team3.airdnd.user.domain.User;
import com.team3.airdnd.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final AccommodationRepository accommodationRepository;
	private final UserRepository userRepository;
	private final ReservationRepository reservationRepository;
	private final ReservedDateRepository reservedDateRepository;
	private final PaymentRepository paymentRepository;
	private final RedissonClient redissonClient;
	private final ChatService chatService;

	public ReservationResponseDto.ReservationInfoResponseDto getReservationInfo(Long accommodationId, LocalDate checkIn,
		LocalDate checkOut) {
		Accommodation acc = accommodationRepository.findById(accommodationId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));

		boolean available = isAvailable(accommodationId, checkIn, checkOut);

		// 가격 계산
		Price price = calculatePrice(acc, checkIn, checkOut);

		return ReservationResponseDto.ReservationInfoResponseDto.builder()
			.available(available)
			.nights(price.nights())
			.pricePerNight(acc.getPricePerNight())
			.totalPrice(price.total())
			.serviceFee(price.fee())
			.finalPrice(price.total() + price.fee())
			.build();
	}

	public ReservationResponseDto.CreateReservationResponseDto createReservation(
		Long accommodationId, ReservationRequestDto.CreateReservationRequestDto request, Long guestId) {

		List<RLock> locks = acquireLocksForReservation(accommodationId, request.getCheckIn(), request.getCheckOut());
		try {
			Accommodation acc = accommodationRepository.findById(accommodationId)
				.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));

			User user = userRepository.findById(guestId)
				.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

			validateAvailability(acc.getId(), request.getCheckIn(), request.getCheckOut());
			validateGuestCount(request.getGuests(), acc.getMaxGuests());

			Price price = calculatePrice(acc, request.getCheckIn(), request.getCheckOut());

			Reservation reservation = Reservation.builder()
				.guest(user)
				.accommodation(acc)
				.orderId(UUID.randomUUID().toString())
				.checkIn(request.getCheckIn())
				.checkOut(request.getCheckOut())
				.guestCount(request.getGuests())
				.totalPrice(price.total())
				.serviceFee(price.fee())
				.status(Reservation.Status.PENDING)
				.build();

			reservationRepository.save(reservation);
			chatService.createRoomIfNotExists(reservation);

			return ReservationResponseDto.CreateReservationResponseDto.builder()
				.reservationId(reservation.getId())
				.orderId(reservation.getOrderId())
				.status(reservation.getStatus().name())
				.amount(price.total())
				.build();
		} finally {
			releaseLocks(locks);
		}
	}

	@Transactional
	public void confirmReservation(Long reservationId) {
		Reservation reservation = getReservationOrThrow(reservationId);

		reservation.setStatus(Reservation.Status.CONFIRMED);

		// 예약 날짜 등록
		for (LocalDate d = reservation.getCheckIn(); d.isBefore(reservation.getCheckOut()); d = d.plusDays(1)) {
			reservedDateRepository.save(
				ReservedDate.builder()
					.accommodation(reservation.getAccommodation())
					.reservedDate(d)
					.build()
			);
		}
	}

	@Transactional
	public void cancelReservation(Long reservationId) {
		Reservation reservation = getReservationOrThrow(reservationId);

		//체크아웃 이후면 취소 불가
		if (!reservation.getCheckOut().isAfter(LocalDate.now())) {
			throw new CommonException(ErrorCode.CANNOT_CANCEL_AFTER_CHECKOUT);
		}

		reservation.setStatus(Reservation.Status.CANCELLED);

		// 예약 날짜 삭제
		reservedDateRepository.deleteByAccommodationIdAndDateRange(
			reservation.getAccommodation().getId(),
			reservation.getCheckIn(),
			reservation.getCheckOut().minusDays(1) // exclusive
		);

		// 결제 삭제
		paymentRepository.deleteByReservation(reservation);
	}

	// 예약 유효성 검사 (존재하는 예약인지)
	private Reservation getReservationOrThrow(Long reservationId) {
		return reservationRepository.findById(reservationId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESERVATION));
	}

	//예약 가능 날짜 검사
	private void validateAvailability(Long accId, LocalDate checkIn, LocalDate checkOut) {
		List<ReservedDate> conflicts = reservedDateRepository.findOverlappingDates(
			accId, checkIn, checkOut.minusDays(1)
		);
		if (!conflicts.isEmpty()) {
			throw new CommonException(ErrorCode.DUPLICATE_RESERVATION_DATE);
		}
	}

	//금액 계산 (수수료, 총금액)
	private Price calculatePrice(Accommodation acc, LocalDate checkIn, LocalDate checkOut) {
		int days = (int)ChronoUnit.DAYS.between(checkIn, checkOut);
		long totalPrice = days * acc.getPricePerNight();
		long serviceFee = (long)(totalPrice * 0.1); // 수수료 10%
		return new Price(days, totalPrice, serviceFee);
	}

	//예약 가능 여부 확인 (예외 x, Boolean)
	private boolean isAvailable(Long accId, LocalDate checkIn, LocalDate checkOut) {
		List<ReservedDate> conflicts = reservedDateRepository.findOverlappingDates(
			accId, checkIn, checkOut.minusDays(1)
		);
		return conflicts.isEmpty();
	}

	//인원 수 검사 - 애초에 프론트에서 잘못된 값이 넘어왔다고 판단
	private void validateGuestCount(int guestCount, int maxGuests) {
		if (guestCount > maxGuests) {
			throw new CommonException(ErrorCode.EXCEEDS_MAX_GUESTS);
		}
	}

	private record Price(int nights, long total, long fee) {
	}

	private List<RLock> acquireLocksForReservation(Long accommodationId, LocalDate checkIn, LocalDate checkOut) {
		List<LocalDate> dates = checkIn.datesUntil(checkOut).toList();
		List<RLock> locks = new ArrayList<>();

		for (LocalDate date : dates) {
			String key = "lock:reservation:" + accommodationId + ":" + date;
			RLock lock = redissonClient.getLock(key);

			try {
				boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
				if (!locked) {
					throw new CommonException(ErrorCode.LOCK_FAILED, "다른 사용자가 해당 날짜를 예약 중입니다.");
				}
				locks.add(lock);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new CommonException(ErrorCode.LOCK_FAILED, "락 획득 중 인터럽트 발생");
			}
		}
		return locks;
	}

	private void releaseLocks(List<RLock> locks) {
		for (RLock lock : locks) {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

}
