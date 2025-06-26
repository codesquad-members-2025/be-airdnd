package com.team3.airdnd.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentRequestDto {
	private String paymentKey;
	private String orderId;
	private Long amount;
}