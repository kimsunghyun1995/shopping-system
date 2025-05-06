package com.ksh.shopping_system.adapter.out.persistence;

import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.adapter.out.persistence.dto.CategoryMinPriceProjection;
import com.ksh.shopping_system.adapter.out.persistence.entity.ProductEntity;
import com.ksh.shopping_system.adapter.out.persistence.mapper.ProductMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.ProductRepository;
import com.ksh.shopping_system.application.port.out.product.*;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.domain.Product;
import com.ksh.shopping_system.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter
		implements SelectProductPort, SaveProductPort, UpdateProductPort, DeleteProductPort {

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	@Override
	public List<Product> findAllProducts() {
		return productRepository.findAll().stream()
				.map(productMapper::toDomain)
				.toList();
	}

	@Override
	public List<Product> findByCategoryName(String categoryName) {
		return productRepository.findByCategoryName(categoryName).stream()
				.map(productMapper::toDomain)
				.toList();
	}

	@Override
	public Product findById(Long productId) {
		ProductEntity productEntity = productRepository.findById(productId)
				.orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, "product not found: " + productId));

		return productMapper.toDomain(productEntity);
	}

	@Override
	public List<CategoryMinPriceProjection> findCategoryMinPrice() {
		return productRepository.findCategoryMinPrice();
	}

	@Override
	public Product saveProduct(Product product) {
		var entity = productMapper.toEntity(product);
		var saved = productRepository.save(entity);
		return productMapper.toDomain(saved);
	}

	@Override
	public Product updateProduct(Product product) {
		var entity = productMapper.toEntity(product);

		ProductEntity oldEntity = productRepository.findById(entity.getId())
				.orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, "product not found: " + entity.getId()));

		oldEntity.changePrice(entity.getPrice());

		return productMapper.toDomain(entity);
	}

	@Override
	public Product updateProductPrice(Long productId, Long price) {
		ProductEntity oldEntity = productRepository.findById(productId)
				.orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, "product not found: " + productId));

		oldEntity.changePrice(price);

		return productMapper.toDomain(oldEntity);
	}

	@Override
	public void deleteProduct(Long productId) {
		var entity = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("상품 없음"));
		productRepository.delete(entity);
	}

	@Override
	public Product findLowestPriceByCategory(String categoryName) {
		ProductEntity productEntity = productRepository.findLowestPriceByCategoryName(categoryName)
				.orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, "category not found: " + categoryName));
		return productMapper.toDomain(productEntity);
	}

	@Override
	public List<Product> findByBrandName(String cheapestBrand) {
		return productRepository.findByBrandName(cheapestBrand).stream()
				.map(productMapper::toDomain)
				.toList();
	}

	@Override
	public List<BrandSumProjection> findBrandSum() {
		return productRepository.findBrandSum();
	}

}
