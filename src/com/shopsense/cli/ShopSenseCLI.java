package com.shopsense.cli;

import com.shopsense.config.DatabaseConfig;
import com.shopsense.model.*;
import com.shopsense.service.ShopSenseService;

import java.util.List;
import java.util.Scanner;

public class ShopSenseCLI {
    private final ShopSenseService service = new ShopSenseService();
    private final Scanner scanner = new Scanner(System.in);
    private int currentUserId = 1;

    public void run() {
        System.out.println("===============================================================");
        System.out.println("       SHOPSENSE - E-Commerce Personalized Recommendation      ");
        System.out.println("            Engine & Dynamic Cart Suggestion Advisor           ");
        System.out.println("===============================================================");
        System.out.println("Database Engine: " + DatabaseConfig.getActiveMode());
        System.out.println("JDBC Target:     " + DatabaseConfig.getActiveUrl());
        System.out.println("===============================================================");

        boolean running = true;
        while (running) {
            User currentUser = service.getUser(currentUserId);
            if (currentUser == null) {
                currentUserId = 1;
                currentUser = service.getUser(currentUserId);
            }

            double cartTotal = service.getCartTotal(currentUserId);
            double remainingBudget = Math.max(0.0, currentUser.getBudget() - cartTotal);

            System.out.println("\n-------------------------------------------------------------");
            System.out.printf(" ACTIVE USER: %s (ID: %d) | Budget: $%.2f | Spent in Cart: $%.2f | Remaining: $%.2f\n",
                    currentUser.getName(), currentUser.getUserId(), currentUser.getBudget(), cartTotal, remainingBudget);
            System.out.printf(" Preferences: Categories: %s | Brands: %s | Tier: %s\n",
                    currentUser.getPreferredCategories(), currentUser.getPreferredBrands(), currentUser.getPriceSensitivity());
            System.out.println("-------------------------------------------------------------");
            System.out.println("1. View Personalized Product Recommendations");
            System.out.println("2. View Shopping Cart & Dynamic Suggestions");
            System.out.println("3. Add Product to Cart");
            System.out.println("4. Checkout / Complete Order");
            System.out.println("5. Switch Active User Profile");
            System.out.println("6. Update User Budget or Preferences");
            System.out.println("7. View User Order & Purchase History");
            System.out.println("8. View Full Product Catalog");
            System.out.println("9. Exit Console");
            System.out.print("\nEnter your choice (1-9): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    displayRecommendations(currentUser);
                    break;
                case "2":
                    displayCartAndSuggestions(currentUser);
                    break;
                case "3":
                    handleAddToCart(currentUser);
                    break;
                case "4":
                    handleCheckout(currentUser);
                    break;
                case "5":
                    handleSwitchUser();
                    break;
                case "6":
                    handleUpdatePreferences(currentUser);
                    break;
                case "7":
                    displayOrderHistory(currentUser);
                    break;
                case "8":
                    displayCatalog();
                    break;
                case "9":
                    System.out.println("\nThank you for exploring ShopSense!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection. Please choose an option from 1 to 9.");
            }
        }
    }

    private void displayRecommendations(User user) {
        System.out.println("\n=============================================================");
        System.out.println("        PERSONALIZED PRODUCT RECOMMENDATIONS FOR " + user.getName().toUpperCase());
        System.out.println("=============================================================");
        List<Recommendation> recs = service.getPersonalizedRecommendations(user.getUserId(), 6);

        if (recs.isEmpty()) {
            System.out.println("No recommendations found for current preferences.");
            return;
        }

        int rank = 1;
        for (Recommendation r : recs) {
            Product p = r.getProduct();
            System.out.printf("\n#%d [%.1f pts] %s (ID: %d)\n", rank++, r.getTotalScore(), p.getName(), p.getProductId());
            System.out.printf("   Category: %-12s | Brand: %-10s | Price: $%.2f | Rating: %.1f★ (%d reviews)\n",
                    p.getCategory(), p.getBrand(), p.getPrice(), p.getRating(), p.getReviewCount());
            System.out.printf("   Score Breakdown -> Cat: %.1f | Brand: %.1f | Collab: %.1f | Budget: %.1f | Quality: %.1f\n",
                    r.getCategoryScore(), r.getBrandScore(), r.getHistoryScore(), r.getBudgetScore(), r.getRatingScore());
            System.out.println("   Why Recommended:");
            for (String reason : r.getMatchReasons()) {
                System.out.println("    - " + reason);
            }
        }
    }

