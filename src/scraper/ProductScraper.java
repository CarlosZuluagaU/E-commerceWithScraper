package scraper;

import scraper.strategies.AmazonScraper;
import scraper.strategies.EbayScraper;
import scraper.strategies.GenericScraper;
import scraper.strategies.ScraperStrategy;
import model.ProductInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProductScraper {
    private static final Map<String, ScraperStrategy> SCRAPERS;

    static {
        Map<String, ScraperStrategy> tempMap = new HashMap<>();
        tempMap.put("amazon.com", new AmazonScraper());
        tempMap.put("ebay.com", new EbayScraper());
        SCRAPERS = Collections.unmodifiableMap(tempMap);
    }

    public ProductInfo scrapeProduct(String url) throws ScraperStrategy.ScraperException {
        String domain = extractDomain(url);
        ScraperStrategy scraper = SCRAPERS.getOrDefault(domain, new GenericScraper());

        // Configurar timeout
        System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
        System.setProperty("sun.net.client.defaultReadTimeout", "5000");

        return scraper.scrape(url);
    }

    private String extractDomain(String url) {
        // Implementación básica de extracción de dominio
        url = url.replaceFirst("^(https?://)?(www.)?", "");
        int domainEnd = url.indexOf('/');
        return domainEnd == -1 ? url : url.substring(0, domainEnd);
    }
}