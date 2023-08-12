package com.eatza.order.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatza.order.dto.OrderRequestDto;
import com.eatza.order.dto.OrderUpdateDto;
import com.eatza.order.dto.OrderUpdateResponseDto;
import com.eatza.order.exception.OrderException;
import com.eatza.order.model.Order;
import com.eatza.order.service.orderservice.OrderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class OrderController {

	@Autowired
	OrderService orderService;

	@PostMapping("/order")
	@ApiOperation(tags = "OrderController", value = "Place a new order", authorizations = {@Authorization(value = "Bearer")})
	public ResponseEntity<Order> placeOrder(@RequestBody OrderRequestDto orderRequestDto) throws OrderException {
		log.debug("In place order method, calling the service");
		Order order = orderService.placeOrder(orderRequestDto);
		log.debug("Order Placed Successfully");
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(order);

	}
	
	@PutMapping("/order/cancel/{orderId}")
	@ApiOperation(tags = "OrderController", value = "Cancel a order", authorizations = {@Authorization(value = "Bearer")})
	public ResponseEntity<String> cancelOrder(@PathVariable Long orderId, @RequestParam Long customerId) throws OrderException {
		log.debug("In cancel order method");
		boolean result =orderService.cancelOrder(orderId, customerId);
		if(result) {
			log.debug("Order Cancelled Successfully");
			return ResponseEntity
					.status(HttpStatus.OK)
					.body("Order Cancelled Successfully");
		} else {
			log.debug("No records found for respective id");
			throw new OrderException("No records found for respective id");
		}	
	}

	@PutMapping("/order")
	@ApiOperation(tags = "OrderController", value = "Update an order", authorizations = {@Authorization(value = "Bearer")})
	public ResponseEntity<OrderUpdateResponseDto> updateOrder(@RequestBody OrderUpdateDto orderUpdateDto) throws OrderException {

		log.debug(" In updateOrder method, calling service");
		OrderUpdateResponseDto updatedResponse = orderService.updateOrder(orderUpdateDto);
		log.debug("Returning back the object");

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(updatedResponse);


	}

	@GetMapping("/order/{orderId}")
	@ApiOperation(tags = "OrderController", value = "Get order by id", authorizations = {@Authorization(value = "Bearer")})
	public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) throws OrderException {
		log.debug("In get order by id method, calling service to get Order by ID");
		Optional<Order> order = orderService.getOrderById(orderId);
		if(order.isPresent()) {
			log.debug("Got order from service");
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(order.get());
		}
		else {
			log.debug("No orders were found");
			throw new OrderException("No result found for specified inputs");
		}
	}

	@GetMapping("/order/value/{orderId}")
	@ApiOperation(tags = "OrderController", value = "Get order amount by id", authorizations = {@Authorization(value = "Bearer")})
	public ResponseEntity<Double> getOrderAmountByOrderId(@PathVariable Long orderId) throws OrderException{
		log.debug("In get order value by id method, calling service to get Order value");
		double price = orderService.getOrderAmountByOrderId(orderId);

		if(price!=0) {
			log.debug("returning price: "+price);
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(price);
		}
		else {
			log.debug("No result found for specified inputs");
			throw new OrderException("No result found for specified inputs");
		}	
	}

}
