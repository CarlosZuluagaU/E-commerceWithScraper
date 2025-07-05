package com.portafolio.webscraper.scraper.recommendation;

import com.portafolio.webscraper.model.ProductInfo;
import com.portafolio.webscraper.scraper.RecommendationEngine;
import java.util.List;
import java.util.Comparator;

public class BalancedScoreRecommendation implements RecommendationEngine {

    private static final double PRICE_WEIGHT = 0.6;
    private static final double RATING_WEIGHT = 0.3;
    private static final double POPULARITY_WEIGHT = 0.1;

    @Override
    public ProductInfo recommendBestProduct(List<ProductInfo> products) {
        // Precalculamos los valores máximos una sola vez
        final double maxPrice = getMaxPrice(products);
        final int maxReviews = getMaxReviews(products);

        return products.stream()
                .min(Comparator.comparingDouble(product -> calculateScore(product, maxPrice, maxReviews)))
                .orElseThrow(() -> new IllegalArgumentException("Empty product list"));
    }

    private double calculateScore(ProductInfo product, double maxPrice, int maxReviews) {
        double priceScore = normalize(product.getCurrentPrice(), maxPrice) * PRICE_WEIGHT;
        double ratingScore = (1 - normalize(product.getSafeRating(), 5.0)) * RATING_WEIGHT;
        double popularityScore = (1 - normalize(product.getSafeReviewCount(), maxReviews)) * POPULARITY_WEIGHT;

        return priceScore + ratingScore + popularityScore;
    }

    private double normalize(double value, double max) {
        return max > 0 ? value / max : 0;
    }

    private double getMaxPrice(List<ProductInfo> products) {
        return products.stream()
                .mapToDouble(ProductInfo::getCurrentPrice)
                .max().orElse(0);
    }

    private int getMaxReviews(List<ProductInfo> products) {
        return products.stream()
                .mapToInt(ProductInfo::getSafeReviewCount)
                .max().orElse(0);
    }
}