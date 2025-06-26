package com.team3.airdnd.payment.dto;

import java.time.LocalDateTime;

import com.team3.airdnd.payment.domain.Payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
	private Long id;
	private Long reservationId;
	private Long paymentMethodId;
	private String orderId;
	private Long amount;
	private LocalDateTime paidAt;

	public static PaymentResponseDto toDto(Payment payment) {
		return PaymentResponseDto.builder()
			.id(payment.getId())
			.reservationId(payment.getReservation().getId())
			.paymentMethodId(payment.getPaymentMethod().getId())
			.orderId(payment.getOrderId())
			.amount(payment.getAmount())
			.paidAt(payment.getPaidAt())
			.build();
	}
}