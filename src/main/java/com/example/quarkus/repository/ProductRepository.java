package com.example.quarkus.repository;

import java.math.BigDecimal;
import java.util.List;

import com.example.quarkus.entity.Product;
import com.example.quarkus.entity.ProductCategory;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public Product findBySku(String sku) {
        return find("sku", sku).firstResult();
    }

    public List<Product> findByCategory(ProductCategory category) {
        return find("category", category).list();
    }

    public List<Product> findActiveProducts() {
        return find("active", true).list();
    }

    public List<Product> findByNameContaining(String name) {
        return find("lower(name) like lower(?1)", "%" + name + "%").list();
    }

    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price >= ?1 and price <= ?2", minPrice, maxPrice).list();
    }

    public List<Product> findLowStockProducts(int threshold) {
        return find("quantity <= ?1 and active = true", threshold).list();
    }

    public long countByCategory(ProductCategory category) {
        return count("category", category);
    }

    public long countActiveProducts() {
        return count("active", true);
    }

    public boolean existsBySku(String sku) {
        return count("sku", sku) > 0;
    }

    public void deactivateProduct(Long id) {
        update("active = false where id = ?1", id);
    }

    public void activateProduct(Long id) {
        update("active = true where id = ?1", id);
    }
}
