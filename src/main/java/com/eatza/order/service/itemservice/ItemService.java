package com.eatza.order.service.itemservice;

import java.util.List;

import com.eatza.order.exception.OrderException;
import com.eatza.order.model.OrderedItem;

public interface ItemService {
	
	public OrderedItem saveItem(OrderedItem item) throws OrderException;
	public Double findAmountbyOrderId(Long orderId) throws OrderException;
	public void deleteItemsById(Long id) throws OrderException;
	public List<OrderedItem> findbyOrderId(Long orderId) throws OrderException;
	

}