    private void displayCartAndSuggestions(User user) {
        System.out.println("\n=============================================================");
        System.out.println("            ACTIVE SHOPPING CART FOR " + user.getName().toUpperCase());
        System.out.println("=============================================================");

        List<CartItem> cartItems = service.getUserCart(user.getUserId());
        double cartTotal = service.getCartTotal(user.getUserId());
        double remaining = Math.max(0.0, user.getBudget() - cartTotal);

        if (cartItems.isEmpty()) {
            System.out.println("Your cart is currently empty.");
        } else {
            for (CartItem item : cartItems) {
                Product p = item.getProduct();
                System.out.printf(" - %-35s x%d @ $%.2f each = $%.2f (CartItemID: %d)\n",
                        p.getName(), item.getQuantity(), p.getPrice(), item.getSubtotal(), item.getCartItemId());
            }
            System.out.println("-------------------------------------------------------------");
            System.out.printf(" TOTAL CART VALUE: $%.2f | REMAINING BUDGET: $%.2f / $%.2f\n",
                    cartTotal, remaining, user.getBudget());
        }

        System.out.println("\n=============================================================");
        System.out.println("        DYNAMIC CART SUGGESTIONS & BUDGET ADD-ONS            ");
        System.out.println("=============================================================");
        List<CartSuggestion> suggestions = service.getDynamicCartSuggestions(user.getUserId());

        if (suggestions.isEmpty()) {
            System.out.println("No cart suggestions at this moment.");
        } else {
            for (CartSuggestion cs : suggestions) {
                Product p = cs.getProduct();
                System.out.printf(" [★ %s] %s (ID: %d) - $%.2f\n", cs.getSuggestionType(), p.getName(), p.getProductId(), p.getPrice());
                System.out.printf("    Trigger: %s | Affinity: %.2f\n", cs.getTriggerItemName(), cs.getAffinityScore());
                System.out.printf("    Reason:  %s\n", cs.getReason());
            }
        }
    }

