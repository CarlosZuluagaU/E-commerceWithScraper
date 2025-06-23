package scraper;

import model.ProductInfo;
import java.util.List;

public interface RecommendationEngine {
    ProductInfo recommendBestProduct(List<ProductInfo> products);
}

