package com.eatza.order.service.orderservice;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.eatza.order.dto.ItemFetchDto;
import com.eatza.order.dto.OrderRequestDto;
import com.eatza.order.dto.OrderUpdateDto;
import com.eatza.order.dto.OrderUpdateResponseDto;
import com.eatza.order.dto.OrderedItemsDto;
import com.eatza.order.exception.OrderException;
import com.eatza.order.kafka.KafkaProducer;
import com.eatza.order.model.Order;
import com.eatza.order.model.OrderedItem;
import com.eatza.order.repository.OrderRepository;
import com.eatza.order.service.itemservice.ItemService;
import com.eatza.order.util.JwtTokenUtil;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@Setter
@Slf4j
public class OrderServiceImpl implements OrderService {


	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ItemService itemService;
	
	@Autowired
	KafkaProducer kafkaProducer;

	@Value("${restaurant.service.search-url.item}")
	private String restaurantServiceItemUrl;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public Order placeOrder(OrderRequestDto orderRequest) throws OrderException {
		try {
			log.debug("In place order method, creating order object to persist");
			Long customerId = orderRequest.getCustomerId();
			Order order = new Order(customerId, "CREATED", orderRequest.getRestaurantId());
			log.debug("saving order in db");
			Order savedOrder = orderRepository.save(order);
	
			log.debug("Getting all ordered items to persist");
			List<OrderedItemsDto> itemsDtoList = orderRequest.getItems();
			for(OrderedItemsDto itemDto: itemsDtoList) {
					
				if( itemDto.getQuantity()<=0) {
					orderRepository.delete(order);
					throw new OrderException("Quantity of item cannot be 0");
				}
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setBearerAuth(JwtTokenUtil.globalScopeToken);
				
				HttpEntity<String> entity = new HttpEntity<>(headers);
				
				Map<String, Long> uriVariables = new HashMap<>();
				uriVariables.put("itemId", itemDto.getItemId());
				
				log.debug("Calling restaurant service to get item details");
				ResponseEntity<ItemFetchDto> item = restTemplate.exchange(restaurantServiceItemUrl, HttpMethod.GET, entity, ItemFetchDto.class, uriVariables);
	
				if(!item.hasBody() ) {
					orderRepository.delete(order);
					throw new OrderException("Item not found");
				} 
				if(!(item.getBody().getMenu().getRestaurant().getId().equals(order.getRestaurantId()))) {
					orderRepository.delete(order);
					throw new OrderException("Item not in given restaurant");
				}
				
				Integer hour = LocalTime.now().getHour();
				if(!(hour>= Integer.parseInt(item.getBody().getMenu().getActiveFrom())) ||
						!(hour<= Integer.parseInt(item.getBody().getMenu().getActiveTill()))) {
					orderRepository.delete(order);
					throw new OrderException("Item currently unavailable in given restaurant");
				}
				
				OrderedItem itemToPersist = new OrderedItem(item.getBody().getName(), itemDto.getQuantity(), item.getBody().getPrice(), savedOrder, item.getBody().getId());
				itemService.saveItem(itemToPersist);
			} 
			kafkaProducer.publishOrder(savedOrder);
			log.debug("Saved order to db");
			return savedOrder;
		
		} catch(Exception ex) {
			if(ex instanceof OrderException)
				throw (OrderException) ex;
			throw new OrderException("Something went wrong, looks "
					+ "like restaurant is currently not accepting orders");
		}
	}

	@Override
	public boolean cancelOrder(Long orderId, Long customerId) throws OrderException {
		try {
			
			log.debug("In cancel order service method, calling repository");
			Optional<Order> order = orderRepository.findActiveOrderById(orderId, customerId);
			if(order.isPresent()) {
				log.debug("Order was found in db");
				order.get().setStatus("CANCELLED");
				orderRepository.save(order.get());
				return true;
			}
			else {
				log.debug("Order not found or already cancelled");
				return false;
			}
			
		} catch(Exception exception) {
			throw new OrderException(exception.getMessage());
		}

	}

	@Override
	public Optional<Order> getOrderById(Long id) throws OrderException {
		try {
			
			return orderRepository.findById(id);
		 
		} catch(Exception exception) {
			throw new OrderException(exception.getMessage());
		}
	}

