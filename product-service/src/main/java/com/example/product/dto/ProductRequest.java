package com.example.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer and 2 decimal digits")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 1_000_000, message = "Stock cannot exceed 1,000,000")
    private Integer stock;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    private String category;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[A-Za-z0-9\\-_]+$",
        message = "SKU can only contain letters, numbers, hyphens, and underscores"
    )
    private String sku;
}