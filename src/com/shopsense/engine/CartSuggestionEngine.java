package com.shopsense.engine;

import com.shopsense.dao.OrderDAO;
import com.shopsense.dao.ProductDAO;
import com.shopsense.model.CartItem;
import com.shopsense.model.CartSuggestion;
import com.shopsense.model.Product;
import com.shopsense.model.User;

import java.util.*;

public class CartSuggestionEngine {
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    public static final double FREE_SHIPPING_THRESHOLD = 100.00;

    /**
     * Computes dynamic suggestions for the user's active cart.
     */
    public List<CartSuggestion> generateCartSuggestions(User user, List<CartItem> cartItems, List<Product> catalog) {
        List<CartSuggestion> suggestions = new ArrayList<>();
        if (user == null || catalog == null) return suggestions;

        Set<Integer> cartProductIds = new HashSet<>();
        double cartTotal = 0.0;
        for (CartItem ci : cartItems) {
            cartProductIds.add(ci.getProductId());
            cartTotal += ci.getSubtotal();
        }

        double remainingBudget = Math.max(0.0, user.getBudget() - cartTotal);
        Map<Integer, CartSuggestion> candidateMap = new LinkedHashMap<>();

        // 1. COMPLEMENTARY SUGGESTIONS (Cross-sell from cart contents)
        for (CartItem ci : cartItems) {
            Product cartProduct = ci.getProduct();
            if (cartProduct == null) {
                cartProduct = productDAO.getProductById(ci.getProductId());
            }
            if (cartProduct == null) continue;

            // Check predefined complements
            Map<Integer, Double> complements = productDAO.getPredefinedComplements(cartProduct.getProductId());
            for (Map.Entry<Integer, Double> entry : complements.entrySet()) {
                int compId = entry.getKey();
                double affinity = entry.getValue();

                if (!cartProductIds.contains(compId) && !candidateMap.containsKey(compId)) {
                    Product compProd = findProduct(catalog, compId);
                    if (compProd != null) {
                        double remainingAfter = remainingBudget - compProd.getPrice();
                        String reason = "Natural complement for " + cartProduct.getName() + " in your cart";
                        if (remainingAfter >= 0) {
                            reason += " (Fits comfortably in remaining budget)";
                        } else {
                            reason += " (Exceeds remaining budget by $" + String.format("%.2f", Math.abs(remainingAfter)) + ")";
                        }

                        candidateMap.put(compId, new CartSuggestion(
                                compProd,
                                cartProduct.getName(),
                                "COMPLEMENT",
                                reason,
                                affinity * 1.5,
                                remainingAfter
                        ));
                    }
                }
            }

            // Check historical co-occurrences in orders
            Map<Integer, Integer> coOccurrences = orderDAO.getFrequentlyBoughtTogether(cartProduct.getProductId());
            for (Map.Entry<Integer, Integer> entry : coOccurrences.entrySet()) {
                int coId = entry.getKey();
                int frequency = entry.getValue();

                if (!cartProductIds.contains(coId) && !candidateMap.containsKey(coId)) {
                    Product coProd = findProduct(catalog, coId);
                    if (coProd != null) {
                        double remainingAfter = remainingBudget - coProd.getPrice();
                        candidateMap.put(coId, new CartSuggestion(
                                coProd,
                                cartProduct.getName(),
                                "COMPLEMENT",
                                "Frequently bought together with " + cartProduct.getName() + " by other customers",
                                1.0 + (frequency * 0.2),
                                remainingAfter
                        ));
                    }
                }
            }
        }

        // 2. FREE SHIPPING THRESHOLD OPTIMIZER
        if (cartTotal > 0 && cartTotal < FREE_SHIPPING_THRESHOLD) {
            double gap = FREE_SHIPPING_THRESHOLD - cartTotal;
            for (Product p : catalog) {
                if (!cartProductIds.contains(p.getProductId()) && !candidateMap.containsKey(p.getProductId())) {
                    // Item price is between gap and gap + $25
                    if (p.getPrice() >= gap && p.getPrice() <= gap + 25.0) {
                        double remainingAfter = remainingBudget - p.getPrice();
                        candidateMap.put(p.getProductId(), new CartSuggestion(
                                p,
                                "Free Shipping Booster",
                                "THRESHOLD_BOOSTER",
                                "Add for $" + String.format("%.2f", p.getPrice()) + " to UNLOCK FREE SHIPPING (Save $9.99 delivery fee)!",
                                1.8,
                                remainingAfter
                        ));
                        break; // Highlight top 1-2 booster items
                    }
                }
            }
        }

        // 3. BUDGET GAP OPTIMIZER (Best-rated accessories that fit remaining budget)
        if (remainingBudget > 10.0) {
            List<Product> affordableAddons = new ArrayList<>();
            for (Product p : catalog) {
                if (!cartProductIds.contains(p.getProductId()) && !candidateMap.containsKey(p.getProductId())) {
                    if (p.getPrice() <= remainingBudget && p.getPrice() <= 60.0) {
                        affordableAddons.add(p);
                    }
                }
            }

            // Sort affordable add-ons by rating and review count
            affordableAddons.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));

            int count = 0;
            for (Product p : affordableAddons) {
                if (count >= 3) break;
                double remainingAfter = remainingBudget - p.getPrice();
                candidateMap.put(p.getProductId(), new CartSuggestion(
                        p,
                        "Budget Maximizer",
                        "BUDGET_SAVER",
                        "High-value add-on: Spend remaining $" + String.format("%.2f", remainingBudget) + " on " + p.getRating() + "★ bestseller",
                        1.2 + (p.getRating() / 10.0),
                        remainingAfter
                ));
                count++;
            }
        }

        suggestions.addAll(candidateMap.values());
        Collections.sort(suggestions);
        return suggestions;
    }

    private Product findProduct(List<Product> catalog, int productId) {
        for (Product p : catalog) {
            if (p.getProductId() == productId) return p;
        }
        return null;
    }
}
