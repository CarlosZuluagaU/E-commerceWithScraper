package com.portafolio.webscraper.scraper.strategies;

import com.portafolio.webscraper.model.ProductInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class EbayScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrapeFirstResultFromSearch(String productName) throws ScraperException {
        try {
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String searchUrl = "https://www.ebay.com/sch/i.html?_nkw=" + encodedName;

            Document searchPage = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000)
                    .get();

            Element firstResultLink = searchPage.selectFirst("li.s-item .s-item__link");

            if (firstResultLink == null) {
                throw new ScraperException("No se encontraron resultados de búsqueda para '" + productName + "' en eBay.");
            }

            String productUrl = firstResultLink.absUrl("href");
            return this.scrape(productUrl);

        } catch (IOException e) {
            throw new ScraperException("Error durante la búsqueda en eBay para: " + productName, e);
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
                    .storeName("eBay")                    // ANTES: seller(extractSeller(doc))
                    .available(checkAvailability(doc))    // ANTES: inStock
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .lastUpdated(LocalDateTime.now())     // ANTES: scrapedAt
                    .build();

        } catch (Exception e) {
            throw new ScraperException("Error al scrapear la URL de producto de eBay: " + url, e);
        }
    }

    // --- MÉTODOS DE EXTRACCIÓN ---

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst(".x-item-title__mainTitle .ux-textspans--BOLD");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private Double extractPrice(Document doc) {
        Element priceElement = doc.selectFirst(".x-price-primary span.ux-textspans");
        if (priceElement != null) {
            try {
                String priceText = priceElement.text().replaceAll("[^\\d.]", "");
                if (!priceText.isEmpty()) {
                    return Double.parseDouble(priceText);
                }
            } catch (NumberFormatException e) {
                // Ignorar error y retornar null
            }
        }
        return null;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst(".ux-image-carousel-item.active img");
        return imageElement != null ? imageElement.attr("src") : null;
    }

    private boolean checkAvailability(Document doc) {
        Element endedElement = doc.selectFirst(".d-quantity__availability span.ux-textspans--BOLD");
        if (endedElement != null) {
            String availabilityText = endedElement.text().toLowerCase();
            if (availabilityText.contains("ended") || availabilityText.contains("no longer available")) {
                return false;
            }
        }
        // Si hay un botón de compra, consideramos que está disponible
        return doc.selectFirst("#binBtn_btn, #isCartBtn_btn") != null;
    }

    private Double extractRating(Document doc) {
        // En eBay, el rating principal es del vendedor, no del producto.
        // Por simplicidad, este scraper no extrae el rating.
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        Element reviewElement = doc.selectFirst("a.ux-anchor[href*='#Reviews'] span.ux-textspans--PSEUDOLINK");
        if (reviewElement != null) {
            try {
                String reviewText = reviewElement.text().replaceAll("[^0-9]", "");
                if (!reviewText.isEmpty()) {
                    return Integer.parseInt(reviewText);
                }
            } catch (NumberFormatException e) {
                // Ignorar error
            }
        }
        return null;
    }
}