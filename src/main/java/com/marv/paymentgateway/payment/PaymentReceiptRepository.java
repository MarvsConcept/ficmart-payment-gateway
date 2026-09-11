package com.marv.paymentgateway.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, UUID> {

    Optional<PaymentReceipt> findByOrderId(String orderId);

    List<PaymentReceipt> findAllByCustomerIdOrderByCreatedAtDesc(String customerId);
}
