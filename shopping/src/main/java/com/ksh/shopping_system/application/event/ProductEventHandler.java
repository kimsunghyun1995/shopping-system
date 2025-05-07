package com.ksh.shopping_system.application.event;

import com.ksh.shopping_system.application.port.out.brand.BrandCachePort;
import com.ksh.shopping_system.application.port.out.product.ProductCachePort;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.domain.Product;
import com.ksh.shopping_system.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventHandler {

	private final ProductCachePort productCachePort;
	private final BrandCachePort brandCachePort;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleProductCreated(ProductCreatedEvent event) {
		Product product = event.getProduct();

		updateMinPriceCache(product);
		updateMaxPriceCache(product);

		Long oldTotal = brandCachePort.getBrandTotal(product.getBrand().getName());
		long newTotal = (oldTotal == null ? 0 : oldTotal) + product.getPriceValue();
		brandCachePort.putBrandTotal(product.getBrand().getName(), newTotal);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleProductUpdated(ProductUpdatedEvent event) {
		Product oldProduct = event.getOldProduct();
		Product newProduct = event.getNewProduct();

		updateMinPriceCache(newProduct);
		updateMaxPriceCache(newProduct);

		String brandName = newProduct.getBrand().getName();
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		if (oldTotal == null) {
			throw new DataNotFoundException(
					ErrorCode.BRAND_CACHE_NOT_FOUND,
					"브랜드 캐시에 해당 브랜드가 없습니다: " + brandName
			);
		}
		long newTotal = oldTotal - oldProduct.getPriceValue() + newProduct.getPriceValue();
		brandCachePort.putBrandTotal(brandName, newTotal);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleProductDeleted(ProductDeletedEvent event) {
		Product product = event.getDeletedProduct();
		long oldPrice = product.getPriceValue();
		String brandName = product.getBrand().getName();

		Product minPriceProduct = productCachePort.getMinPrice(product.getCategory());
		Product maxPriceProduct = productCachePort.getMaxPrice(product.getCategory());

		if (minPriceProduct != null && minPriceProduct.getId() == product.getId()) {
			productCachePort.removeMinPrice(product.getCategory().getName());
		}
		if (maxPriceProduct != null && maxPriceProduct.getId() == product.getId()) {
			productCachePort.removeMaxPrice(product.getCategory().getName());
		}

		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		if (oldTotal == null) oldTotal = 0L;
		long newTotal = oldTotal - oldPrice;
		if (newTotal <= 0) {
			brandCachePort.removeBrand(brandName);
		} else {
			brandCachePort.putBrandTotal(brandName, newTotal);
		}

	}

	private void updateMinPriceCache(Product product) {
		Product minPriceProduct = productCachePort.getMinPrice(product.getCategory());
		if (minPriceProduct == null
				|| product.getId() == minPriceProduct.getId()
				|| product.getPriceValue() < minPriceProduct.getPriceValue()) {
			productCachePort.putMinPrice(product.getCategory().getName(), product);
		}
	}

	private void updateMaxPriceCache(Product product) {
		Product maxPriceProduct = productCachePort.getMaxPrice(product.getCategory());
		if (maxPriceProduct == null
				|| product.getId() == maxPriceProduct.getId()
				|| product.getPriceValue() > maxPriceProduct.getPriceValue()) {
			productCachePort.putMaxPrice(product.getCategory().getName(), product);
		}
	}

}
