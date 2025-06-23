package scraper.recommendation;

import model.ProductInfo;
import scraper.RecommendationEngine;
import java.util.List;
import java.util.Comparator;

public class BestPriceRecommendation implements RecommendationEngine {
    @Override
    public ProductInfo recommendBestProduct(List<ProductInfo> products) {
        return products.stream()
                .min(Comparator.comparingDouble(ProductInfo::getCurrentPrice))
                .orElseThrow(() -> new IllegalArgumentException("Empty product list"));
    }
}