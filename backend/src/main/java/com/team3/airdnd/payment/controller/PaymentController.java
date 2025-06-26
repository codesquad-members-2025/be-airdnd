package com.team3.airdnd.payment.controller;

import java.util.List;

import com.team3.airdnd.payment.domain.Payment;
import com.team3.airdnd.payment.dto.CancelRequestDto;
import com.team3.airdnd.payment.dto.PaymentRequestDto;
import com.team3.airdnd.payment.dto.PaymentResponseDto;
import com.team3.airdnd.payment.service.PaymentService;
import com.team3.airdnd.reservation.domain.Reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/approve")
	public ResponseEntity<PaymentResponseDto> approvePayment(@RequestBody PaymentRequestDto paymentRequestDto) {
		Payment payment = paymentService.approvePayment(paymentRequestDto);
		PaymentResponseDto responseDto = PaymentResponseDto.toDto(payment);
		return ResponseEntity.ok(responseDto);
	}

	//paymentId로 조회
	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long paymentId) {
		PaymentResponseDto responseDto = paymentService.getPaymentById(paymentId);
		return ResponseEntity.ok(responseDto);
	}

	//guestId & status 로 조회
	@GetMapping("/by-guest")
	public ResponseEntity<List<PaymentResponseDto>> getPaymentsByGuest(
		@RequestParam Long guestId,
		@RequestParam(required = false) Reservation.Status status
	) {
		List<Payment> payments = paymentService.getPaymentsByGuestId(guestId, status);
		List<PaymentResponseDto> response = payments.stream()
			.map(PaymentResponseDto::toDto)
			.toList();
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{paymentId}/cancel")
	public ResponseEntity<Void> cancelPayment(
		@PathVariable Long paymentId,
		@RequestBody CancelRequestDto cancelRequest
	) {
		paymentService.cancelPayment(paymentId, cancelRequest.getReason());
		return ResponseEntity.noContent().build();
	}


}