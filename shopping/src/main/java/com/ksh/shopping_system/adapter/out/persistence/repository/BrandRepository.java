package com.ksh.shopping_system.adapter.out.persistence.repository;

import com.ksh.shopping_system.adapter.out.persistence.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
	Optional<BrandEntity> findByName(String name);
}

