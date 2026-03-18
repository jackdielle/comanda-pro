package com.saleorigano.repository;

import com.saleorigano.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Product.Category category);
    List<Product> findByAvailableTrue();
    Optional<Product> findByNameAndCategory(String name, Product.Category category);
}
