package com.shopsense.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Product {
    private int productId;
    private String name;
    private String category;
    private String brand;
    private double price;
    private double rating;
    private int reviewCount;
    private String tagsRaw;
    private int stockQuantity;
    private String description;
    private String imageUrl;

    public Product() {}

    public Product(int productId, String name, String category, String brand,
                   double price, double rating, int reviewCount, String tagsRaw,
                   int stockQuantity, String description, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.tagsRaw = tagsRaw != null ? tagsRaw : "";
        this.stockQuantity = stockQuantity;
        this.description = description != null ? description : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getTagsRaw() { return tagsRaw; }
    public void setTagsRaw(String tagsRaw) { this.tagsRaw = tagsRaw; }

    public List<String> getTags() {
        if (tagsRaw == null || tagsRaw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = tagsRaw.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim().toLowerCase();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + productId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", price=$" + String.format("%.2f", price) +
                ", rating=" + rating +
                '}';
    }
}
