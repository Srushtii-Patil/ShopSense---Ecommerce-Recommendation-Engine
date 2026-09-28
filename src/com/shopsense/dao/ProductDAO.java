package com.shopsense.dao;

import com.shopsense.config.DatabaseConfig;
import com.shopsense.model.Product;

import java.sql.*;
import java.util.*;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, brand, price, rating, review_count, tags, stock_quantity, description, image_url FROM products ORDER BY rating DESC, review_count DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching all products: " + e.getMessage());
        }
        return products;
    }

    public Product getProductById(int productId) {
        String sql = "SELECT product_id, name, category, brand, price, rating, review_count, tags, stock_quantity, description, image_url FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching product by ID " + productId + ": " + e.getMessage());
        }
        return null;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, brand, price, rating, review_count, tags, stock_quantity, description, image_url FROM products WHERE LOWER(category) = LOWER(?) ORDER BY rating DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching products by category " + category + ": " + e.getMessage());
        }
        return products;
    }

    public List<Product> getProductsUnderPrice(double maxPrice) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, category, brand, price, rating, review_count, tags, stock_quantity, description, image_url FROM products WHERE price <= ? ORDER BY price DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching products under price " + maxPrice + ": " + e.getMessage());
        }
        return products;
    }

    public Map<Integer, Double> getPredefinedComplements(int productId) {
        Map<Integer, Double> complements = new LinkedHashMap<>();
        String sql = "SELECT complement_product_id, affinity_score FROM product_complements WHERE product_id = ? ORDER BY affinity_score DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complements.put(rs.getInt("complement_product_id"), rs.getDouble("affinity_score"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching complements for product " + productId + ": " + e.getMessage());
        }
        return complements;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY category ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    public List<String> getAllBrands() {
        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM products ORDER BY brand ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                brands.add(rs.getString("brand"));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error fetching brands: " + e.getMessage());
        }
        return brands;
    }

    public Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("brand"),
                rs.getDouble("price"),
                rs.getDouble("rating"),
                rs.getInt("review_count"),
                rs.getString("tags"),
                rs.getInt("stock_quantity"),
                rs.getString("description"),
                rs.getString("image_url")
        );
    }
}
