package com.ksh.shopping_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ksh.shopping_system.adapter.out.persistence.entity.BrandEntity;
import com.ksh.shopping_system.adapter.out.persistence.entity.CategoryEntity;
import com.ksh.shopping_system.adapter.out.persistence.entity.ProductEntity;
import com.ksh.shopping_system.adapter.out.persistence.repository.BrandRepository;
import com.ksh.shopping_system.adapter.out.persistence.repository.CategoryRepository;
import com.ksh.shopping_system.adapter.out.persistence.repository.ProductRepository;

import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		initializeData();
	}

	private void initializeData() {
		Map<String, BrandEntity> brands = new HashMap<>();
		String[] brandNames = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};

		for (String name : brandNames) {
			BrandEntity brand = new BrandEntity(name);
			brandRepository.save(brand);
			brands.put(name, brand);
		}

		Map<String, CategoryEntity> categories = new HashMap<>();
		String[] categoryNames = {"상의", "아우터", "바지", "스니커즈", "가방", "모자", "양말", "액세서리"};

		for (String name : categoryNames) {
			CategoryEntity category = new CategoryEntity(name);
			categoryRepository.save(category);
			categories.put(name, category);
		}

		int[][] prices = {
				{11200, 5500, 4200, 9000, 2000, 1700, 1800, 2300}, // A
				{10500, 5900, 3800, 9100, 2100, 2000, 2000, 2200}, // B
				{10000, 6200, 3300, 9200, 2200, 1900, 2200, 2100}, // C
				{10100, 5100, 3000, 9500, 2500, 1500, 2400, 2000}, // D
				{10700, 5000, 3800, 9900, 2300, 1800, 2100, 2100}, // E
				{11200, 7200, 4000, 9300, 2100, 1600, 2300, 1900}, // F
				{10500, 5800, 3900, 9000, 2200, 1700, 2100, 2000}, // G
				{10800, 6300, 3100, 9700, 2100, 1600, 2000, 2000}, // H
				{11400, 6700, 3200, 9500, 2400, 1700, 1700, 2400}  // I
		};

		for (int i = 0; i < brandNames.length; i++) {
			BrandEntity brand = brands.get(brandNames[i]);

			for (int j = 0; j < categoryNames.length; j++) {
				CategoryEntity category = categories.get(categoryNames[j]);
				Long price = Long.valueOf(prices[i][j]);

				ProductEntity product = new ProductEntity(brand, category, price);
				productRepository.save(product);
			}
		}

		System.out.println("Data initialization completed: " +
				brandNames.length + " brands, " +
				categoryNames.length + " categories, and " +
				(brandNames.length * categoryNames.length) + " products created.");
	}

}