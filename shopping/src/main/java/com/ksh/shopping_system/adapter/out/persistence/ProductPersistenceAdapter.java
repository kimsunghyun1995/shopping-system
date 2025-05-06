package com.ksh.shopping_system.adapter.out.persistence;

import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.adapter.out.persistence.dto.CategoryMinPriceProjection;
import com.ksh.shopping_system.adapter.out.persistence.entity.ProductEntity;
import com.ksh.shopping_system.adapter.out.persistence.mapper.ProductMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.ProductRepository;
import com.ksh.shopping_system.application.port.out.product.DeleteProductPort;
import com.ksh.shopping_system.application.port.out.product.SaveProductPort;
import com.ksh.shopping_system.application.port.out.product.SelectProductPort;
import com.ksh.shopping_system.application.port.out.product.UpdateProductPort;
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
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"product not found: " + productId
				));
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
		// DB에서 기존 엔티티 조회
		ProductEntity oldEntity = productRepository.findById(product.getId())
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"product not found: " + product.getId()
				));
		// 가격 갱신
		oldEntity.changePrice(product.getPriceValue());
		// 나머지 업데이트(필요하다면)
		return productMapper.toDomain(oldEntity);
	}

	@Override
	public Product updateProductPrice(Long productId, Long price) {
		ProductEntity oldEntity = productRepository.findById(productId)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"product not found: " + productId
				));
		oldEntity.changePrice(price);
		return productMapper.toDomain(oldEntity);
	}

	@Override
	public void deleteProduct(Long productId) {
		var entity = productRepository.findById(productId)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"product not found: " + productId
				));
		productRepository.delete(entity);
	}

	@Override
	public Product findLowestPriceByCategory(String categoryName) {
		ProductEntity productEntity = productRepository.findLowestPriceByCategoryName(categoryName)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"category not found or no products: " + categoryName
				));
		return productMapper.toDomain(productEntity);
	}

	@Override
	public Product findHighestPriceByCategory(String categoryName) {
		ProductEntity productEntity = productRepository.findHighestPriceByCategoryName(categoryName)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"category not found or no products: " + categoryName
				));
		return productMapper.toDomain(productEntity);
	}

	@Override
	public List<Product> findByBrandName(String brandName) {
		return productRepository.findByBrandName(brandName).stream()
				.map(productMapper::toDomain)
				.toList();
	}

	@Override
	public List<BrandSumProjection> findBrandSum() {
		return productRepository.findBrandSum();
	}

}

