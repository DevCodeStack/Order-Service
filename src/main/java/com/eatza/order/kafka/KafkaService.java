package com.eatza.order.kafka;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eatza.order.model.Order;
import com.eatza.order.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaService {
	
	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	KafkaProducer kafkaProducer;
	
	public void updateOrderStatus(Order order) {
		log.debug("In updateOrderStatus");
		try {
		Optional<Order> previousOrder = orderRepository.findActiveOrder(order.getId());
		if(previousOrder.isPresent()) {
			Order savedOrder = orderRepository.save(order);
			kafkaProducer.publishOrder(savedOrder);
		} log.debug("Order is completed or cancelled");
		} catch(Exception ex) {
			log.info("Failed to publish order : ", ex.getMessage());;
		}
		
	}
	
}
