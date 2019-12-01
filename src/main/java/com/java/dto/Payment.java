package com.java.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Payment {
	
	@Id
	private String paymentId;
	private Integer orderId;
	private String username;
	private double amount;
	private Action action;
	private LocalDateTime time;
	private PaymentMethod method;
	
	public enum PaymentMethod {
		CreditCard, Paypay, ApplePay, Cash
	}
	
	public enum Action {
		Payment, Refund
	}
}
