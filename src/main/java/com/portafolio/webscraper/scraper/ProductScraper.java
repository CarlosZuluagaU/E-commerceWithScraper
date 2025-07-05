package com.portafolio.webscraper.scraper;

import com.portafolio.webscraper.model.ProductInfo;
import com.portafolio.webscraper.scraper.strategies.*;

import java.util.Collections;
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

    public ProductInfo scrapeFromSearch(String productName, String storeDomain)
            throws ScraperStrategy.ScraperException {
        ScraperStrategy scraper = SCRAPERS.get(storeDomain);

        if (scraper == null) {
            throw new ScraperStrategy.ScraperException(
                    "No se encontró una estrategia de scraping para el dominio: " + storeDomain);
        }

        return scraper.scrapeFirstResultFromSearch(productName);
    }

    private String extractDomain(String url) {
        url = url.replaceFirst("^(https?://)?(www\\.)?", "");
        int domainEnd = url.indexOf('/');
        return domainEnd == -1 ? url : url.substring(0, domainEnd);
    }

    public static Map<String, ScraperStrategy> getScrapers() {
        return Collections.unmodifiableMap(SCRAPERS);
    }
}