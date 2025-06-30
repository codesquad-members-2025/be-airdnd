package com.dmz.airdnd.reservation.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.dmz.airdnd.accommodation.domain.Accommodation;
import com.dmz.airdnd.accommodation.repository.AccommodationRepository;
import com.dmz.airdnd.common.auth.UserContext;
import com.dmz.airdnd.common.auth.dto.UserInfo;
import com.dmz.airdnd.common.exception.DuplicateReservationException;
import com.dmz.airdnd.common.exception.ErrorCode;
import com.dmz.airdnd.fixture.TestAccommodationFactory;
import com.dmz.airdnd.fixture.TestReservationFactory;
import com.dmz.airdnd.fixture.TestUserFactory;
import com.dmz.airdnd.reservation.domain.Reservation;
import com.dmz.airdnd.reservation.dto.request.ReservationRequest;
import com.dmz.airdnd.reservation.dto.response.ReservationResponse;
import com.dmz.airdnd.reservation.repository.ReservationRepository;
import com.dmz.airdnd.user.domain.Role;
import com.dmz.airdnd.user.domain.User;
import com.dmz.airdnd.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private AccommodationRepository accommodationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AvailabilityService availabilityService;

	@Mock
	private RedisLockService redisLockService;

	@Mock
	private ApplicationEventPublisher publisher;

	private Reservation reservation;

	private Accommodation accommodation;

	private User guest;

	@BeforeEach
	void setup() throws InterruptedException {
		UserContext.set(new UserInfo(1L, Role.USER));
		guest = TestUserFactory.createTestUser(1L);
		accommodation = TestAccommodationFactory.createTestAccommodation(1L);
		reservation = TestReservationFactory.createTestReservation(guest, accommodation, LocalDate.of(2025, 7, 21),
			LocalDate.of(2025, 7, 23));

		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(1);
			runnable.run();
			return null;
		}).when(redisLockService).executeWithMultiLock(anyList(), any());
	}

	@Test
	@DisplayName("선택한 날짜에 예약이 없으면 예약이 성공하고, ReservationResponse를 반환한다")
	void success_booking() {
		//given
		ReservationRequest reservationRequest = TestReservationFactory.createReservationRequest();
		when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
		when(userRepository.findById(guest.getId())).thenReturn(Optional.of(guest));
		when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
		doNothing().when(availabilityService).saveReservationDates(accommodation, reservation);

		//when
		ReservationResponse reservationResponse = reservationService.booking(accommodation.getId(), reservationRequest);

		//then
		assertThat(reservationResponse.getCheckInDate()).isEqualTo(reservationRequest.getCheckInDate());
		assertThat(reservationResponse.getCheckOutDate()).isEqualTo(reservationRequest.getCheckOutDate());
		assertThat(reservationResponse.getNumberOfGuests()).isEqualTo(reservationRequest.getNumberOfGuests());
		assertThat(reservationResponse.getTotalPrice()).isEqualTo(reservation.getTotalPrice());
	}

	@Test
	@DisplayName("동일한 날짜에 이미 예약이 있을 경우 예약에 실패하고, DuplicateReservationException 예외가 발생한다.")
	void fail_booking() {
		// given
		ReservationRequest reservationRequest = TestReservationFactory.createReservationRequest();
		when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
		when(userRepository.findById(guest.getId())).thenReturn(Optional.of(guest));
		when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
		doThrow(new DuplicateReservationException(ErrorCode.DUPLICATE_RESERVATION))
			.when(availabilityService)
			.saveReservationDates(accommodation, reservation);

		// when & then
		assertThatThrownBy(() -> reservationService.booking(accommodation.getId(), reservationRequest))
			.isInstanceOf(DuplicateReservationException.class)
			.hasMessage("이미 존재하는 예약입니다.");
	}
}
