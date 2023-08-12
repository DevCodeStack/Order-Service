package com.eatza.order.service.itemservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eatza.order.exception.OrderException;
import com.eatza.order.model.OrderedItem;
import com.eatza.order.repository.OrderedItemRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

	@Autowired
	OrderedItemRepository itemRepository;
	
	public OrderedItem saveItem(OrderedItem item) throws OrderException {
		try {
			return itemRepository.save(item);
		} catch(Exception ex) {
			throw new OrderException(ex.getMessage());
		}
	}

	@Override
	public Double findAmountbyOrderId(Long orderId) throws OrderException {
		try {
			log.debug("In find amount by order id method in service, calling repository to fetch");
			return itemRepository.findAmountByOrderId(orderId);
		} catch(Exception ex) {
			throw new OrderException(ex.getMessage());
		}
	}
	@Override
	public void deleteItemsById(Long id) throws OrderException {
		try {
			log.debug("In delete item by id method, calling repository");
			itemRepository.deleteById(id);
		} catch(Exception ex) {
			throw new OrderException(ex.getMessage());
		}
	}

	@Override
	public List<OrderedItem> findbyOrderId(Long orderId) throws OrderException {
		try {
			log.debug("In find item by order id method in service, calling repository to fetch");
			return itemRepository.findByOrderId(orderId);
		} catch(Exception ex) {
			throw new OrderException(ex.getMessage());
		}
	}


}
