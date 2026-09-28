package com.shopsense.engine;

import com.shopsense.model.*;

import java.util.*;

public class RecommendationEngine {

    /**
     * Computes personalized product recommendations for a user.
     *
     * @param user The customer profile with budget and preferences
     * @param catalog All available products
     * @param purchaseHistory Products previously purchased by the user
     * @param currentCart Current items in user's cart
     * @param coOccurrences Co-occurrence frequency map from collaborative filtering
     * @param limit Maximum number of recommendations to return
     * @return Sorted list of Recommendations with detailed score breakdowns and reasons
     */
    public List<Recommendation> generateRecommendations(
            User user,
            List<Product> catalog,
            List<Product> purchaseHistory,
            List<CartItem> currentCart,
            Map<Integer, Integer> coOccurrences,
            int limit) {

        List<Recommendation> recommendations = new ArrayList<>();
        if (user == null || catalog == null || catalog.isEmpty()) {
            return recommendations;
        }

        // Collect IDs of already purchased products
        Set<Integer> purchasedIds = new HashSet<>();
        for (Product p : purchaseHistory) {
            purchasedIds.add(p.getProductId());
        }

        // Collect IDs of products in current cart
        Set<Integer> cartProductIds = new HashSet<>();
        double currentCartTotal = 0.0;
        for (CartItem ci : currentCart) {
            cartProductIds.add(ci.getProductId());
            currentCartTotal += ci.getSubtotal();
        }

        double remainingBudget = Math.max(0.0, user.getBudget() - currentCartTotal);

        // Find max co-occurrence for scaling
        int maxCoCount = 1;
        if (coOccurrences != null) {
            for (int count : coOccurrences.values()) {
                if (count > maxCoCount) maxCoCount = count;
            }
        }

        for (Product product : catalog) {
            int pid = product.getProductId();

            // Skip items already in active cart
            if (cartProductIds.contains(pid)) {
                continue;
            }

            // Repurchase policy: Don't recommend expensive durable goods already owned (laptops, phones)
            if (purchasedIds.contains(pid) && isDurableGood(product)) {
                continue;
            }

            Recommendation rec = new Recommendation();
            rec.setProduct(product);

            double categoryScore = computeCategoryScore(user, product, purchaseHistory, rec);
            double brandScore = computeBrandScore(user, product, purchaseHistory, rec);
            double historyScore = computeCollaborativeScore(pid, coOccurrences, maxCoCount, purchaseHistory, rec);
            double budgetScore = computeBudgetScore(user, product, remainingBudget, rec);
            double ratingScore = computeRatingScore(product, rec);

            double totalScore = categoryScore + brandScore + historyScore + budgetScore + ratingScore;
            // Round to 1 decimal place
            totalScore = Math.round(totalScore * 10.0) / 10.0;

            rec.setCategoryScore(categoryScore);
            rec.setBrandScore(brandScore);
            rec.setHistoryScore(historyScore);
            rec.setBudgetScore(budgetScore);
            rec.setRatingScore(ratingScore);
            rec.setTotalScore(totalScore);

            recommendations.add(rec);
        }

        // Sort descending by total score
        Collections.sort(recommendations);

        if (recommendations.size() > limit) {
            return new ArrayList<>(recommendations.subList(0, limit));
        }
        return recommendations;
    }

    private double computeCategoryScore(User user, Product product, List<Product> history, Recommendation rec) {
        double score = 0.0;
        // User explicit preference
        if (user.prefersCategory(product.getCategory())) {
            score += 25.0;
            rec.addMatchReason("Matches your preferred category: " + product.getCategory());
        }

        // Implicit category affinity from past purchases
        int categoryPurchases = 0;
        for (Product p : history) {
            if (p.getCategory().equalsIgnoreCase(product.getCategory())) {
                categoryPurchases++;
            }
        }
        if (categoryPurchases > 0) {
            double boost = Math.min(10.0, categoryPurchases * 3.5);
            score += boost;
            if (!user.prefersCategory(product.getCategory())) {
                rec.addMatchReason("Aligns with past category interest (" + product.getCategory() + ")");
            }
        }
        return Math.min(30.0, score);
    }

