package com.java.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

import com.java.dto.Payment;


@Transactional
public interface PaymentRepository extends MongoRepository<Payment, String> {

	public Payment findByPaymentId(String paymentId);
	
	public List<Payment> findByOrderId( Integer orderId );
	
	public List<Payment> findByUsername( String username );

}