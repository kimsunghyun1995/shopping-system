package com.ksh.shopping_system.application.event;

import com.ksh.shopping_system.domain.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductUpdatedEvent extends ApplicationEvent {
	private final Product oldProduct;
	private final Product newProduct;

	public ProductUpdatedEvent(Object source, Product oldProduct, Product newProduct) {
		super(source);
		this.oldProduct = oldProduct;
		this.newProduct = newProduct;
	}

}