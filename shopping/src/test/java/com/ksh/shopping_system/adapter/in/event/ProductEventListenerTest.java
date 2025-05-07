package com.ksh.shopping_system.adapter.in.event;

import com.ksh.shopping_system.application.event.ProductCreatedEvent;
import com.ksh.shopping_system.application.port.out.brand.BrandCachePort;
import com.ksh.shopping_system.application.port.out.product.ProductCachePort;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.Brand;
import com.ksh.shopping_system.domain.Category;
import com.ksh.shopping_system.domain.Product;
import com.ksh.shopping_system.exception.CacheConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.*;


/**
 * 비동기 + Retry test
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductEventListenerTest {

	@MockitoBean
	private ProductCachePort productCachePort;

	@MockitoBean
	private BrandCachePort brandCachePort;

	@Autowired
	private ProductEventListener eventListener;

	@Test
	void handleProductCreated_cacheRetry() {
		// given
		Product product = new Product(1L, new Brand("Nike"), new Category("상의"), new Price(10000));

		given(brandCachePort.getBrandTotal("Nike"))
				.willThrow(new CacheConnectionException("cache fail #1"))
				.willThrow(new CacheConnectionException("cache fail #2"))
				.willReturn(0L);

		// when
		eventListener.handleProductCreated(new ProductCreatedEvent(this, product));
		// ↑ 여기서 eventListener는 스프링이 만든 프록시, @Retryable + @Async가 적용됨

		// then
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(brandCachePort, times(3)).getBrandTotal("Nike");
			verify(brandCachePort, times(1)).putBrandTotal("Nike", 10000L);
		});
	}

}
