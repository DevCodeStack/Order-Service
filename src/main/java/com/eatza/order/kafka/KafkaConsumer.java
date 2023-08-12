package com.eatza.order.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.eatza.order.model.Order;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaConsumer {
	
	@Autowired
	KafkaService kafkaService;

	@KafkaListener(topics = "topicrestaurant", groupId = "json", containerFactory = "kafkaListener")
	public void consumeJson(Order order) {
		log.debug("Order recieved");
		kafkaService.updateOrderStatus(order);
	}
}
