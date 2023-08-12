package com.eatza.order.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.eatza.order.model.Order;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaProducer {
	
	@Autowired
	KafkaTemplate<String, Order> kafkaTemplate;
	
	public void publishOrder(Order order) {
		try {
		kafkaTemplate.send("topicorder", order);
		log.debug("Order published");
		}catch(Exception ex) {
			log.debug("Error while publishing order object : {}", order.getId());
		}
	}

}
