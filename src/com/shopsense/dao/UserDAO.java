package com.shopsense.dao;

import com.shopsense.config.DatabaseConfig;
import com.shopsense.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, name, email, budget, preferred_categories, preferred_brands, price_sensitivity, created_at FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    public User getUserById(int userId) {
        String sql = "SELECT user_id, name, email, budget, preferred_categories, preferred_brands, price_sensitivity, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching user by ID " + userId + ": " + e.getMessage());
        }
        return null;
    }

    public boolean updateBudget(int userId, double budget) {
        String sql = "UPDATE users SET budget = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, budget);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error updating budget for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updatePreferences(int userId, String categories, String brands, String sensitivity) {
        String sql = "UPDATE users SET preferred_categories = ?, preferred_brands = ?, price_sensitivity = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categories);
            ps.setString(2, brands);
            ps.setString(3, sensitivity);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error updating preferences for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getDouble("budget"),
                rs.getString("preferred_categories"),
                rs.getString("preferred_brands"),
                rs.getString("price_sensitivity"),
                rs.getTimestamp("created_at")
        );
    }
}
