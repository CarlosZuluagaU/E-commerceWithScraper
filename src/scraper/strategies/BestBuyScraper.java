package scraper.strategies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import model.ProductInfo;
import scraper.strategies.ScraperStrategy;
import java.io.IOException;

public class BestBuyScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();

            return ProductInfo.builder()
                    .name(extractTitle(doc))
                    .currentPrice(extractPrice(doc))
                    .imageUrl(extractImage(doc))
                    .sourceUrl(url)
                    .seller("Best Buy")
                    .inStock(checkAvailability(doc))
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .build();

        } catch (IOException e) {
            throw new ScraperException("Error scraping Best Buy: " + e.getMessage(), e);
        }
    }

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("h1.heading-5");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private double extractPrice(Document doc) {
        Element priceElement = doc.selectFirst("div.priceView-customer-price > span");
        if (priceElement != null) {
            try {
                String priceText = priceElement.text().replaceAll("[^0-9.]", "");
                return Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing Best Buy price: " + priceElement.text());
            }
        }
        return 0.0;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst("img.primary-image");
        return imageElement != null ? imageElement.absUrl("src") : "";
    }

    private boolean checkAvailability(Document doc) {
        Element availability = doc.selectFirst("button.add-to-cart-button");
        return availability != null && !availability.text().contains("Sold Out");
    }

    private Double extractRating(Document doc) {
        Element ratingElement = doc.selectFirst("span.c-reviews-v4 span.c-review-average");
        if (ratingElement != null) {
            try {
                return Double.parseDouble(ratingElement.text());
            } catch (NumberFormatException e) {
                System.err.println("Error extracting Best Buy rating: " + ratingElement.text());
            }
        }
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        Element reviewElement = doc.selectFirst("span.c-reviews-v4 span.c-total-reviews");
        if (reviewElement != null) {
            try {
                String reviewText = reviewElement.text().replaceAll("[^0-9]", "");
                return Integer.parseInt(reviewText);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing Best Buy review count: " + reviewElement.text());
            }
        }
        return null;
    }
}