package scraper.strategies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import model.ProductInfo;
import org.jsoup.nodes.Element;
import scraper.strategies.ScraperStrategy;
import java.io.IOException;
import java.time.LocalDateTime;

public class AmazonScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .referrer("https://www.google.com")
                    .timeout(10000)
                    .get();

            return ProductInfo.builder()
                    .name(extractTitle(doc))
                    .currentPrice(extractPrice(doc))
                    .imageUrl(extractImage(doc))
                    .sourceUrl(url)
                    .seller(extractSeller(doc))
                    .inStock(checkAvailability(doc))
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .scrapedAt(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            throw new ScraperException("Error scraping Amazon: " + e.getMessage(), e);
        }
    }

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("#productTitle");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private double extractPrice(Document doc) {
        // Intenta primero con el precio normal
        Element priceElement = doc.selectFirst(".a-price-whole");

        // Si no encuentra, busca en otras ubicaciones comunes
        if (priceElement == null) {
            priceElement = doc.selectFirst(".priceToPay span.a-price-whole");
        }
        if (priceElement == null) {
            priceElement = doc.selectFirst("span.aok-offscreen");
        }

        if (priceElement != null) {
            try {
                String priceText = priceElement.text()
                        .replaceAll("[^\\d.]", "");
                return Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing price: " + priceElement.text());
            }
        }
        return 0.0;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst("#landingImage, #imgBlkFront");
        return imageElement != null ? imageElement.absUrl("src") : "";
    }

    private String extractSeller(Document doc) {
        // Busca en diferentes ubicaciones posibles
        Element sellerElement = doc.selectFirst("#bylineInfo, #sellerProfileTriggerId, #merchantInfo");
        if (sellerElement != null) {
            String sellerText = sellerElement.text()
                    .replaceAll("Visit the | Store|Brand: ", "")
                    .trim();
            return sellerText.isEmpty() ? "Amazon" : sellerText;
        }
        return "Amazon";
    }

    private boolean checkAvailability(Document doc) {
        Element availability = doc.selectFirst("#availability, #outOfStock");
        if (availability != null) {
            String availabilityText = availability.text().toLowerCase();
            return !(availabilityText.contains("currently unavailable") ||
                    availabilityText.contains("out of stock"));
        }
        return true;
    }

    private Double extractRating(Document doc) {
        try {
            Element ratingElement = doc.selectFirst("#acrPopover, .reviewCountTextLinkedHistogram");
            if (ratingElement != null) {
                String ratingText = ratingElement.attr("title")
                        .replaceAll("[^0-9.]", "");
                if (!ratingText.isEmpty()) {
                    return Double.parseDouble(ratingText);
                }
            }

            // Alternativa para algunos formatos
            ratingElement = doc.selectFirst("i.a-icon-star span");
            if (ratingElement != null) {
                String ratingText = ratingElement.text()
                        .split(" ")[0]
                        .trim();
                return Double.parseDouble(ratingText);
            }
        } catch (Exception e) {
            System.err.println("Error extracting rating: " + e.getMessage());
        }
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        try {
            Element reviewElement = doc.selectFirst("#acrCustomerReviewText, #reviewsMedley");
            if (reviewElement != null) {
                String reviewText = reviewElement.text()
                        .replaceAll("[^0-9]", "");
                if (!reviewText.isEmpty()) {
                    return Integer.parseInt(reviewText);
                }
            }

            // Alternativa para algunos formatos
            reviewElement = doc.selectFirst("a[href*=reviews] span");
            if (reviewElement != null) {
                String reviewText = reviewElement.text()
                        .replaceAll("[^0-9]", "");
                return Integer.parseInt(reviewText);
            }
        } catch (Exception e) {
            System.err.println("Error extracting review count: " + e.getMessage());
        }
        return null;
    }
}