package com.shopsense.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class User {
    private int userId;
    private String name;
    private String email;
    private double budget;
    private String preferredCategoriesRaw;
    private String preferredBrandsRaw;
    private String priceSensitivity; // 'BUDGET', 'BALANCED', 'PREMIUM'
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String name, String email, double budget,
                String preferredCategoriesRaw, String preferredBrandsRaw,
                String priceSensitivity, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.budget = budget;
        this.preferredCategoriesRaw = preferredCategoriesRaw != null ? preferredCategoriesRaw : "";
        this.preferredBrandsRaw = preferredBrandsRaw != null ? preferredBrandsRaw : "";
        this.priceSensitivity = priceSensitivity != null ? priceSensitivity : "BALANCED";
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getPreferredCategoriesRaw() { return preferredCategoriesRaw; }
    public void setPreferredCategoriesRaw(String raw) { this.preferredCategoriesRaw = raw; }

    public List<String> getPreferredCategories() {
        if (preferredCategoriesRaw == null || preferredCategoriesRaw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = preferredCategoriesRaw.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    public String getPreferredBrandsRaw() { return preferredBrandsRaw; }
    public void setPreferredBrandsRaw(String raw) { this.preferredBrandsRaw = raw; }

    public List<String> getPreferredBrands() {
        if (preferredBrandsRaw == null || preferredBrandsRaw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = preferredBrandsRaw.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    public String getPriceSensitivity() { return priceSensitivity; }
    public void setPriceSensitivity(String priceSensitivity) { this.priceSensitivity = priceSensitivity; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean prefersCategory(String category) {
        if (category == null) return false;
        for (String c : getPreferredCategories()) {
            if (c.equalsIgnoreCase(category.trim())) return true;
        }
        return false;
    }

    public boolean prefersBrand(String brand) {
        if (brand == null) return false;
        for (String b : getPreferredBrands()) {
            if (b.equalsIgnoreCase(brand.trim())) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + userId +
                ", name='" + name + '\'' +
                ", budget=$" + String.format("%.2f", budget) +
                ", categories=" + getPreferredCategories() +
                ", brands=" + getPreferredBrands() +
                ", tier='" + priceSensitivity + '\'' +
                '}';
    }
}
