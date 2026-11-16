package com.n11bootcamp.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.n11bootcamp.product_service.entity.Product;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
 
    Optional<Product> findByIdAndDeletedFalse(UUID id);
 
    Optional<Product> findBySkuAndDeletedFalse(String sku);
 
    boolean existsBySkuAndDeletedFalse(String sku);
 
    // Stok işlemlerinde race condition'ı önlemek için PESSIMISTIC_WRITE lock
    // Order-service aynı anda N sipariş gönderirse, her biri sırayla işlenir
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);

    List<Product> findAllByIdInAndDeletedFalseAndActiveTrue(List<UUID> ids);

}