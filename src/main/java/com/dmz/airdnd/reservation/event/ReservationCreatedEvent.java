package com.dmz.airdnd.reservation.event;

import lombok.Getter;

@Getter
public class ReservationCreatedEvent {
	private final Long reservationId;

	public ReservationCreatedEvent(Long reservationId) {
		this.reservationId = reservationId;
	}
}
