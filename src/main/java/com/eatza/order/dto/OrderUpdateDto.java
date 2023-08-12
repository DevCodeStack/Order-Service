package com.eatza.order.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class OrderUpdateDto {
	
	private Long restaurantId;
	private List<OrderedItemsDto> items;
	private Long orderId;
	private Long customerId;
	
	public OrderUpdateDto(Long restaurantId, List<OrderedItemsDto> items, Long orderId, Long customerId) {
		super();
		this.restaurantId = restaurantId;
		this.items = items;
		this.orderId = orderId;
		this.customerId = customerId;
	}

	
	

	
	
	
	

}
