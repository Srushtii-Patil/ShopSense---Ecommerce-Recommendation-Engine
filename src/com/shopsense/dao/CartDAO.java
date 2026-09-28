package com.shopsense.dao;

import com.shopsense.config.DatabaseConfig;
import com.shopsense.model.CartItem;
import com.shopsense.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private final ProductDAO productDAO = new ProductDAO();

    public List<CartItem> getCartItemsByUser(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT c.cart_item_id, c.user_id, c.product_id, c.quantity, c.added_at, " +
                     "p.name, p.category, p.brand, p.price, p.rating, p.review_count, p.tags, p.stock_quantity, p.description, p.image_url " +
                     "FROM cart_items c " +
                     "JOIN products p ON c.product_id = p.product_id " +
                     "WHERE c.user_id = ? " +
                     "ORDER BY c.added_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem(
                            rs.getInt("cart_item_id"),
                            rs.getInt("user_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity"),
                            rs.getTimestamp("added_at")
                    );
                    Product prod = productDAO.mapRowToProduct(rs);
                    item.setProduct(prod);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CartDAO] Error fetching cart items for user " + userId + ": " + e.getMessage());
        }
        return items;
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        // Check if item is already in cart for this user
        String checkSql = "SELECT cart_item_id, quantity FROM cart_items WHERE user_id = ? AND product_id = ?";
        String updateSql = "UPDATE cart_items SET quantity = quantity + ? WHERE cart_item_id = ?";
        String insertSql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, userId);
                psCheck.setInt(2, productId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        int cartItemId = rs.getInt("cart_item_id");
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                            psUpdate.setInt(1, quantity);
                            psUpdate.setInt(2, cartItemId);
                            return psUpdate.executeUpdate() > 0;
                        }
                    }
                }
            }

            // Insert new cart item
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, userId);
                psInsert.setInt(2, productId);
                psInsert.setInt(3, quantity);
                return psInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CartDAO] Error adding product " + productId + " to cart for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromCart(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CartDAO] Error removing cart item " + cartItemId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateQuantity(int cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(cartItemId);
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CartDAO] Error updating quantity for cart item " + cartItemId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[CartDAO] Error clearing cart for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    public double getCartTotal(int userId) {
        List<CartItem> items = getCartItemsByUser(userId);
        double total = 0.0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }
}
