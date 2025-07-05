package com.portafolio.webscraper.scraper.strategies;

import com.portafolio.webscraper.model.ProductInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrapeFirstResultFromSearch(String productName) throws ScraperException {
        throw new ScraperException("La búsqueda por nombre no es compatible con el scraper genérico. Se requiere una URL de producto directa.");
    }

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .referrer("https://www.google.com")
                    .timeout(15000)
                    .get();

            // --- CORRECCIONES EN EL BUILDER ---
            return ProductInfo.builder()
                    .name(extractMeta(doc, "og:title", "title"))
                    .currentPrice(extractPrice(doc))
                    .imageUrl(extractMeta(doc, "og:image"))
                    .productUrl(url)                          // ANTES: sourceUrl
                    .storeName(extractStoreNameFromUrl(url))  // ANTES: seller
                    .available(checkAvailability(doc))        // ANTES: inStock
                    .lastUpdated(LocalDateTime.now())         // ANTES: scrapedAt
                    .build();

        } catch (IOException e) {
            throw new ScraperException("Error en scraping genérico para la URL: " + url, e);
        }
    }

    private String extractMeta(Document doc, String... selectors) {
        for (String selector : selectors) {
            Element meta = doc.selectFirst("meta[property='" + selector + "']");
            if (meta != null && meta.hasAttr("content")) {
                return meta.attr("content").trim();
            }
            Element element = doc.selectFirst(selector);
            if (element != null) {
                return element.text().trim();
            }
        }
        return null;
    }

    private Double extractPrice(Document doc) {
        String[] priceSelectors = {
                "meta[property='og:price:amount']",
                "meta[property='product:price:amount']",
                "[itemprop=price]",
                ".price",
        };

        for (String selector : priceSelectors) {
            Element priceElement = doc.selectFirst(selector);
            if (priceElement != null) {
                try {
                    String priceText = priceElement.tagName().equals("meta")
                            ? priceElement.attr("content")
                            : priceElement.text();
                    priceText = priceText.replaceAll("[^\\d.,]", "").replace(',', '.').trim();
                    if (!priceText.isEmpty()) {
                        return Double.parseDouble(priceText);
                    }
                } catch (NumberFormatException e) {
                    // Ignorar y probar el siguiente
                }
            }
        }
        return null;
    }

    private boolean checkAvailability(Document doc) {
        String bodyText = doc.body().text().toLowerCase();
        String[] unavailableKeywords = {"out of stock", "unavailable", "agotado", "no disponible"};
        for (String keyword : unavailableKeywords) {
            if (bodyText.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private String extractStoreNameFromUrl(String url) {
        try {
            Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?([^./]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String domain = matcher.group(1);
                return domain.substring(0, 1).toUpperCase() + domain.substring(1);
            }
        } catch (Exception e) {
            // Ignorar error
        }
        return "Unknown Store";
    }
}