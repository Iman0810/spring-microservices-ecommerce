package com.example.product.repository;

import com.example.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by exact category — returns a paginated result.
     * Spring generates: SELECT * FROM products WHERE category = ? LIMIT ? OFFSET ?
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Look up a product by SKU (unique identifier).
     */
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    /**
     * Search products by name (case-insensitive) OR category (case-insensitive).
     * Uses a custom JPQL query for LIKE + OR behavior.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    Page<Product> search(@Param("query") String query, Pageable pageable);

    /**
     * Find products that are low in stock — useful for admin dashboards.
     */
    List<Product> findByStockLessThan(Integer threshold);
}