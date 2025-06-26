package com.team3.airdnd.review.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team3.airdnd.global.exception.CommonException;
import com.team3.airdnd.global.exception.ErrorCode;
import com.team3.airdnd.reservation.domain.Reservation;
import com.team3.airdnd.reservation.repository.ReservationRepository;
import com.team3.airdnd.review.domain.Review;
import com.team3.airdnd.review.dto.ReviewRequestDto;
import com.team3.airdnd.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReservationRepository reservationRepository;

	@Transactional
	public void createReview(ReviewRequestDto request, Long guestId) {
		Reservation reservation = reservationRepository.findById(request.getReservationId())
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESERVATION));

		validateReviewable(reservation, guestId);
		
		Review review = Review.builder()
			.reservation(reservation)
			.content(request.getContent())
			.rating(request.getRating())
			.build();

		reviewRepository.save(review);
	}

	public void deleteReview(Long reviewId, Long guestId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_REVIEW));

		// 본인 여부 확인
		Long reviewOwnerId = review.getReservation().getGuest().getId();
		if (!reviewOwnerId.equals(guestId)) {
			throw new CommonException(ErrorCode.NOT_AUTHOR_OF_REVIEW);
		}

		reviewRepository.delete(review);
	}

	private void validateReviewable(Reservation reservation, Long guestId) {
		if (!reservation.getGuest().getId().equals(guestId)) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}
		if (reviewRepository.existsByReservationId(reservation.getId())) {
			throw new CommonException(ErrorCode.ALREADY_WRITTEN_REVIEW);
		}
		if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
			throw new CommonException(ErrorCode.INVALID_RESERVATION_STATUS);
		}
		if (reservation.getCheckOut().isAfter(LocalDate.now())) {
			throw new CommonException(ErrorCode.CHECKOUT_NOT_PASSED);
		}
	}
}