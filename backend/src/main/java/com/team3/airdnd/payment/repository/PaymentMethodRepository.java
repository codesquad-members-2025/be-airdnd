package com.team3.airdnd.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team3.airdnd.payment.domain.PaymentMethod;
import com.team3.airdnd.user.domain.User;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
	void deleteByUser(User user);
}