package com.java;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.java.dao.PaymentRepository;
import com.java.dto.Payment;
import com.java.dto.Payment.Action;
import com.java.dto.Payment.PaymentMethod;

import brave.sampler.Sampler;

@EnableMongoRepositories(basePackages= {"com.java.dao"})
@EntityScan(basePackages= {"com.example.demo.dto"})
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass=false)
@EnableAspectJAutoProxy(proxyTargetClass=false)
@EnableHystrix
@EnableDiscoveryClient
public class ShoppingAppPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingAppPaymentApplication.class, args);
	}
	
	@Bean
	public Sampler sampler() {
		return Sampler.ALWAYS_SAMPLE;
	}
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		///client side load balancing
	    return new RestTemplate();//it is making a call to shipping service: call eureka server.. how many instances.. which instance to send the request to..
	}
	
	@Profile("!prod")
	@Bean
	public CommandLineRunner populateReviews() {
		return new PaymentPopulator();
	}
	
	public class PaymentPopulator implements CommandLineRunner {

		@Autowired PaymentRepository pr;
		
		@Override
		public void run(String... args) throws Exception {
			Payment o1 = Payment.builder().orderId(1234).username("kaigew").amount(21.21).action(Action.Payment).time(LocalDateTime.now()).method(PaymentMethod.ApplePay).build();
			Payment o2 = Payment.builder().orderId(1235).username("ninaw").amount(12.21).action(Action.Payment).time(LocalDateTime.now()).method(PaymentMethod.Cash).build();
			pr.save(o1);
			pr.save(o2);
		}
	}

}
