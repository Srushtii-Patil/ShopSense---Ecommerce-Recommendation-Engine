package com.shopsense.service;

import com.shopsense.dao.*;
import com.shopsense.engine.CartSuggestionEngine;
import com.shopsense.engine.RecommendationEngine;
import com.shopsense.model.*;

import java.util.*;

public class ShopSenseService {
    private final UserDAO userDAO = new UserDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();
    private final CartSuggestionEngine cartSuggestionEngine = new CartSuggestionEngine();

    // ==================== USER OPERATIONS ====================

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUser(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean updateUserBudget(int userId, double budget) {
        return userDAO.updateBudget(userId, budget);
    }

    public boolean updateUserPreferences(int userId, String categories, String brands, String sensitivity) {
        return userDAO.updatePreferences(userId, categories, brands, sensitivity);
    }

    // ==================== PRODUCT OPERATIONS ====================

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public Product getProduct(int productId) {
        return productDAO.getProductById(productId);
    }

    public List<String> getAllCategories() {
        return productDAO.getAllCategories();
    }

    public List<String> getAllBrands() {
        return productDAO.getAllBrands();
    }

    // ==================== ORDER OPERATIONS ====================

    public List<Order> getUserOrders(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    public List<Product> getUserPurchasedProducts(int userId) {
        return orderDAO.getPurchasedProductsByUser(userId);
    }

    public Order checkout(int userId) {
        List<CartItem> cartItems = cartDAO.getCartItemsByUser(userId);
        if (cartItems.isEmpty()) return null;

        double total = 0.0;
        for (CartItem ci : cartItems) {
            total += ci.getSubtotal();
        }

        Order order = orderDAO.createOrder(userId, total, cartItems);
        if (order != null) {
            cartDAO.clearCart(userId);
        }
        return order;
    }

    // ==================== CART OPERATIONS ====================

    public List<CartItem> getUserCart(int userId) {
        return cartDAO.getCartItemsByUser(userId);
    }

    public double getCartTotal(int userId) {
        return cartDAO.getCartTotal(userId);
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        return cartDAO.addToCart(userId, productId, quantity);
    }

    public boolean updateCartQuantity(int cartItemId, int quantity) {
        return cartDAO.updateQuantity(cartItemId, quantity);
    }

    public boolean removeFromCart(int cartItemId) {
        return cartDAO.removeFromCart(cartItemId);
    }

    public boolean clearCart(int userId) {
        return cartDAO.clearCart(userId);
    }

    // ==================== RECOMMENDATION LOGIC ====================

    public List<Recommendation> getPersonalizedRecommendations(int userId, int limit) {
        User user = userDAO.getUserById(userId);
        if (user == null) return Collections.emptyList();

        List<Product> catalog = productDAO.getAllProducts();
        List<Product> history = orderDAO.getPurchasedProductsByUser(userId);
        List<CartItem> cart = cartDAO.getCartItemsByUser(userId);

        // Fetch co-occurrences based on purchased items + cart items
        Set<Integer> interestProductIds = new HashSet<>();
        for (Product p : history) {
            interestProductIds.add(p.getProductId());
        }
        for (CartItem ci : cart) {
            interestProductIds.add(ci.getProductId());
        }

        Map<Integer, Integer> coOccurrences = orderDAO.getCoOccurrencesForProducts(interestProductIds);

        return recommendationEngine.generateRecommendations(user, catalog, history, cart, coOccurrences, limit);
    }

    public List<CartSuggestion> getDynamicCartSuggestions(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) return Collections.emptyList();

        List<CartItem> cart = cartDAO.getCartItemsByUser(userId);
        List<Product> catalog = productDAO.getAllProducts();

        return cartSuggestionEngine.generateCartSuggestions(user, cart, catalog);
    }
}
