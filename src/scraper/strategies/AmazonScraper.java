package scraper.strategies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import model.ProductInfo;
import org.jsoup.nodes.Element;
import scraper.strategies.ScraperStrategy;
import java.io.IOException;
import java.util.Map;

public class AmazonScraper implements ScraperStrategy {

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            return ProductInfo.builder()
                    .name(extractTitle(doc))
                    .currentPrice(extractPrice(doc))
                    .imageUrl(extractImage(doc))
                    .sourceUrl(url)
                    .seller(extractSeller(doc))
                    .inStock(checkAvailability(doc))
                    .build();

        } catch (IOException e) {
            throw new ScraperException("Error scraping Amazon: " + e.getMessage());
        }
    }

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("#productTitle");
        return titleElement != null ? titleElement.text().trim() : "No title found";
    }

    private double extractPrice(Document doc) {
        Element priceElement = doc.selectFirst(".a-price-whole");
        if (priceElement != null) {
            try {
                return Double.parseDouble(priceElement.text().replaceAll("[^\\d.]", ""));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing price: " + priceElement.text());
            }
        }
        return 0.0;
    }

    private String extractImage(Document doc) {
        Element imageElement = doc.selectFirst("#landingImage");
        return imageElement != null ? imageElement.attr("src") : "";
    }

    private String extractSeller(Document doc) {
        Element sellerElement = doc.selectFirst("#bylineInfo");
        return sellerElement != null ? sellerElement.text().trim() : "Amazon";
    }

    private boolean checkAvailability(Document doc) {
        Element availability = doc.selectFirst("#availability");
        return availability != null && !availability.text().contains("Currently unavailable");
    }


}