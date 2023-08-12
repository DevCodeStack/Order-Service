package com.eatza.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eatza.order.dto.ErrorResponseDto;
import com.eatza.order.dto.OrderRequestDto;
import com.eatza.order.dto.OrderUpdateDto;
import com.eatza.order.dto.OrderUpdateResponseDto;
import com.eatza.order.dto.OrderedItemsDto;
import com.eatza.order.exception.CustomGlobalExceptionHandler;
import com.eatza.order.exception.InvalidTokenException;
import com.eatza.order.exception.UnauthorizedException;
import com.eatza.order.model.Order;
import com.eatza.order.model.OrderedItem;
import com.eatza.order.service.orderservice.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

	private MockMvc mockMvc;
	
	@Mock
	OrderService orderService;
	
	@InjectMocks
	OrderController orderController;
	
	private OrderRequestDto orderRequestDto;
	
	private OrderUpdateDto orderUpdateDto;
	
	private List<OrderedItem> orderedItemList;
	
	private JacksonTester<OrderRequestDto> jsonOrderRequestDto;
	
	private JacksonTester<Order> jsonOrder;
	
	private JacksonTester<OrderUpdateDto> jsonOrderUpdateDto;
	
	private JacksonTester<OrderUpdateResponseDto> jsonUpdateResponseDto;
	
	private JacksonTester<ErrorResponseDto> jsonErrorResponseDto;
	
	@BeforeEach
	void setUp() throws Exception {
		
		mockMvc = MockMvcBuilders.standaloneSetup(orderController)
				.setControllerAdvice(new CustomGlobalExceptionHandler())
				.build();
		
		JacksonTester.initFields(this, new ObjectMapper());
		
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
		
		orderUpdateDto = new OrderUpdateDto(1l, orderedItemsDtoList, 1l, 1l);
	}
	

	//Positive test case : placeOrder
	@Test
	void placeOrder_Success() throws Exception {
		Order order = new Order(1l, "CREATED", 1l);
		order.setId(1l);
		
		when(orderService.placeOrder(any())).thenReturn(order);
		
		MockHttpServletResponse response = mockMvc.perform(
				post("/order").contentType(MediaType.APPLICATION_JSON)
				.content(jsonOrderRequestDto.write(orderRequestDto).getJson())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		Order returnedOrder = jsonOrder.parseObject(response.getContentAsString());
		assertEquals(order.getId(), returnedOrder.getId());
		
	}
	
	
	//Negative test case : placeOrder
	@Test
	void placeOrder_Failed() throws Exception {
		Order order = new Order(1l, "CREATED", 1l);
		order.setId(1l);
		
		when(orderService.placeOrder(any())).thenThrow(new InvalidTokenException());
		
		MockHttpServletResponse response = mockMvc.perform(
				post("/order").contentType(MediaType.APPLICATION_JSON)
				.content(jsonOrderRequestDto.write(orderRequestDto).getJson())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	
	//Positive test case : cancelOrder
	@Test
	void cancelOrder_Success() throws Exception {
		Long orderId = 1l;
		Long customerId = 1l;
		
		when(orderService.cancelOrder(any(), any())).thenReturn(true);
		
		MockHttpServletResponse response = mockMvc.perform(
				put("/order/cancel/"+orderId).accept(MediaType.APPLICATION_JSON)
				.param("customerId", customerId.toString())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		String status = response.getContentAsString();
		assertEquals("Order Cancelled Successfully", status);
		
	}
	
	//Negative test case : cancelOrder
	@Test
	void cancelOrder_FailedUpdate() throws Exception {
		Long orderId = 1l;
		Long customerId = 1l;
		
		when(orderService.cancelOrder(any(), any())).thenReturn(false);
		
		MockHttpServletResponse response = mockMvc.perform(
				put("/order/cancel/"+orderId).accept(MediaType.APPLICATION_JSON)
				.param("customerId", customerId.toString())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	//Negative test case : cancelOrder
	@Test
	void cancelOrder_Failed() throws Exception {
		Long orderId = 1l;
		Long customerId = 1l;
		
		when(orderService.cancelOrder(any(), any())).thenThrow(new RuntimeException("Error occured"));
		
		MockHttpServletResponse response = mockMvc.perform(
				put("/order/cancel/"+orderId).accept(MediaType.APPLICATION_JSON)
				.param("customerId", customerId.toString())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	//Positive test case : updateOrder
	@Test
	void updateOrder_Success() throws Exception {
		
		OrderUpdateResponseDto updateResponseDto = new OrderUpdateResponseDto(1l, 1l, "UPDATED", 1l, orderedItemList);
		
		when(orderService.updateOrder(any())).thenReturn(updateResponseDto);
		
		MockHttpServletResponse response = mockMvc.perform(
				put("/order").contentType(MediaType.APPLICATION_JSON)
				.content(jsonOrderUpdateDto.write(orderUpdateDto).getJson())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		OrderUpdateResponseDto responseDto = jsonUpdateResponseDto.parseObject(response.getContentAsString());
		assertEquals(1l, responseDto.getOrderId());
		
	}
	
	//Negative test case : updateOrder
	@Test
	void updateOrder_Failed() throws Exception {
		
		when(orderService.updateOrder(any())).thenThrow(new UnauthorizedException());
		
		MockHttpServletResponse response = mockMvc.perform(
				put("/order").contentType(MediaType.APPLICATION_JSON)
				.content(jsonOrderUpdateDto.write(orderUpdateDto).getJson())
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	//Positive test case : getOrderById
	@Test
	void getOrderById_Success() throws Exception {
		Long orderId = 1l;
		
		when(orderService.getOrderById(any()))
			.thenReturn(Optional.of(orderedItemList.get(0).getOrder()));
		
		MockHttpServletResponse response = mockMvc.perform(
				get("/order/"+orderId).accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		Order responseDto = jsonOrder.parseObject(response.getContentAsString());
		assertEquals(1l, responseDto.getId());
		
	}
	
	//Negative test case : getOrderById
	@Test
	void getOrderById_EmptyOrder() throws Exception {
		Long orderId = 1l;
		
		when(orderService.getOrderById(any()))
			.thenReturn(Optional.empty());
		
		MockHttpServletResponse response = mockMvc.perform(
				get("/order/"+orderId).accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	//Negative test case : getOrderById
	@Test
	void getOrderById_Failed() throws Exception {
		Long orderId = 1l;
		
		when(orderService.getOrderById(any()))
			.thenThrow(new RuntimeException("Error occured"));
		
		MockHttpServletResponse response = mockMvc.perform(
				get("/order/"+orderId).accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	//Positive test case : getOrderAmountByOrderId
	@Test
	void getOrderAmountByOrderId_Success() throws Exception {
		Long orderId = 1l;
		
		when(orderService.getOrderAmountByOrderId(any()))
			.thenReturn(20.0);
		
		MockHttpServletResponse response = mockMvc.perform(
				get("/order/value/"+orderId).accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		assertEquals(20.0, Double.parseDouble(response.getContentAsString()));
		
	}
	
	//Negative test case : getOrderAmountByOrderId
	@Test
	void getOrderAmountByOrderId_ZeroAmount() throws Exception {
		Long orderId = 1l;
		
		when(orderService.getOrderAmountByOrderId(any()))
			.thenReturn(0d);
		
		MockHttpServletResponse response = mockMvc.perform(
				get("/order/value/"+orderId).accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}
	
	//Negative test case : getOrderAmountByOrderId
	@Test
	void getOrderAmountByOrderId_Failed() throws Exception {
		Long orderId = 1l;
		
		when(orderService.getOrderAmountByOrderId(any()))
			.thenThrow(new RuntimeException("Error occured"));
		
		MockHttpServletResponse response = mockMvc.perform(
				get("/order/value/"+orderId).accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer Token"))
				.andReturn().getResponse();
		
		ErrorResponseDto errorResponseDto = jsonErrorResponseDto.parseObject(response.getContentAsString());
		assertEquals("EX500", errorResponseDto.getCode());
		
	}

}