    private double computeBrandScore(User user, Product product, List<Product> history, Recommendation rec) {
        double score = 0.0;
        if (user.prefersBrand(product.getBrand())) {
            score += 20.0;
            rec.addMatchReason("Matches your preferred brand: " + product.getBrand());
        }

        // Brand loyalty from past purchases
        boolean boughtBrandBefore = false;
        for (Product p : history) {
            if (p.getBrand().equalsIgnoreCase(product.getBrand())) {
                boughtBrandBefore = true;
                break;
            }
        }
        if (boughtBrandBefore) {
            score += 8.0;
            if (!user.prefersBrand(product.getBrand())) {
                rec.addMatchReason("Brand loyalty: You previously purchased " + product.getBrand());
            }
        }
        return Math.min(25.0, score);
    }

    private double computeCollaborativeScore(int pid, Map<Integer, Integer> coOccurrences, int maxCoCount,
                                             List<Product> history, Recommendation rec) {
        if (coOccurrences == null || !coOccurrences.containsKey(pid)) {
            return 0.0;
        }

        int count = coOccurrences.get(pid);
        double normalized = ((double) count / maxCoCount) * 25.0;

        if (count >= 2) {
            rec.addMatchReason("Frequently bought together by shoppers with similar purchases");
        } else {
            rec.addMatchReason("Popular pairing with items in your purchase history");
        }

        return Math.min(25.0, normalized);
    }

    private double computeBudgetScore(User user, Product product, double remainingBudget, Recommendation rec) {
        double price = product.getPrice();
        double userBudget = user.getBudget();
        String tier = user.getPriceSensitivity() != null ? user.getPriceSensitivity().toUpperCase() : "BALANCED";

        // If product is strictly within remaining budget
        if (price <= remainingBudget) {
            double fitness = 15.0;

            if ("BUDGET".equals(tier)) {
                // Budget shoppers love high value for low price
                if (price <= remainingBudget * 0.5) {
                    fitness += 10.0;
                    rec.addMatchReason("Great budget-friendly pick under $" + String.format("%.2f", remainingBudget));
                } else {
                    fitness += 5.0;
                    rec.addMatchReason("Fits comfortably within remaining budget ($" + String.format("%.2f", remainingBudget) + ")");
                }
            } else if ("PREMIUM".equals(tier)) {
                // Premium shoppers prefer flagship features
                if (price >= remainingBudget * 0.4) {
                    fitness += 10.0;
                    rec.addMatchReason("Premium flagship pick within your budget capacity");
                } else {
                    fitness += 5.0;
                    rec.addMatchReason("Affordable add-on within remaining budget");
                }
            } else { // BALANCED
                fitness += 8.0;
                rec.addMatchReason("Fits within your remaining budget ($" + String.format("%.2f", remainingBudget) + ")");
            }

            return Math.min(25.0, fitness);
        } else if (price <= userBudget) {
            // Over current remaining budget because of items in cart, but within total budget
            rec.addMatchReason("Within total budget ceiling, but exceeds current cart remainder");
            return 8.0;
        } else {
            // Over user total budget
            double overage = (price - userBudget) / userBudget;
            if (overage <= 0.25) {
                rec.addMatchReason("Slight stretch: " + Math.round(overage * 100) + "% above your $" + (int)userBudget + " budget");
                return 4.0;
            }
            return 0.0;
        }
    }

    private double computeRatingScore(Product product, Recommendation rec) {
        double score = 0.0;
        // Rating out of 5 scaled to 8 points
        score += (product.getRating() / 5.0) * 8.0;

        // Review volume credibility up to 2 points
        if (product.getReviewCount() >= 2000) {
            score += 2.0;
            rec.addMatchReason("Top-rated bestseller (" + product.getRating() + "★ from " + product.getReviewCount() + " reviews)");
        } else if (product.getReviewCount() >= 500) {
            score += 1.5;
            rec.addMatchReason("Highly rated (" + product.getRating() + "★ with " + product.getReviewCount() + " reviews)");
        }

        return Math.min(10.0, score);
    }

    private boolean isDurableGood(Product product) {
        String cat = product.getCategory().toLowerCase();
        return cat.contains("laptop") || cat.contains("phone") || cat.contains("computer");
    }
}
