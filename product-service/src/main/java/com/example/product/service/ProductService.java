package com.example.product.service;

import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.entity.Product;
import com.example.product.exception.ConflictException;
import com.example.product.exception.ResourceNotFoundException;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    // READ
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
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
    // UPDATE
    // ─────────────────────────────────────────────────────────

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // If SKU is changing, ensure the new one isn't taken by another product
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
        log.info("Updated product id={} sku={}", updated.getId(), updated.getSku());

        return toResponse(updated);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
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