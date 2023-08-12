package com.eatza.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemFetchDto {

	private Long id;
	private String name;
	private String description;
	private double price;
	@JsonProperty("menu")
	private MenuFetchDto menu;

	public ItemFetchDto(Long id, String namee, String description, double price, MenuFetchDto menu) {
		super();
		this.id = id;
		this.name = namee;
		this.description = description;
		this.price = price;
		this.menu = menu;
	}




}
