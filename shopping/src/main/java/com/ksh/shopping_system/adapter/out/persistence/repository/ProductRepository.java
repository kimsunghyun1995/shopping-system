package com.ksh.shopping_system.adapter.out.persistence.repository;

import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.adapter.out.persistence.dto.CategoryMinPriceProjection;
import com.ksh.shopping_system.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

	// 특정 카테고리에 대한 모든 상품
	List<ProductEntity> findByCategoryName(String categoryName);

	// 특정 카데고리에 상품 중 최저가
	@Query("SELECT p FROM ProductEntity p " +
			"WHERE p.category.name = :categoryName " +
			"ORDER BY p.price ASC LIMIT 1")
	Optional<ProductEntity> findLowestPriceByCategoryName(String categoryName);

	// 특정 카데고리에 상품 중 최고가
	@Query("SELECT p FROM ProductEntity p " +
			"WHERE p.category.name = :categoryName " +
			"ORDER BY p.price DESC LIMIT 1")
	Optional<ProductEntity> findHighestPriceByCategoryName(String categoryName);

	// 카데고리별 최저가
	@Query("SELECT p.category.name AS categoryName, p.brand.name, MIN(p.price) AS minPrice " +
			"FROM ProductEntity p " +
			"GROUP BY p.category.name")
	List<CategoryMinPriceProjection> findCategoryMinPrice();

	@Query("""
       SELECT p.brand.name AS brandName,
              SUM(p.price)  AS totalPrice
         FROM ProductEntity p
        GROUP BY p.brand.name
    """)
	List<BrandSumProjection> findBrandSum();

	List<ProductEntity> findByBrandName(String cheapestBrand);

}
