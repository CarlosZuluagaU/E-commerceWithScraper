package scraper.strategies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import model.ProductInfo;
import scraper.strategies.ScraperStrategy;
import java.io.IOException;

public class WalmartScraper implements ScraperStrategy {

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
                    .seller("Walmart")
                    .inStock(checkAvailability(doc))
                    .rating(extractRating(doc))
                    .reviewCount(extractReviewCount(doc))
                    .build();

        } catch (IOException e) {
            throw new ScraperException("Error scraping Walmart: " + e.getMessage(), e);
        }
    }

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("h1.prod-ProductTitle");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private double extractPrice(Document doc) {
        Element priceElement = doc.selectFirst("span.price-characteristic");
        if (priceElement != null) {
            try {
                String priceText = priceElement.attr("content");
                return Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing Walmart price: " + priceElement.text());
            }
        }
        return 0.0;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst("img.slider-list-img");
        return imageElement != null ? imageElement.absUrl("src") : "";
    }

    private boolean checkAvailability(Document doc) {
        Element availability = doc.selectFirst("button.prod-ProductCTA--primary");
        return availability != null && availability.text().contains("Add to cart");
    }

    private Double extractRating(Document doc) {
        Element ratingElement = doc.selectFirst("span.stars-container");
        if (ratingElement != null) {
            String ratingText = ratingElement.attr("aria-label");
            try {
                return Double.parseDouble(ratingText.split(" ")[0]);
            } catch (Exception e) {
                System.err.println("Error extracting Walmart rating: " + e.getMessage());
            }
        }
        return null;
    }

    private Integer extractReviewCount(Document doc) {
        Element reviewElement = doc.selectFirst("span.stars-reviews-count");
        if (reviewElement != null) {
            try {
                String reviewText = reviewElement.text().replaceAll("[^0-9]", "");
                return Integer.parseInt(reviewText);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing Walmart review count: " + reviewElement.text());
            }
        }
        return null;
    }
}