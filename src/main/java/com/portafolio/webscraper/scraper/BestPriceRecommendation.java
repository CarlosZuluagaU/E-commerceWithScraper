package com.portafolio.webscraper.scraper;

import com.portafolio.webscraper.model.ProductInfo;

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