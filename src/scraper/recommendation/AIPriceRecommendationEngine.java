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
        double priceScore = calculatePriceScore(product, maxPrice);
        double ratingScore = calculateRatingScore(product);
        double popularityScore = calculatePopularityScore(product, maxReviews);

        return (PRICE_WEIGHT * priceScore)
                - (RATING_WEIGHT * ratingScore)
                - (POPULARITY_WEIGHT * popularityScore);
    }

    private double calculatePriceScore(ProductInfo product, double maxPrice) {
        return normalizePrice(product.getCurrentPrice(), maxPrice);
    }

    private double calculateRatingScore(ProductInfo product) {
        return product.getSafeRating() / 5.0; // Normalizado a escala 0-1
    }

    private double calculatePopularityScore(ProductInfo product, int maxReviews) {
        return normalizePopularity(product.getSafeReviewCount(), maxReviews);
    }

    private double normalizePrice(double price, double maxPrice) {
        return maxPrice > 0 ? price / maxPrice : 0;
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

    public void displayAnalysis(ProductInfo product, List<ProductInfo> allProducts) {
        double maxPrice = getMaxPrice(allProducts);
        int maxReviews = getMaxReviews(allProducts);

        double priceScore = calculatePriceScore(product, maxPrice);
        double ratingScore = calculateRatingScore(product);
        double popularityScore = calculatePopularityScore(product, maxReviews);

        System.out.println("\n📊 ANÁLISIS DE RECOMENDACIÓN:");
        System.out.println("----------------------------------");
        System.out.printf("• Precio: %s (Normalizado: %.2f)\n",
                product.getFormattedPrice(), priceScore);
        System.out.printf("• Rating: %.1f/5 (Normalizado: %.2f)\n",
                product.getSafeRating(), ratingScore);
        System.out.printf("• Popularidad: %d reviews (Normalizado: %.2f)\n",
                product.getSafeReviewCount(), popularityScore);
        System.out.println("----------------------------------");
        System.out.printf("PUNTUACIÓN FINAL: %.2f\n",
                (PRICE_WEIGHT * priceScore) -
                        (RATING_WEIGHT * ratingScore) -
                        (POPULARITY_WEIGHT * popularityScore));
        System.out.println("(Valores más bajos indican mejor recomendación)");
    }
}