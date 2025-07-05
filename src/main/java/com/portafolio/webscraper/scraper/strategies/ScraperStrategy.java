package com.portafolio.webscraper.scraper.strategies;

import com.portafolio.webscraper.model.ProductInfo;

public interface ScraperStrategy {
    ProductInfo scrape(String url) throws ScraperException;
    ProductInfo scrapeFirstResultFromSearch(String productName) throws ScraperException;

    // Definición de la excepción como clase interna
    class ScraperException extends Exception {
        public ScraperException(String message) {
            super(message);
        }

        public ScraperException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}