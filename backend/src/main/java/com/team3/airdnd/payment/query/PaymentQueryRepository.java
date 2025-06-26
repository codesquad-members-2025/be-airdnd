package com.team3.airdnd.payment.query;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.airdnd.payment.domain.Payment;
import com.team3.airdnd.payment.domain.QPayment;
import com.team3.airdnd.payment.domain.QPaymentMethod;
import com.team3.airdnd.reservation.domain.QReservation;
import com.team3.airdnd.reservation.domain.Reservation;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentQueryRepository {

	private final JPAQueryFactory queryFactory;

	public List<Payment> findByGuestIdAndOptionalStatus(Long guestId, Reservation.Status status) {
		QPayment payment = QPayment.payment;
		QReservation reservation = QReservation.reservation;
		QPaymentMethod paymentMethod = QPaymentMethod.paymentMethod;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(reservation.guest.id.eq(guestId));
		if (status != null) {
			builder.and(reservation.status.eq(status));
		}

		return queryFactory
			.selectFrom(payment)
			.join(payment.reservation, reservation).fetchJoin()
			.join(payment.paymentMethod, paymentMethod).fetchJoin()
			.where(builder)
			.fetch();
	}
}
