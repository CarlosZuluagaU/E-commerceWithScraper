package scraper.recommendation;

import model.ProductInfo;
import scraper.RecommendationEngine;
import java.util.List;
import java.util.Comparator;

public class AIPriceRecommendationEngine implements RecommendationEngine {

    private static final double PRICE_WEIGHT = 0.6;
    private static final double RATING_WEIGHT = 0.3;
    private static final double POPULARITY_WEIGHT = 0.1;

    @Override
    public ProductInfo recommendBestProduct(List<ProductInfo> products) {
        // Precalculamos los valores máximos
        final double maxPrice = getMaxPrice(products);
        final int maxReviews = getMaxReviews(products);

        return products.stream()
                .min(Comparator.comparingDouble(product ->
                        calculateProductScore(product, maxPrice, maxReviews)))
                .orElseThrow(() -> new RuntimeException("No products to recommend"));
    }

    private double calculateProductScore(ProductInfo product, double maxPrice, int maxReviews) {
        // Normalizamos los valores
        double normalizedPrice = normalizePrice(product.getCurrentPrice(), maxPrice);
        double normalizedRating = product.getSafeRating() / 5.0; // Rating 0-5
        double normalizedPopularity = normalizePopularity(product.getSafeReviewCount(), maxReviews);

        return (PRICE_WEIGHT * normalizedPrice)
                - (RATING_WEIGHT * normalizedRating)
                - (POPULARITY_WEIGHT * normalizedPopularity);
    }

    private double normalizePrice(double price, double maxPrice) {
        return price / maxPrice;
    }

    private double normalizePopularity(int reviewCount, int maxReviews) {
        return maxReviews > 0 ? (double) reviewCount / maxReviews : 0;
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