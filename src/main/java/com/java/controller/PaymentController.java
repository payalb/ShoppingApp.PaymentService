package com.java.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.dao.PaymentRepository;
import com.java.dto.Payment;
import com.java.dto.Shipping;
import com.java.dto.ShopError;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RequestMapping("/payments")
@RestController
public class PaymentController {
	
	@Autowired PaymentRepository pr;
	@Autowired RestTemplate template;
	
	@HystrixCommand(fallbackMethod="fallbackInsertPayment",commandProperties=
		{@HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value="5"),
			@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value="30000")})
	@PostMapping
	public ResponseEntity insertPayment( Payment payment, BindingResult result ) {
		if ( payment == null ) {
			return ResponseEntity.badRequest().body(ShopError.builder().message(Arrays.asList("Empty details")).build());
		}else if(result.hasErrors()) {
			return ResponseEntity.badRequest().body(ShopError.builder().message(
					result.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.toList())));
		} else {
			Shipping s = Shipping.builder().orderId(payment.getOrderId()).address("dreamdreadream").customerName(payment.getUsername()).build();
			// String orderJson = "{ \"orderId\":\""+ payment.getOrderId() +"\", \"status\":\"Paid\" }";
			ObjectMapper map = new ObjectMapper();
			String shippingJson = null;
			try {
				shippingJson = map.writeValueAsString(s);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType( MediaType.APPLICATION_JSON );
			//HttpEntity<String> orderEntity = new HttpEntity<>( orderJson, headers );
			HttpEntity<String> shippingEntity = new HttpEntity<>( shippingJson, headers );
			//ResponseEntity<Object> OrderResponse = template.exchange("/orders", HttpMethod.PATCH, orderEntity, Object.class);
			template.setUriTemplateHandler(new DefaultUriBuilderFactory("http://shipping-service"));
			ResponseEntity<Object> ShippingResponse = template.exchange("/shippings", HttpMethod.POST, shippingEntity, Object.class);
			if( ShippingResponse.getStatusCodeValue() > 199 && ShippingResponse.getStatusCodeValue() < 300) {// && OrderResponse.getStatusCodeValue() > 199 && OrderResponse.getStatusCodeValue() < 300 ) {
				System.out.println("Change order success");
				pr.save(payment);
				return ResponseEntity.ok().build();
			}
			else {
				System.out.println("Change order fail");
				return ResponseEntity.badRequest().body(ShopError.builder().message(Arrays.asList("update order failed")).build());
			}
		}
	}
	
	public ResponseEntity fallbackInsertPayment(Payment payment, BindingResult result ) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ShopError.builder().message(Arrays.asList("Problem updating shipment/payment. Please try again later")).build());
	}
	
	@GetMapping("/{paymentId}")
	public Payment retrievePayment( @PathVariable String paymentId ) {
		Optional<Payment> p = pr.findById(paymentId);
		if( p.isPresent() )
			return p.get();
		return null;
	}
	
	@GetMapping("/username/{username}")
	public List<Payment> retrievePaymentList( @PathVariable String username ) {
		return pr.findByUsername(username);
	}
	
	@GetMapping("/order/{orderId}")
	public List<Payment> retrievePaymentListByOrderId( @PathVariable Integer orderId ) {
		return pr.findByOrderId(orderId);
	}
	
	@PatchMapping
	public ResponseEntity updatePayment( @RequestBody Payment payment, BindingResult result ) {
		if ( payment == null || !pr.findById(payment.getPaymentId()).isPresent() ) {
			return ResponseEntity.badRequest().body(ShopError.builder().message(Arrays.asList("Details enter invalid")).build());
		}else if(result.hasErrors()) {
			return ResponseEntity.badRequest().body(ShopError.builder().message(
					result.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.toList())));
		} else {
			pr.save(payment);
			return ResponseEntity.ok().build();
		}
	}
	
	@DeleteMapping("/{paymentId}")
	public ResponseEntity deletePayment( @PathVariable String paymentId, BindingResult result ) {
		if ( paymentId == null || !pr.findById( paymentId ).isPresent() ){
			return ResponseEntity.badRequest().body(ShopError.builder().message(Arrays.asList("Details enter invalid")).build());
		} else if(result.hasErrors()) {
			return ResponseEntity.badRequest().body(ShopError.builder().message(
					result.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.toList())));
		} else {
			pr.deleteById(paymentId);
			return ResponseEntity.ok().build();
		}
	}
}
