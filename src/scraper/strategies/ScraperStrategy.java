package scraper.strategies;

import model.ProductInfo;

public interface ScraperStrategy {
    ProductInfo scrape(String url) throws ScraperException;

    class ScraperException extends Exception {
        public ScraperException(String message) {
            super(message);
        }

        // 🔧 Este constructor adicional te permite incluir una "causa"
        public ScraperException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
