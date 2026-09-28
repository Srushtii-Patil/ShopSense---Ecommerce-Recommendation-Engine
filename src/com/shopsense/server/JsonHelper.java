package com.shopsense.server;

import com.shopsense.model.*;

import java.util.List;
import java.util.Map;

public class JsonHelper {

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        String hex = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - hex.length(); k++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    public static String userToJson(User u) {
        if (u == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"userId\":").append(u.getUserId()).append(",");
        sb.append("\"name\":\"").append(escape(u.getName())).append("\",");
        sb.append("\"email\":\"").append(escape(u.getEmail())).append("\",");
        sb.append("\"budget\":").append(u.getBudget()).append(",");
        sb.append("\"preferredCategoriesRaw\":\"").append(escape(u.getPreferredCategoriesRaw())).append("\",");
        sb.append("\"preferredBrandsRaw\":\"").append(escape(u.getPreferredBrandsRaw())).append("\",");
        sb.append("\"priceSensitivity\":\"").append(escape(u.getPriceSensitivity())).append("\",");

        sb.append("\"preferredCategories\":[");
        List<String> cats = u.getPreferredCategories();
        for (int i = 0; i < cats.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(cats.get(i))).append("\"");
        }
        sb.append("],");

        sb.append("\"preferredBrands\":[");
        List<String> brands = u.getPreferredBrands();
        for (int i = 0; i < brands.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(brands.get(i))).append("\"");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    public static String usersToJson(List<User> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(userToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String productToJson(Product p) {
        if (p == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"productId\":").append(p.getProductId()).append(",");
        sb.append("\"name\":\"").append(escape(p.getName())).append("\",");
        sb.append("\"category\":\"").append(escape(p.getCategory())).append("\",");
        sb.append("\"brand\":\"").append(escape(p.getBrand())).append("\",");
        sb.append("\"price\":").append(p.getPrice()).append(",");
        sb.append("\"rating\":").append(p.getRating()).append(",");
        sb.append("\"reviewCount\":").append(p.getReviewCount()).append(",");
        sb.append("\"tagsRaw\":\"").append(escape(p.getTagsRaw())).append("\",");
        sb.append("\"stockQuantity\":").append(p.getStockQuantity()).append(",");
        sb.append("\"description\":\"").append(escape(p.getDescription())).append("\",");
        sb.append("\"imageUrl\":\"").append(escape(p.getImageUrl())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public static String productsToJson(List<Product> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(productToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String recommendationToJson(Recommendation r) {
        if (r == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"product\":").append(productToJson(r.getProduct())).append(",");
        sb.append("\"totalScore\":").append(r.getTotalScore()).append(",");
        sb.append("\"categoryScore\":").append(r.getCategoryScore()).append(",");
        sb.append("\"brandScore\":").append(r.getBrandScore()).append(",");
        sb.append("\"historyScore\":").append(r.getHistoryScore()).append(",");
        sb.append("\"budgetScore\":").append(r.getBudgetScore()).append(",");
        sb.append("\"ratingScore\":").append(r.getRatingScore()).append(",");
        sb.append("\"matchReasons\":[");
        List<String> reasons = r.getMatchReasons();
        for (int i = 0; i < reasons.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(reasons.get(i))).append("\"");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static String recommendationsToJson(List<Recommendation> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(recommendationToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String cartItemToJson(CartItem ci) {
        if (ci == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"cartItemId\":").append(ci.getCartItemId()).append(",");
        sb.append("\"userId\":").append(ci.getUserId()).append(",");
        sb.append("\"productId\":").append(ci.getProductId()).append(",");
        sb.append("\"quantity\":").append(ci.getQuantity()).append(",");
        sb.append("\"subtotal\":").append(ci.getSubtotal()).append(",");
        sb.append("\"product\":").append(productToJson(ci.getProduct()));
        sb.append("}");
        return sb.toString();
    }

    public static String cartItemsToJson(List<CartItem> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(cartItemToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String cartSuggestionToJson(CartSuggestion cs) {
        if (cs == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"product\":").append(productToJson(cs.getProduct())).append(",");
        sb.append("\"triggerItemName\":\"").append(escape(cs.getTriggerItemName())).append("\",");
        sb.append("\"suggestionType\":\"").append(escape(cs.getSuggestionType())).append("\",");
        sb.append("\"reason\":\"").append(escape(cs.getReason())).append("\",");
        sb.append("\"affinityScore\":").append(cs.getAffinityScore()).append(",");
        sb.append("\"remainingBudgetAfter\":").append(cs.getRemainingBudgetAfter());
        sb.append("}");
        return sb.toString();
    }

    public static String cartSuggestionsToJson(List<CartSuggestion> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(cartSuggestionToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String orderToJson(Order o) {
        if (o == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"orderId\":").append(o.getOrderId()).append(",");
        sb.append("\"userId\":").append(o.getUserId()).append(",");
        sb.append("\"totalAmount\":").append(o.getTotalAmount()).append(",");
        sb.append("\"orderDate\":\"").append(o.getOrderDate() != null ? o.getOrderDate().toString() : "").append("\",");
        sb.append("\"status\":\"").append(escape(o.getStatus())).append("\",");
        sb.append("\"items\":[");
        List<OrderItem> items = o.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            OrderItem item = items.get(i);
            sb.append("{");
            sb.append("\"orderItemId\":").append(item.getOrderItemId()).append(",");
            sb.append("\"productId\":").append(item.getProductId()).append(",");
            sb.append("\"quantity\":").append(item.getQuantity()).append(",");
            sb.append("\"unitPrice\":").append(item.getUnitPrice()).append(",");
            sb.append("\"product\":").append(productToJson(item.getProduct()));
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static String ordersToJson(List<Order> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(orderToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
