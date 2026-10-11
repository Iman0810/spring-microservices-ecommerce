package com.example.product.service;

import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.entity.Product;
import com.example.product.exception.ConflictException;
import com.example.product.exception.ResourceNotFoundException;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ─────────────────────────────────────────────────────────
    // CACHE NAMES
    // ─────────────────────────────────────────────────────────
    private static final String PRODUCT_CACHE = "products";
    private static final String PRODUCT_BY_SKU_CACHE = "products-by-sku";

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ConflictException("SKU already exists: " + request.getSku());
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .sku(request.getSku())
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // READ — with caching
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Cacheable(value = PRODUCT_CACHE, key = "#id")
    public ProductResponse getById(Long id) {
        log.info("CACHE MISS — fetching product id={} from DB", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = PRODUCT_BY_SKU_CACHE, key = "#sku")
    public ProductResponse getBySku(String sku) {
        log.info("CACHE MISS — fetching product sku={} from DB", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product with SKU", sku));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Pageable pageable) {
        return productRepository.search(query, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStock(Integer threshold) {
        return productRepository.findByStockLessThan(threshold)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE — evicts stale cache entries
    // ─────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = { PRODUCT_CACHE, PRODUCT_BY_SKU_CACHE }, allEntries = true)
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (!product.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new ConflictException("SKU already exists: " + request.getSku());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setSku(request.getSku());

        Product updated = productRepository.save(product);
        log.info("Updated product id={} sku={} — evicted caches", updated.getId(), updated.getSku());

        return toResponse(updated);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE — evicts stale cache entries
    // ─────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = { PRODUCT_CACHE, PRODUCT_BY_SKU_CACHE }, allEntries = true)
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={} — evicted caches", id);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .sku(product.getSku())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}