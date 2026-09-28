package com.shopsense.model;

import java.sql.Timestamp;

public class CartItem {
    private int cartItemId;
    private int userId;
    private int productId;
    private int quantity;
    private Timestamp addedAt;
    private Product product;

    public CartItem() {}

    public CartItem(int cartItemId, int userId, int productId, int quantity, Timestamp addedAt) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public double getSubtotal() {
        if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0.0;
    }
}
