package com.portafolio.webscraper.scraper.strategies; // Asegúrate de que el paquete sea correcto

import com.portafolio.webscraper.model.ProductInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class AmazonScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrapeFirstResultFromSearch(String productName) throws ScraperException {
        try {
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String searchUrl = "https://www.amazon.com/s?k=" + encodedName;

            Document searchPage = createHumanConnection(searchUrl).get();

            Elements results = searchPage.select("div[data-component-type='s-search-result']");
            Element firstResultLink = null;

            for (Element result : results) {
                if (result.select("span:contains(Sponsored)").isEmpty()) {
                    firstResultLink = result.selectFirst("h2 a.a-link-normal");
                    if (firstResultLink != null) break;
                }
            }

            if (firstResultLink == null) {
                throw new ScraperException("No se encontraron resultados de búsqueda para '" + productName + "' en Amazon.");
            }

            String productUrl = firstResultLink.absUrl("href");
            return this.scrape(productUrl);

        } catch (IOException e) {
            throw new ScraperException("Error durante la búsqueda en Amazon para: " + productName, e);
        }
    }

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = createHumanConnection(url).get();

            // --- CORRECCIONES EN EL BUILDER ---
            return ProductInfo.builder()
                    .name(extractTitle(doc))
                    .currentPrice(extractPrice(doc))
                    .imageUrl(extractImage(doc))
                    .productUrl(url)                  // ANTES: sourceUrl
                    .storeName("Amazon")              // ANTES: seller
                    .available(checkAvailability(doc)) // ANTES: inStock
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .lastUpdated(LocalDateTime.now()) // ANTES: scrapedAt
                    .build();

        } catch (Exception e) {
            throw new ScraperException("Error al scrapear la URL de producto de Amazon: " + url, e);
        }
    }

    private Connection createHumanConnection(String url) {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .referrer("https://www.google.com")
                .timeout(15000);
    }

    // --- MÉTODOS DE EXTRACCIÓN (sin cambios) ---
    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("#productTitle");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private Double extractPrice(Document doc) {
        // Lógica para encontrar el precio
        Element priceElement = doc.selectFirst("span.a-price span.a-offscreen");
        if (priceElement != null) {
            try {
                String priceText = priceElement.text().replaceAll("[^\\d.,]", "").replace(',', '.');
                if (!priceText.isEmpty()) {
                    return Double.parseDouble(priceText);
                }
            } catch (NumberFormatException e) {
                // No hacer nada, probar el siguiente selector
            }
        }
        return null;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst("#landingImage");
        return imageElement != null ? imageElement.absUrl("src") : "";
    }

    private boolean checkAvailability(Document doc) {
        Element availability = doc.selectFirst("#availability");
        if (availability != null && availability.text().toLowerCase().contains("unavailable")) {
            return false;
        }
        return doc.selectFirst("#add-to-cart-button") != null;
    }

    private Double extractRating(Document doc) {
        try {
            Element ratingElement = doc.selectFirst("#acrPopover");
            if (ratingElement != null) {
                String ratingText = ratingElement.attr("title").split(" ")[0].replace(',', '.').trim();
                return Double.parseDouble(ratingText);
            }
        } catch (Exception e) { /* Ignorar error */ }
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        try {
            Element reviewElement = doc.selectFirst("#acrCustomerReviewText");
            if (reviewElement != null) {
                String reviewText = reviewElement.text().replaceAll("[^0-9]", "");
                return Integer.parseInt(reviewText);
            }
        } catch (Exception e) { /* Ignorar error */ }
        return null;
    }
}