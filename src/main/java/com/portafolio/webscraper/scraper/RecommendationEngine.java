package com.portafolio.webscraper.scraper;

import com.portafolio.webscraper.model.ProductInfo;
import java.util.List;

public interface RecommendationEngine {
    ProductInfo recommendBestProduct(List<ProductInfo> products);
}

