package com.team3.airdnd.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team3.airdnd.global.dto.ResponseDto;
import com.team3.airdnd.review.dto.ReviewRequestDto;
import com.team3.airdnd.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<ResponseDto<Void>> createReview(@RequestBody @Valid ReviewRequestDto request,
		@RequestParam("guestId") Long guestId) {
		reviewService.createReview(request, guestId);
		return ResponseDto.ok(null);
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<ResponseDto<Void>> deleteReview(@PathVariable Long reviewId,
		@RequestParam("guestId") Long guestId) {
		reviewService.deleteReview(reviewId, guestId);
		return ResponseDto.ok(null);
	}
}
