package com.example.quarkus.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.quarkus.entity.Product;
import com.example.quarkus.entity.ProductCategory;
import com.example.quarkus.service.ProductService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductController {

    @Inject
    ProductService productService;

    @GET
    public List<Product> getAllProducts(@QueryParam("active") Boolean active) {
        if (active != null && active) {
            return productService.getActiveProducts();
        }
        return productService.getAllProducts();
    }

    @GET
    @Path("/{id}")
    public Product getProductById(@PathParam("id") Long id) {
        return productService.getProductById(id);
    }

    @GET
    @Path("/sku/{sku}")
    public Product getProductBySku(@PathParam("sku") String sku) {
        return productService.getProductBySku(sku);
    }

    @GET
    @Path("/category/{category}")
    public List<Product> getProductsByCategory(@PathParam("category") ProductCategory category) {
        return productService.getProductsByCategory(category);
    }

    @GET
    @Path("/search")
    public List<Product> searchProducts(@QueryParam("name") String name,
            @QueryParam("minPrice") BigDecimal minPrice,
            @QueryParam("maxPrice") BigDecimal maxPrice) {
        if (name != null && !name.trim().isEmpty()) {
            return productService.searchProductsByName(name);
        }
        if (minPrice != null && maxPrice != null) {
            return productService.getProductsByPriceRange(minPrice, maxPrice);
        }
        return productService.getAllProducts();
    }

    @GET
    @Path("/low-stock")
    public List<Product> getLowStockProducts(@QueryParam("threshold") @DefaultValue("10") int threshold) {
        return productService.getLowStockProducts(threshold);
    }

    @POST
    public Response createProduct(Product product) {
        try {
            Product createdProduct = productService.createProduct(product);
            return Response.status(Response.Status.CREATED).entity(createdProduct).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") Long id, Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            return Response.ok(updatedProduct).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") Long id) {
        try {
            productService.deleteProduct(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/deactivate")
    public Response deactivateProduct(@PathParam("id") Long id) {
        try {
            productService.deactivateProduct(id);
            return Response.ok(Map.of("message", "Product deactivated successfully")).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/activate")
    public Response activateProduct(@PathParam("id") Long id) {
        try {
            productService.activateProduct(id);
            return Response.ok(Map.of("message", "Product activated successfully")).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/stock")
    public Response updateStock(@PathParam("id") Long id, Map<String, Integer> stockUpdate) {
        try {
            Integer quantity = stockUpdate.get("quantity");
            if (quantity == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Quantity is required")).build();
            }
            Product updatedProduct = productService.updateStock(id, quantity);
            return Response.ok(updatedProduct).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/stock/adjust")
    public Response adjustStock(@PathParam("id") Long id, Map<String, Integer> stockAdjustment) {
        try {
            Integer adjustment = stockAdjustment.get("adjustment");
            if (adjustment == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Adjustment is required")).build();
            }
            Product updatedProduct = productService.adjustStock(id, adjustment);
            return Response.ok(updatedProduct).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/stats")
    public Response getProductStats() {
        return Response.ok(Map.of(
                "totalProducts", productService.getProductCount(),
                "activeProducts", productService.getActiveProductCount())).build();
    }

    @GET
    @Path("/categories")
    public ProductCategory[] getCategories() {
        return ProductCategory.values();
    }
}
