package scraper;

import scraper.strategies.*;
import model.ProductInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProductScraper {
    private static final Map<String, ScraperStrategy> SCRAPERS = Map.of(
            "amazon.com", new AmazonScraper(),
            "ebay.com", new EbayScraper(),
            "walmart.com", new WalmartScraper(),
            "bestbuy.com", new BestBuyScraper()
    );

    public ProductInfo scrapeProduct(String url) throws ScraperStrategy.ScraperException {
        String domain = extractDomain(url);
        ScraperStrategy scraper = SCRAPERS.getOrDefault(domain, new GenericScraper());
        return scraper.scrape(url);
    }


    private String extractDomain(String url) {
        // Implementación básica de extracción de dominio
        url = url.replaceFirst("^(https?://)?(www.)?", "");
        int domainEnd = url.indexOf('/');
        return domainEnd == -1 ? url : url.substring(0, domainEnd);
    }
}