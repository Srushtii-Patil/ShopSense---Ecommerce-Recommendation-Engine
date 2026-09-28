package com.shopsense.dao;

import com.shopsense.config.DatabaseConfig;
import com.shopsense.model.CartItem;
import com.shopsense.model.Order;
import com.shopsense.model.OrderItem;
import com.shopsense.model.Product;

import java.sql.*;
import java.util.*;

public class OrderDAO {
    private final ProductDAO productDAO = new ProductDAO();

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, total_amount, order_date, status FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_id"),
                            rs.getInt("user_id"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("order_date"),
                            rs.getString("status")
                    );
                    order.setItems(getOrderItems(conn, order.getOrderId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error fetching orders for user " + userId + ": " + e.getMessage());
        }
        return orders;
    }

    private List<OrderItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, " +
                     "p.name, p.category, p.brand, p.price, p.rating, p.review_count, p.tags, p.stock_quantity, p.description, p.image_url " +
                     "FROM order_items oi JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getInt("order_item_id"),
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    );
                    Product prod = productDAO.mapRowToProduct(rs);
                    item.setProduct(prod);
                    items.add(item);
                }
            }
        }
        return items;
    }

    public Set<Integer> getPurchasedProductIdsByUser(int userId) {
        Set<Integer> productIds = new HashSet<>();
        String sql = "SELECT DISTINCT oi.product_id FROM orders o JOIN order_items oi ON o.order_id = oi.order_id WHERE o.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error fetching purchased product IDs for user " + userId + ": " + e.getMessage());
        }
        return productIds;
    }

    public List<Product> getPurchasedProductsByUser(int userId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT DISTINCT p.product_id, p.name, p.category, p.brand, p.price, p.rating, p.review_count, p.tags, p.stock_quantity, p.description, p.image_url " +
                     "FROM orders o " +
                     "JOIN order_items oi ON o.order_id = oi.order_id " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE o.user_id = ? " +
                     "ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(productDAO.mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error fetching purchased products for user " + userId + ": " + e.getMessage());
        }
        return products;
    }

    /**
     * Collaborative filtering query:
     * Discovers products co-purchased with a given product in historical orders.
     */
    public Map<Integer, Integer> getFrequentlyBoughtTogether(int productId) {
        Map<Integer, Integer> frequencyMap = new LinkedHashMap<>();
        String sql = "SELECT oi2.product_id, COUNT(DISTINCT oi2.order_id) AS co_count " +
                     "FROM order_items oi1 " +
                     "JOIN order_items oi2 ON oi1.order_id = oi2.order_id AND oi1.product_id != oi2.product_id " +
                     "WHERE oi1.product_id = ? " +
                     "GROUP BY oi2.product_id " +
                     "ORDER BY co_count DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencyMap.put(rs.getInt("product_id"), rs.getInt("co_count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error finding co-occurring items for product " + productId + ": " + e.getMessage());
        }
        return frequencyMap;
    }

    /**
     * Collaborative filtering query for a set of products (e.g. from user's history or current cart)
     */
    public Map<Integer, Integer> getCoOccurrencesForProducts(Collection<Integer> productIds) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return frequencyMap;

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }

        String sql = "SELECT oi2.product_id, COUNT(DISTINCT oi2.order_id) AS co_count " +
                     "FROM order_items oi1 " +
                     "JOIN order_items oi2 ON oi1.order_id = oi2.order_id AND oi1.product_id != oi2.product_id " +
                     "WHERE oi1.product_id IN (" + inClause + ") " +
                     "GROUP BY oi2.product_id " +
                     "ORDER BY co_count DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            for (Integer pid : productIds) {
                ps.setInt(idx++, pid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pid = rs.getInt("product_id");
                    if (!productIds.contains(pid)) {
                        frequencyMap.put(pid, rs.getInt("co_count"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error finding co-occurrences for products: " + e.getMessage());
        }
        return frequencyMap;
    }

    public Order createOrder(int userId, double totalAmount, List<CartItem> items) {
        String insertOrderSql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'COMPLETED')";
        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int orderId = -1;
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, userId);
                psOrder.setDouble(2, totalAmount);
                psOrder.executeUpdate();

                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    }
                }
            }

            if (orderId == -1) {
                conn.rollback();
                return null;
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (CartItem item : items) {
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getProduct() != null ? item.getProduct().getPrice() : 0.0);
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();

            Order order = new Order(orderId, userId, totalAmount, new Timestamp(System.currentTimeMillis()), "COMPLETED");
            return order;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error creating order: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
    }
}
