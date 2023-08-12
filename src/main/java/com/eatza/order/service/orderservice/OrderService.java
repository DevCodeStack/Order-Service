package com.eatza.order.service.orderservice;

import java.util.Optional;

import com.eatza.order.dto.OrderRequestDto;
import com.eatza.order.dto.OrderUpdateDto;
import com.eatza.order.dto.OrderUpdateResponseDto;
import com.eatza.order.exception.OrderException;
import com.eatza.order.model.Order;

public interface OrderService  {
	
	public Order placeOrder(OrderRequestDto orderRequest) throws OrderException;
	public boolean cancelOrder(Long orderId, Long customerId) throws OrderException;
	public Optional<Order> getOrderById(Long id) throws OrderException;
	public double getOrderAmountByOrderId(Long id) throws OrderException;
	public OrderUpdateResponseDto updateOrder(OrderUpdateDto orderUpdateRequest) throws OrderException;

}