	@Override
	public double getOrderAmountByOrderId(Long orderId) throws OrderException {
		try {
			
			Optional<Order> order = orderRepository.findById(orderId);
			if(order.isPresent())
				return itemService.findAmountbyOrderId(orderId);
			throw new OrderException("Order not found");
		
		} catch(OrderException oex) {
			throw new OrderException(oex.getMessage());
		} catch(Exception ex) {
			throw new OrderException(ex.getMessage());
		}


	}

	@Override
	public OrderUpdateResponseDto updateOrder(OrderUpdateDto orderUpdateRequest) throws OrderException {
		try {
			
			Long customerId = orderUpdateRequest.getCustomerId();
			Order order = new Order(customerId, "UPDATED", orderUpdateRequest.getRestaurantId());
			Optional<Order> previouslyPersistedOrder = orderRepository.findActiveOrderById(orderUpdateRequest.getOrderId(), customerId);
	
			if(!previouslyPersistedOrder.isPresent()) {
				throw new OrderException("Update Failed, respective order not found");
			}
			if(!(orderUpdateRequest.getRestaurantId().equals(previouslyPersistedOrder.get().getRestaurantId() ))) {
				throw new OrderException("Update Failed, cannot change restaurants while updating order");
	
			}
			List<OrderedItem> previouslyOrderedItems= itemService.findbyOrderId(previouslyPersistedOrder.get().getId());
			order.setId(previouslyPersistedOrder.get().getId());
			order.setCreateDateTime(previouslyPersistedOrder.get().getCreateDateTime());
			List<OrderedItemsDto> itemsDtoList = orderUpdateRequest.getItems();
			List<OrderedItem> updateItemsListToReturn = new ArrayList<>();
			for(OrderedItemsDto itemDto: itemsDtoList) {
				
				if(itemDto.getQuantity()<=0 ) {
					// deleting previously updated items 
					for(OrderedItem itemsUpdateToBeReverted: updateItemsListToReturn) {
						itemService.deleteItemsById(itemsUpdateToBeReverted.getId());
					}
					throw new OrderException("Update Failed, quantity cannot be zero");
				}
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setBearerAuth(JwtTokenUtil.globalScopeToken);
				
				HttpEntity<String> entity = new HttpEntity<>(headers);
				
				Map<String, Long> uriVariables = new HashMap<>();
				uriVariables.put("itemId", itemDto.getItemId());
				
				ResponseEntity<ItemFetchDto> item = 
						restTemplate.exchange(restaurantServiceItemUrl, HttpMethod.GET, entity, ItemFetchDto.class, uriVariables);
				if(!item.hasBody()) {
					// deleting previously updated items : if any new updated item is not present
					for(OrderedItem itemsUpdateToBeReverted: updateItemsListToReturn) {
						itemService.deleteItemsById(itemsUpdateToBeReverted.getId());
					}
					throw new OrderException("Update Failed, item not found in menu");
				}
				if(item.getBody().getMenu().getRestaurant().getId()!=order.getRestaurantId() ) {
					// deleting previously updated items 
					for(OrderedItem itemsUpdateToBeReverted: updateItemsListToReturn) {
						itemService.deleteItemsById(itemsUpdateToBeReverted.getId());
					}
					throw new OrderException("Update Failed, item does not belong to respective restaurant");
				}
	
				Integer hour = LocalTime.now().getHour();
				if(!(hour>= Integer.parseInt(item.getBody().getMenu().getActiveFrom())) &&
						!(hour<= Integer.parseInt(item.getBody().getMenu().getActiveTill()))) {
					for(OrderedItem itemsUpdateToBeReverted: updateItemsListToReturn) {
						itemService.deleteItemsById(itemsUpdateToBeReverted.getId());
					}
					throw new OrderException("Item currently unavailable in given restaurant");
				}
	
				OrderedItem itemToPersist = new OrderedItem(item.getBody().getName(), itemDto.getQuantity(), item.getBody().getPrice(),previouslyPersistedOrder.get(), item.getBody().getId());
				itemToPersist.setId(itemDto.getItemId());
				OrderedItem savedItem = itemService.saveItem(itemToPersist);
				updateItemsListToReturn.add(savedItem);
				
	
			}
			for(OrderedItem previouslyOrderedItem: previouslyOrderedItems) {
				itemService.deleteItemsById(previouslyOrderedItem.getId());
			}
			Order savedOrder = orderRepository.save(order);
			kafkaProducer.publishOrder(savedOrder);
			return new OrderUpdateResponseDto(savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getStatus(), savedOrder.getRestaurantId(),updateItemsListToReturn );
		
		} catch(Exception exception) {
			throw new OrderException(exception.getMessage());
		}

	}

}
