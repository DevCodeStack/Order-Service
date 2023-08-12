package com.eatza.order.service.orderservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.eatza.order.dto.ItemFetchDto;
import com.eatza.order.dto.MenuFetchDto;
import com.eatza.order.dto.OrderRequestDto;
import com.eatza.order.dto.OrderUpdateDto;
import com.eatza.order.dto.OrderUpdateResponseDto;
import com.eatza.order.dto.OrderedItemsDto;
import com.eatza.order.dto.RestaurantFetchDto;
import com.eatza.order.exception.OrderException;
import com.eatza.order.kafka.KafkaProducer;
import com.eatza.order.model.Order;
import com.eatza.order.model.OrderedItem;
import com.eatza.order.repository.OrderRepository;
import com.eatza.order.service.itemservice.ItemService;
import com.eatza.order.util.ErrorCodesEnum;
import com.eatza.order.util.JwtTokenUtil;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	OrderRepository orderRepository;

	@Mock
	ItemService itemService;
	
	@Mock
	JwtTokenUtil tokenUtil;
	
	@Mock
	KafkaProducer kafkaProducer;

	private String restaurantServiceItemUrl = "http://searchUrl";

	@Mock
	RestTemplate restTemplate;
	
	@InjectMocks
	OrderServiceImpl orderServiceImpl;
	
	private List<OrderedItem> orderedItemList;
	
	private OrderRequestDto orderRequestDto;
	
	private List<ItemFetchDto> itemFetchDtoList;
	
	private OrderUpdateDto orderUpdateDto;
	
	@BeforeEach
	void setUp() throws Exception {
		
		orderServiceImpl.setRestaurantServiceItemUrl(restaurantServiceItemUrl);
		
		Order order = new Order(1l, "CREATED", 1l);
		order.setId(1l);
		
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
		
		OrderedItemsDto firstOrderedItemsDto = new OrderedItemsDto(1l, 5);
		OrderedItemsDto secondOrderedItemsDto = new OrderedItemsDto(2l, 1);
		
		List<OrderedItemsDto> orderedItemsDtoList = new ArrayList<>();
		orderedItemsDtoList.add(firstOrderedItemsDto);
		orderedItemsDtoList.add(secondOrderedItemsDto);
		
		orderRequestDto = new OrderRequestDto();
		orderRequestDto.setRestaurantId(1l);
		orderRequestDto.setItems(orderedItemsDtoList);
		
		RestaurantFetchDto restaurantFetchDto = new RestaurantFetchDto();
		restaurantFetchDto.setId(1l);
		restaurantFetchDto.setName("Hotel Tridev");
		restaurantFetchDto.setLocation("Jatni");
		restaurantFetchDto.setCuisine("Indian veg");
		restaurantFetchDto.setBudget(1000);
		restaurantFetchDto.setRating(4.2);
		
		RestaurantFetchDto newRestaurantFetchDto = new RestaurantFetchDto(2l, "Saffron spice", "Jatni", "Indian mixed", 2000, 4.2);
		
		Integer fromHour = LocalTime.now().getHour();
		Integer toHour = fromHour+3;
		MenuFetchDto morningMenuFetchDto = new MenuFetchDto();
		morningMenuFetchDto.setId(1l);
		morningMenuFetchDto.setActiveFrom(fromHour.toString());
		morningMenuFetchDto.setActiveTill(toHour.toString());
		morningMenuFetchDto.setRestaurant(restaurantFetchDto);
		
		MenuFetchDto afternoonMenuFetchDto = new MenuFetchDto(2l, fromHour.toString(), toHour.toString(), newRestaurantFetchDto);
		
		ItemFetchDto itemFetchDto = new ItemFetchDto();
		itemFetchDto.setId(1l);
		itemFetchDto.setName("Dosa");
		itemFetchDto.setDescription("Onion Dosa");
		itemFetchDto.setPrice(30);
		itemFetchDto.setMenu(morningMenuFetchDto);
		
		ItemFetchDto afternoonItemFetchDto = 
				new ItemFetchDto(2l, "Rice", "Plain Rice", 15, afternoonMenuFetchDto);
		
		itemFetchDtoList = new ArrayList<>();
		itemFetchDtoList.add(itemFetchDto);
		itemFetchDtoList.add(afternoonItemFetchDto);
		orderUpdateDto = new OrderUpdateDto(1l, orderedItemsDtoList, 1l, 1l);
		
		
	}

	//Positive test case : placeOrder
	@Test
	void placeOrder_Success() {
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		
		when(itemService.saveItem(any())).thenReturn(orderedItemList.get(0));
		doNothing().when(kafkaProducer).publishOrder(any());
		Order order = orderServiceImpl.placeOrder(orderRequestDto);
		assertEquals(orderedItemList.get(0).getOrder().getId(), order.getId());
		assertEquals(orderedItemList.get(0).getOrder().getCustomerId(), order.getCustomerId());
		assertEquals(orderedItemList.get(0).getOrder().getRestaurantId(), order.getRestaurantId());
		assertEquals(orderedItemList.get(0).getOrder().getStatus(), order.getStatus());
		
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_ErrorSavingOrderEntity() {
		String authorization = "Bearer token";
		
		
		when(orderRepository.save(any())).thenThrow(new RuntimeException("Error occured while saving order"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
		
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_ErrorSavingItemEntity() {
		String authorization = "Bearer token";

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		
		when(itemService.saveItem(any())).thenThrow(new RuntimeException("Error occured while saving item"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_TokenError() {
		String authorization = "Bearer token";
				
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_RestTemplateError() {
		String authorization = "Bearer token";

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenThrow(new RuntimeException("Error occured"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_QuantityError() {
		String authorization = "Bearer token";
		orderRequestDto.getItems().get(0).setQuantity(0);

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		doNothing().when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_QuantityException() {
		String authorization = "Bearer token";
		orderRequestDto.getItems().get(0).setQuantity(0);

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		doThrow(new RuntimeException("Error occured while deleting")).when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_EmptyItemError() {
		String authorization = "Bearer token";

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(null));
		doNothing().when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_EmptyItemException() {
		String authorization = "Bearer token";

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(null));
		doThrow(new OrderException("Error occured while deleting", ErrorCodesEnum.INTERNAL_SERVER_ERROR)).when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_RestaurantMismatchError() {
		String authorization = "Bearer token";

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(1)));
		doNothing().when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_RestaurantMismatchException() {
		String authorization = "Bearer token";
		itemFetchDtoList.get(0).getMenu().getRestaurant().setId(2l);

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		doThrow(new RuntimeException("Error occured while deleting")).when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_FromHourError() {
		String authorization = "Bearer token";
		Integer fromHour = LocalTime.now().getHour() + 1;
		itemFetchDtoList.get(0).getMenu().setActiveFrom(fromHour.toString());

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		doNothing().when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_TillHourError() {
		String authorization = "Bearer token";
		Integer tillHour = LocalTime.now().getHour() - 1;
		itemFetchDtoList.get(0).getMenu().setActiveTill(tillHour.toString());

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		doNothing().when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_HourException() {
		String authorization = "Bearer token";
		Integer fromHour = LocalTime.now().getHour() + 1;
		itemFetchDtoList.get(0).getMenu().setActiveFrom(fromHour.toString());

		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		doThrow(new RuntimeException("Error occured while deleting")).when(orderRepository).delete(any());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.placeOrder(orderRequestDto);});
	}
	
	//Positive test case : cancelOrder
	@Test
	void cancelOrder_Success() {
		String authorization = "Bearer token";
		Long orderId = 1l;
		Long customerId = 1l;

		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		
		assertDoesNotThrow(() -> {orderServiceImpl.cancelOrder(orderId, customerId);});
	}
	
	//Negative test case : cancelOrder
	@Test
	void cancelOrder_EmptyOrderError() {
		Long orderId = 1l;
		Long customerId = 1l;

		
		when(orderRepository.findActiveOrderById(any(), any()))
		.thenReturn(Optional.empty());
		
		assertDoesNotThrow(() -> {orderServiceImpl.cancelOrder(orderId, customerId);});
	}
	
	//Negative test case : cancelOrder
	@Test
	void cancelOrder_EmptyOrderException() {
		String authorization = "Bearer token";
		Long orderId = 1l;
		Long customerId = 1l;

		
		when(orderRepository.findActiveOrderById(any(), any()))
		.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(orderRepository.save(any())).thenThrow(new RuntimeException("Error occured while saving order"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.cancelOrder(orderId, customerId);});
	}
	
	//Positive test case : getOrderById
	@Test
	void getOrderById_Success() {
		Long orderId = 1l;

		when(orderRepository.findById(any()))
		.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		Order order =  orderServiceImpl.getOrderById(orderId).get();
		
		assertEquals(1l, order.getId());
	}
	
	//Negative test case : getOrderById
	@Test
	void getOrderById_Exception() {
		Long orderId = 1l;

		when(orderRepository.findById(any()))
		.thenThrow(new RuntimeException("Error occured while searching for order"));
		assertThrows(OrderException.class, () -> {orderServiceImpl.getOrderById(orderId);});
	}
	
	//Positive test case : getOrderAmountByOrderId
	@Test
	void getOrderAmountByOrderId_Success() {
		Long orderId = 1l;

		when(orderRepository.findById(any())).thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findAmountbyOrderId(any()))
		.thenReturn(10.0);
		Double amount =  orderServiceImpl.getOrderAmountByOrderId(orderId);
		
		assertEquals(10.0, amount);
	}
	
	//Negative test case : getOrderAmountByOrderId
	@Test
	void getOrderAmountByOrderId_EmptyObjectError() {
		Long orderId = 1l;

		when(orderRepository.findById(any())).thenReturn(Optional.empty());
		assertThrows(OrderException.class, () -> {orderServiceImpl.getOrderAmountByOrderId(orderId);});
	}
	
	//Negative test case : getOrderAmountByOrderId
	@Test
	void getOrderAmountByOrderId_Exception() {
		Long orderId = 1l;

		when(orderRepository.findById(any())).thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findAmountbyOrderId(any()))
		.thenThrow(new RuntimeException("Error occured while searching for order amount"));
		assertThrows(OrderException.class, () -> {orderServiceImpl.getOrderAmountByOrderId(orderId);});
	}
	
	//Positive test case : updateOrder
	@Test
	void updateOrder_Success() {		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		when(itemService.saveItem(any())).thenReturn(orderedItemList.get(0));
		doNothing().when(itemService).deleteItemsById(any());
		
		when(orderRepository.save(any())).thenReturn(orderedItemList.get(0).getOrder());
		
		OrderUpdateResponseDto updateResponseDto = orderServiceImpl.updateOrder(orderUpdateDto);
		assertEquals(1l , updateResponseDto.getOrderId());
		assertEquals(1l , updateResponseDto.getCustomerId());
		assertEquals(1l , updateResponseDto.getRestaurantId());

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_ErrorSavingItemEntity() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		when(itemService.saveItem(any())).thenThrow(new RuntimeException("Error occured while saving order"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_ErrorSavingOrderEntity() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		when(itemService.saveItem(any())).thenReturn(orderedItemList.get(0));
		doNothing().when(itemService).deleteItemsById(any());
		
		when(orderRepository.save(any())).thenThrow(new RuntimeException("Error occured while saving item"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});
		
	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_TokenError() {
		String authorization = "Bearer token";
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_PreviousOrderEmpty() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.empty());
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_PreviousOrderException() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenThrow(new RuntimeException("Error occured while saving item"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_RestaurantMismatch() {
		String authorization = "Bearer token";
		orderUpdateDto.setRestaurantId(2l);
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_OrderedItemsException() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenThrow(new RuntimeException("Error occured"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_QuantityError() {
		String authorization = "Bearer token";
		orderedItemList.get(0).setQuantity(0);
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_RestTemplateException() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenThrow(new RuntimeException("Error occured"));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_ItemEmptyException() {
		String authorization = "Bearer token";
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(null));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_FromHourMismatch() {
		String authorization = "Bearer token";
		Integer fromHour = LocalTime.now().getHour() + 1;
		itemFetchDtoList.get(0).getMenu().setActiveFrom(fromHour.toString());
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_TillHourMismatch() {
		String authorization = "Bearer token";
		Integer tillHour = LocalTime.now().getHour() - 1;
		itemFetchDtoList.get(0).getMenu().setActiveTill(tillHour.toString());
		
		//add getCreateDateTime to order
		
		
		when(orderRepository.findActiveOrderById(any(), any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		when(itemService.findbyOrderId(any())).thenReturn(orderedItemList);
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<ItemFetchDto>>any(),
				ArgumentMatchers.anyMap()))
			.thenReturn(ResponseEntity.ok(itemFetchDtoList.get(0)));
		
		assertThrows(OrderException.class, () -> {orderServiceImpl.updateOrder(orderUpdateDto);});

	}

}
