package com.shopsense.model;

import java.util.ArrayList;
import java.util.List;

public class Recommendation implements Comparable<Recommendation> {
    private Product product;
    private double totalScore;
    private double categoryScore;
    private double brandScore;
    private double historyScore;
    private double budgetScore;
    private double ratingScore;
    private List<String> matchReasons = new ArrayList<>();

    public Recommendation() {}

    public Recommendation(Product product, double totalScore) {
        this.product = product;
        this.totalScore = totalScore;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public double getCategoryScore() { return categoryScore; }
    public void setCategoryScore(double categoryScore) { this.categoryScore = categoryScore; }

    public double getBrandScore() { return brandScore; }
    public void setBrandScore(double brandScore) { this.brandScore = brandScore; }

    public double getHistoryScore() { return historyScore; }
    public void setHistoryScore(double historyScore) { this.historyScore = historyScore; }

    public double getBudgetScore() { return budgetScore; }
    public void setBudgetScore(double budgetScore) { this.budgetScore = budgetScore; }

    public double getRatingScore() { return ratingScore; }
    public void setRatingScore(double ratingScore) { this.ratingScore = ratingScore; }

    public List<String> getMatchReasons() { return matchReasons; }
    public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }

    public void addMatchReason(String reason) {
        if (this.matchReasons == null) this.matchReasons = new ArrayList<>();
        this.matchReasons.add(reason);
    }

    @Override
    public int compareTo(Recommendation other) {
        return Double.compare(other.totalScore, this.totalScore); // Highest score first
    }
}
