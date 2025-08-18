package com.example.quarkus.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    public String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    public BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Column(name = "sku", nullable = false, unique = true, length = 50)
    public String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    public ProductCategory category;

    @Column(name = "active", nullable = false)
    public Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Default constructor
    public Product() {
    }

    // Constructor with required fields
    public Product(String name, String description, BigDecimal price, Integer quantity, String sku) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.sku = sku;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with all fields
    public Product(String name, String description, BigDecimal price, Integer quantity,
            String sku, ProductCategory category, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.sku = sku;
        this.category = category;
        this.active = active;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for Panache queries
    public static Product findBySku(String sku) {
        return find("sku", sku).firstResult();
    }

    public static List<Product> findByCategory(ProductCategory category) {
        return find("category", category).list();
    }

    public static List<Product> findActiveProducts() {
        return find("active", true).list();
    }

    public static List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price >= ?1 and price <= ?2", minPrice, maxPrice).list();
    }

    public static List<Product> findLowStock(int threshold) {
        return find("quantity <= ?1 and active = true", threshold).list();
    }

    // Business logic methods
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }

    public boolean isLowStock(int threshold) {
        return quantity != null && quantity <= threshold;
    }

    public void updateStock(int newQuantity) {
        this.quantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void adjustStock(int adjustment) {
        if (this.quantity != null) {
            this.quantity += adjustment;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', sku='%s', price=%s, quantity=%d, active=%s}",
                id, name, sku, price, quantity, active);
    }
}
