package com.ksh.shopping_system.application.event;

import com.ksh.shopping_system.domain.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductDeletedEvent extends ApplicationEvent {
	private final Product deletedProduct;

	public ProductDeletedEvent(Object source, Product deletedProduct) {
		super(source);
		this.deletedProduct = deletedProduct;
	}

}
