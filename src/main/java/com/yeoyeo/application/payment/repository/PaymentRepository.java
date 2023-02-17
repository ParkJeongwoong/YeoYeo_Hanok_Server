package com.yeoyeo.application.payment.repository;

import com.yeoyeo.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByMerchantUid(String MerchantUid);
}
