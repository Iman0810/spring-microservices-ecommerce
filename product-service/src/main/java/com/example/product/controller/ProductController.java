package com.example.product.controller;

import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─────────────────────────────────────────────────────────
    // LIST & SEARCH (public)
    // ─────────────────────────────────────────────────────────

    /**
     * List all products with pagination.
     * GET /products?page=0&size=20&sortBy=name&direction=asc
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(productService.list(pageable));
    }

    /**
     * List products by category with pagination.
     * GET /products/category/Electronics?page=0&size=20
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponse>> listByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(productService.listByCategory(category, pageable));
    }

    /**
     * Search products by name or category (case-insensitive).
     * GET /products/search?q=iphone&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(productService.search(query, pageable));
    }

    /**
     * List products with stock below the given threshold.
     * Useful for admin dashboards (reordering alerts).
     * GET /products/low-stock?threshold=5
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> lowStock(
            @RequestParam(defaultValue = "5") Integer threshold) {
        return ResponseEntity.ok(productService.lowStock(threshold));
    }

    // ─────────────────────────────────────────────────────────
    // READ ONE (public)
    // ─────────────────────────────────────────────────────────

    /**
     * Get a product by id.
     * GET /products/42
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Get a product by SKU.
     * GET /products/sku/IPHONE-15-PRO-256
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getBySku(sku));
    }

    // ─────────────────────────────────────────────────────────
    // CREATE / UPDATE / DELETE (admin-only — enforcement in 8.5)
    // ─────────────────────────────────────────────────────────

    /**
     * Create a new product.
     * POST /products
     * (Auth enforcement will be added in 8.5)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing product.
     * PUT /products/42
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * Delete a product.
     * DELETE /products/42
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Build a Pageable from query params, with safe defaults.
     */
    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        // Guard against abuse: cap page size at 100
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, safeSize, sort);
    }
}