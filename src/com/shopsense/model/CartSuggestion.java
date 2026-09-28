package com.shopsense.model;

public class CartSuggestion implements Comparable<CartSuggestion> {
    private Product product;
    private String triggerItemName;
    private String suggestionType; // 'COMPLEMENT', 'BUDGET_SAVER', 'THRESHOLD_BOOSTER'
    private String reason;
    private double affinityScore;
    private double remainingBudgetAfter;

    public CartSuggestion() {}

    public CartSuggestion(Product product, String triggerItemName, String suggestionType,
                          String reason, double affinityScore, double remainingBudgetAfter) {
        this.product = product;
        this.triggerItemName = triggerItemName;
        this.suggestionType = suggestionType;
        this.reason = reason;
        this.affinityScore = affinityScore;
        this.remainingBudgetAfter = remainingBudgetAfter;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getTriggerItemName() { return triggerItemName; }
    public void setTriggerItemName(String triggerItemName) { this.triggerItemName = triggerItemName; }

    public String getSuggestionType() { return suggestionType; }
    public void setSuggestionType(String suggestionType) { this.suggestionType = suggestionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public double getAffinityScore() { return affinityScore; }
    public void setAffinityScore(double affinityScore) { this.affinityScore = affinityScore; }

    public double getRemainingBudgetAfter() { return remainingBudgetAfter; }
    public void setRemainingBudgetAfter(double remainingBudgetAfter) { this.remainingBudgetAfter = remainingBudgetAfter; }

    @Override
    public int compareTo(CartSuggestion other) {
        return Double.compare(other.affinityScore, this.affinityScore);
    }
}
