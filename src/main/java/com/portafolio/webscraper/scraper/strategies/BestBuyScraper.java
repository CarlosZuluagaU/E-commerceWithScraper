package com.portafolio.webscraper.scraper.strategies; // Asegúrate de que el paquete sea correcto

import com.portafolio.webscraper.model.ProductInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class BestBuyScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrapeFirstResultFromSearch(String productName) throws ScraperException {
        try {
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String searchUrl = "https://www.bestbuy.com/site/searchpage.jsp?st=" + encodedName;

            Document searchPage = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .get();

            Element firstResultLink = searchPage.selectFirst("li.sku-item h4.sku-title a");

            if (firstResultLink == null) {
                throw new ScraperException("No se encontraron resultados de búsqueda para '" + productName + "' en Best Buy.");
            }

            String productUrl = firstResultLink.absUrl("href");
            return this.scrape(productUrl);

        } catch (IOException e) {
            throw new ScraperException("Error durante la búsqueda en Best Buy para: " + productName, e);
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
                    .productUrl(url)                  // ANTES: sourceUrl
                    .storeName("Best Buy")              // ANTES: seller
                    .available(checkAvailability(doc)) // ANTES: inStock
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .lastUpdated(LocalDateTime.now()) // ANTES: scrapedAt
                    .build();

        } catch (Exception e) {
            throw new ScraperException("Error al scrapear la URL de producto de Best Buy: " + url, e);
        }
    }

    // --- MÉTODOS DE EXTRACCIÓN (Corregidos para mayor robustez) ---

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("h1.heading-5.v-fw-regular");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private Double extractPrice(Document doc) {
        Element priceElement = doc.selectFirst(".priceView-hero-price.priceView-customer-price span[aria-hidden=true]");
        if (priceElement != null) {
            try {
                String priceText = priceElement.text().replaceAll("[^\\d.]", "");
                if (!priceText.isEmpty()) {
                    return Double.parseDouble(priceText);
                }
            } catch (NumberFormatException e) {
                // Ignorar error y retornar null si el parseo falla
            }
        }
        return null; // Devolver null si no se encuentra el precio
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst(".primary-image.is-visible");
        return imageElement != null ? imageElement.attr("src") : null;
    }

    private boolean checkAvailability(Document doc) {
        Element availabilityButton = doc.selectFirst("button.add-to-cart-button");
        if (availabilityButton != null) {
            String buttonText = availabilityButton.text().toLowerCase();
            return !buttonText.contains("sold out") && !buttonText.contains("unavailable");
        }
        return false;
    }

    private Double extractRating(Document doc) {
        Element ratingElement = doc.selectFirst(".ugc-c-review-average");
        if (ratingElement != null) {
            try {
                return Double.parseDouble(ratingElement.text().trim());
            } catch (NumberFormatException e) {
                // Ignorar error
            }
        }
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        Element reviewElement = doc.selectFirst("a.c-reviews-v4 .c-reviews-v4-count");
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