package com.example.quarkus.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.quarkus.entity.Product;
import com.example.quarkus.entity.ProductCategory;
import com.example.quarkus.repository.ProductRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.listAll();
    }

    public List<Product> getActiveProducts() {
        return productRepository.findActiveProducts();
    }

    public Product getProductById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        return product;
    }

    public Product getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku);
        if (product == null) {
            throw new NotFoundException("Product not found with SKU: " + sku);
        }
        return product;
    }

    public List<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    @Transactional
    public Product createProduct(@Valid Product product) {
        // Check if SKU already exists
        if (productRepository.existsBySku(product.sku)) {
            throw new IllegalArgumentException("Product with SKU '" + product.sku + "' already exists");
        }

        productRepository.persist(product);
        return product;
    }

    @Transactional
    public Product updateProduct(Long id, @Valid Product updatedProduct) {
        Product existingProduct = getProductById(id);

        // Check if SKU is being changed and if it already exists
        if (!existingProduct.sku.equals(updatedProduct.sku) &&
                productRepository.existsBySku(updatedProduct.sku)) {
            throw new IllegalArgumentException("Product with SKU '" + updatedProduct.sku + "' already exists");
        }

        existingProduct.name = updatedProduct.name;
        existingProduct.description = updatedProduct.description;
        existingProduct.price = updatedProduct.price;
        existingProduct.quantity = updatedProduct.quantity;
        existingProduct.sku = updatedProduct.sku;
        existingProduct.category = updatedProduct.category;
        existingProduct.active = updatedProduct.active;

        return existingProduct;
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        productRepository.deactivateProduct(id);
    }

    @Transactional
    public void activateProduct(Long id) {
        productRepository.activateProduct(id);
    }

    @Transactional
    public Product updateStock(Long id, int quantity) {
        Product product = getProductById(id);
        product.quantity = quantity;
        return product;
    }

    @Transactional
    public Product adjustStock(Long id, int adjustment) {
        Product product = getProductById(id);
        int newQuantity = product.quantity + adjustment;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current quantity: " + product.quantity);
        }
        product.quantity = newQuantity;
        return product;
    }

    public long getProductCount() {
        return productRepository.count();
    }

    public long getActiveProductCount() {
        return productRepository.countActiveProducts();
    }

    public long getProductCountByCategory(ProductCategory category) {
        return productRepository.countByCategory(category);
    }
}
