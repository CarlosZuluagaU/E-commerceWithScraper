package com.portafolio.webscraper.scraper.strategies;

import com.portafolio.webscraper.model.ProductInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class WalmartScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrapeFirstResultFromSearch(String productName) throws ScraperException {
        try {
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String searchUrl = "https://www.walmart.com/search?q=" + encodedName;

            Document searchPage = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .get();

            Element firstResultLink = searchPage.selectFirst("a[data-testid=product-title-link]");

            if (firstResultLink == null) {
                firstResultLink = searchPage.selectFirst("div[data-item-id] a");
            }

            if (firstResultLink == null) {
                throw new ScraperException("No se encontraron resultados de búsqueda para '" + productName + "' en Walmart.");
            }

            String productUrl = firstResultLink.absUrl("href");
            return this.scrape(productUrl);

        } catch (IOException e) {
            throw new ScraperException("Error durante la búsqueda en Walmart para: " + productName, e);
        }
    }

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .get();

            // --- CORRECCIONES EN EL BUILDER ---
            return ProductInfo.builder()
                    .name(extractTitle(doc))
                    .currentPrice(extractPrice(doc))
                    .imageUrl(extractImage(doc))
                    .productUrl(url)                      // ANTES: sourceUrl
                    .storeName("Walmart")                 // ANTES: seller
                    .available(checkAvailability(doc))    // ANTES: inStock
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .lastUpdated(LocalDateTime.now())     // ANTES: scrapedAt
                    .build();

        } catch (Exception e) {
            throw new ScraperException("Error al scrapear la URL de producto de Walmart: " + url, e);
        }
    }

    // --- MÉTODOS DE EXTRACCIÓN ---

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("h1[itemprop=name]");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private Double extractPrice(Document doc) {
        Element priceElement = doc.selectFirst("div[data-testid=product-buy-box] span[itemprop=price]");
        if (priceElement != null) {
            try {
                String priceText = priceElement.attr("content");
                if (!priceText.isEmpty()) {
                    return Double.parseDouble(priceText);
                }
            } catch (NumberFormatException e) {
                // Ignorar error
            }
        }
        return null;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst("div[data-testid=media-thumbnail-container] img");
        return imageElement != null ? imageElement.attr("src") : null;
    }

    private boolean checkAvailability(Document doc) {
        if (doc.selectFirst("[data-testid=outOfStock-PUP]") != null) {
            return false;
        }
        return doc.selectFirst("button[data-testid=add-to-cart-section-button]") != null;
    }

    private Double extractRating(Document doc) {
        Element ratingElement = doc.selectFirst("span.f7.mr1.b.black");
        if (ratingElement != null) {
            try {
                return Double.parseDouble(ratingElement.text().trim());
            } catch (Exception e) {
                // Ignorar error
            }
        }
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        Element reviewElement = doc.selectFirst("a[href='#reviews']");
        if (reviewElement != null) {
            try {
                String reviewText = reviewElement.text().replaceAll("[^0-9]", "");
                return Integer.parseInt(reviewText);
            } catch (NumberFormatException e) {
                // Ignorar error
            }
        }
        return null;
    }
}