    private void handleAddToCart(User user) {
        System.out.print("\nEnter Product ID to add: ");
        try {
            int pid = Integer.parseInt(scanner.nextLine().trim());
            Product p = service.getProduct(pid);
            if (p == null) {
                System.out.println("Product ID not found.");
                return;
            }
            System.out.print("Enter quantity (default 1): ");
            String qStr = scanner.nextLine().trim();
            int qty = qStr.isEmpty() ? 1 : Integer.parseInt(qStr);

            boolean success = service.addToCart(user.getUserId(), pid, qty);
            if (success) {
                System.out.printf("Added %d x '%s' to cart successfully!\n", qty, p.getName());
            } else {
                System.out.println("Failed to add product to cart.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private void handleCheckout(User user) {
        List<CartItem> cartItems = service.getUserCart(user.getUserId());
        if (cartItems.isEmpty()) {
            System.out.println("\nCannot checkout: your cart is empty.");
            return;
        }

        double total = service.getCartTotal(user.getUserId());
        System.out.printf("\nConfirm checkout for %d items totaling $%.2f? (y/n): ", cartItems.size(), total);
        String confirm = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(confirm) || "yes".equals(confirm)) {
            Order order = service.checkout(user.getUserId());
            if (order != null) {
                System.out.printf("\nSuccess! Order #%d has been processed for $%.2f.\n", order.getOrderId(), order.getTotalAmount());
                System.out.println("Items added to your purchase history. Recommendation weights have been updated!");
            } else {
                System.out.println("Checkout failed.");
            }
        } else {
            System.out.println("Checkout cancelled.");
        }
    }

    private void handleSwitchUser() {
        List<User> users = service.getAllUsers();
        System.out.println("\nSelect a user profile:");
        for (User u : users) {
            System.out.printf(" [%d] %-15s | Budget: $%.2f | Tier: %-8s | Likes: %s\n",
                    u.getUserId(), u.getName(), u.getBudget(), u.getPriceSensitivity(), u.getPreferredCategories());
        }
        System.out.print("\nEnter User ID to switch to: ");
        try {
            int newId = Integer.parseInt(scanner.nextLine().trim());
            User u = service.getUser(newId);
            if (u != null) {
                currentUserId = newId;
                System.out.println("Switched to active user: " + u.getName());
            } else {
                System.out.println("User ID not found.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void handleUpdatePreferences(User user) {
        System.out.println("\n--- UPDATE BUDGET & PREFERENCES ---");
        System.out.printf("Current Budget: $%.2f. Enter new budget (or press Enter to keep): ", user.getBudget());
        String bStr = scanner.nextLine().trim();
        if (!bStr.isEmpty()) {
            try {
                double newBudget = Double.parseDouble(bStr);
                service.updateUserBudget(user.getUserId(), newBudget);
                user.setBudget(newBudget);
                System.out.println("Budget updated to $" + newBudget);
            } catch (Exception e) {
                System.out.println("Invalid budget value.");
            }
        }

        System.out.printf("Current Categories: %s. Enter new comma-separated categories (or Enter to keep): ", user.getPreferredCategoriesRaw());
        String cStr = scanner.nextLine().trim();
        String newCats = cStr.isEmpty() ? user.getPreferredCategoriesRaw() : cStr;

        System.out.printf("Current Brands: %s. Enter new comma-separated brands (or Enter to keep): ", user.getPreferredBrandsRaw());
        String brStr = scanner.nextLine().trim();
        String newBrands = brStr.isEmpty() ? user.getPreferredBrandsRaw() : brStr;

        System.out.printf("Current Tier: %s (BUDGET / BALANCED / PREMIUM). Enter new tier (or Enter to keep): ", user.getPriceSensitivity());
        String tStr = scanner.nextLine().trim();
        String newTier = tStr.isEmpty() ? user.getPriceSensitivity() : tStr.toUpperCase();

        service.updateUserPreferences(user.getUserId(), newCats, newBrands, newTier);
        System.out.println("Preferences saved successfully!");
    }

    private void displayOrderHistory(User user) {
        System.out.println("\n=============================================================");
        System.out.println("              ORDER & PURCHASE HISTORY FOR " + user.getName().toUpperCase());
        System.out.println("=============================================================");

        List<Order> orders = service.getUserOrders(user.getUserId());
        if (orders.isEmpty()) {
            System.out.println("No past orders on record.");
            return;
        }

        for (Order o : orders) {
            System.out.printf("\nOrder #%d | Date: %s | Total: $%.2f | Status: %s\n",
                    o.getOrderId(), o.getOrderDate(), o.getTotalAmount(), o.getStatus());
            for (OrderItem item : o.getItems()) {
                Product p = item.getProduct();
                System.out.printf("   - %-32s (%s) x%d @ $%.2f\n",
                        p != null ? p.getName() : "Item #" + item.getProductId(),
                        p != null ? p.getCategory() : "",
                        item.getQuantity(), item.getUnitPrice());
            }
        }
    }

    private void displayCatalog() {
        System.out.println("\n=============================================================");
        System.out.println("                     FULL PRODUCT CATALOG                    ");
        System.out.println("=============================================================");
        List<Product> products = service.getAllProducts();
        for (Product p : products) {
            System.out.printf("[%d] %-36s | %-12s | %-8s | $%-7.2f | %.1f★\n",
                    p.getProductId(), p.getName(), p.getCategory(), p.getBrand(), p.getPrice(), p.getRating());
        }
    }
}
