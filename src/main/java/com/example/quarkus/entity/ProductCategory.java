package com.example.quarkus.entity;

public enum ProductCategory {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing"),
    BOOKS("Books"),
    HOME_GARDEN("Home & Garden"),
    SPORTS("Sports"),
    TOYS("Toys"),
    AUTOMOTIVE("Automotive"),
    BEAUTY("Beauty"),
    FOOD_BEVERAGE("Food & Beverage"),
    OTHER("Other");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
