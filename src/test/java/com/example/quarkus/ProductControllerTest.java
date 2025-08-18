package com.example.quarkus;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.jupiter.api.Test;

import com.example.quarkus.entity.Product;
import com.example.quarkus.entity.ProductCategory;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@QuarkusTest
public class ProductControllerTest {

    @Test
    public void testGetAllProducts() {
        given()
                .when().get("/api/products")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void testGetCategories() {
        given()
                .when().get("/api/products/categories")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testGetProductStats() {
        given()
                .when().get("/api/products/stats")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("totalProducts", greaterThanOrEqualTo(0))
                .body("activeProducts", greaterThanOrEqualTo(0));
    }

    @Test
    public void testCreateProduct() {
        Product product = new Product();
        product.name = "Test Product";
        product.description = "Test Description";
        product.price = new BigDecimal("99.99");
        product.quantity = 10;
        product.sku = "TEST-001";
        product.category = ProductCategory.ELECTRONICS;
        product.active = true;

        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when().post("/api/products")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", is("Test Product"))
                .body("sku", is("TEST-001"))
                .body("category", is("ELECTRONICS"));
    }

    @Test
    public void testCreateProductWithDuplicateSku() {
        // First create a product
        Product product1 = new Product();
        product1.name = "First Product";
        product1.description = "First Description";
        product1.price = new BigDecimal("50.00");
        product1.quantity = 5;
        product1.sku = "DUPLICATE-SKU";
        product1.category = ProductCategory.BOOKS;
        product1.active = true;

        given()
                .contentType(ContentType.JSON)
                .body(product1)
                .when().post("/api/products")
                .then()
                .statusCode(201);

        // Try to create another product with the same SKU
        Product product2 = new Product();
        product2.name = "Second Product";
        product2.description = "Second Description";
        product2.price = new BigDecimal("75.00");
        product2.quantity = 8;
        product2.sku = "DUPLICATE-SKU";
        product2.category = ProductCategory.CLOTHING;
        product2.active = true;

        given()
                .contentType(ContentType.JSON)
                .body(product2)
                .when().post("/api/products")
                .then()
                .statusCode(400)
                .body("error", containsString("already exists"));
    }

    @Test
    public void testSearchProducts() {
        given()
                .queryParam("name", "test")
                .when().get("/api/products/search")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void testGetLowStockProducts() {
        given()
                .queryParam("threshold", 5)
                .when().get("/api/products/low-stock")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
