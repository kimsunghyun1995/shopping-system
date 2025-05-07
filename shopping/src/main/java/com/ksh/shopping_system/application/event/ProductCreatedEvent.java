package com.ksh.shopping_system.application.event;

import com.ksh.shopping_system.domain.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductCreatedEvent extends ApplicationEvent {

	private final Product product;

	public ProductCreatedEvent(Object source, Product product) {
		super(source);
		this.product = product;
	}

}