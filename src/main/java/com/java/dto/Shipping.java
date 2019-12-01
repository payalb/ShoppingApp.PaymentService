package com.java.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shipping {

	private Integer id;
	private String address;
	private String customerName = "anonymous";
	private int storeId;
	private Integer orderId;
	private float weight;
	private float price;
}