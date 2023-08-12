package com.eatza.order.service.itemservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eatza.order.exception.OrderException;
import com.eatza.order.model.Order;
import com.eatza.order.model.OrderedItem;
import com.eatza.order.repository.OrderedItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
	
	@Mock
	OrderedItemRepository itemRepository;
	
	@InjectMocks
	ItemServiceImpl itemServiceImpl;
	
	private List<OrderedItem> orderedItemList;

	@BeforeEach
	void setUp() throws Exception {
		
		Order order = new Order(1l, "CREATED", 1l);
		
		OrderedItem firstOrderedItem = new OrderedItem("Dosa", 5, 10.0, order, 1l);
		firstOrderedItem.setId(1l);
		OrderedItem secondOrderedItem = new OrderedItem();
		secondOrderedItem.setName("Rice");
		secondOrderedItem.setQuantity(1);
		secondOrderedItem.setPrice(4.0);
		secondOrderedItem.setOrder(order);
		secondOrderedItem.setItemId(2l);
		secondOrderedItem.setId(2l);
		
		orderedItemList = new ArrayList<>();
		orderedItemList.add(firstOrderedItem);
		orderedItemList.add(secondOrderedItem);
	}

	//Positive test case : saveItem
	@Test
	void saveItem_Success() throws OrderException {
		when(itemRepository.save(any())).thenReturn(orderedItemList.get(0));
		OrderedItem item = itemServiceImpl.saveItem(orderedItemList.get(0));
		
		assertEquals(orderedItemList.get(0), item);
		assertEquals(orderedItemList.get(0).getId(), item.getId());
		assertEquals(orderedItemList.get(0).getItemId(), item.getItemId());
		assertEquals(orderedItemList.get(0).getName(), item.getName());
		assertEquals(orderedItemList.get(0).getPrice(), item.getPrice());
		assertEquals(orderedItemList.get(0).getQuantity(), item.getQuantity());
		assertEquals(orderedItemList.get(0).getOrder().getStatus(), item.getOrder().getStatus());
		assertEquals(orderedItemList.get(0).getOrder().getCustomerId(), item.getOrder().getCustomerId());
		assertEquals(orderedItemList.get(0).getOrder().getRestaurantId(), item.getOrder().getRestaurantId());
	}
	
	//Negative test case : saveItem
	@Test
	void saveItem_Failed() {
		when(itemRepository.save(any())).thenThrow(new RuntimeException());
		assertThrows(OrderException.class, () -> {itemServiceImpl.saveItem(orderedItemList.get(0));});
		
	}
	
	//Positive test case : findAmountbyOrderId
	@Test
	void findAmountbyOrderId_Success() throws OrderException {
		Long orderId = 1l;
		when(itemRepository.findAmountByOrderId(any())).thenReturn(40.0);
		Double result = itemServiceImpl.findAmountbyOrderId(orderId);
		
		assertEquals(40.0, result);
	}
	
	//Negative test case : findAmountbyOrderId
	@Test
	void findAmountbyOrderId_Failed() {
		Long orderId = 1l;
		when(itemRepository.findAmountByOrderId(any())).thenThrow(new RuntimeException());
		assertThrows(OrderException.class, () -> {itemServiceImpl.findAmountbyOrderId(orderId);});
		
	}
	
	//Positive test case : findAmountbyOrderId
	@Test
	void deleteItemsById_Success() throws OrderException {
		Long orderId = 1l;
		doNothing().when(itemRepository).deleteById(any());
		assertDoesNotThrow(() -> itemServiceImpl.deleteItemsById(orderId));
		
	}
	
	//Negative test case : findAmountbyOrderId
	@Test
	void deleteItemsById_Failed() {
		Long orderId = 1l;
		doThrow(new RuntimeException()).when(itemRepository).deleteById(any());
		assertThrows(OrderException.class, () -> {itemServiceImpl.deleteItemsById(orderId);});
		
	}
	
	//Positive test case : findAmountbyOrderId
	@Test
	void findbyOrderId_Success() throws OrderException {
		Long orderId = 1l;
		when(itemRepository.findByOrderId(any())).thenReturn(orderedItemList);
		List<OrderedItem> resultList = itemServiceImpl.findbyOrderId(orderId);
		
		assertEquals(orderedItemList.size(), resultList.size());
	}
	
	//Negative test case : findAmountbyOrderId
	@Test
	void findbyOrderId_Failed() {
		Long orderId = 1l;
		when(itemRepository.findByOrderId(any())).thenThrow(new OrderException());
		assertThrows(OrderException.class, () -> {itemServiceImpl.findbyOrderId(orderId);});
		
	}

}
