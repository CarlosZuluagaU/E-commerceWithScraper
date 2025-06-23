package scraper;

import java.net.URI;
import java.net.URISyntaxException;

public class ScraperUtils {
    public static String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL inválida: " + url);
        }
    }

    public static String cleanPriceString(String price) {
        return price.replaceAll("[^0-9.,]", "").replace(",", ".");
    }
}