package com.portafolio.webscraper.scraper.strategies;

// La excepción ahora es una clase pública en su propio archivo.
public class ScraperException extends Exception {

    public ScraperException(String message) {
        super(message);
    }

    public ScraperException(String message, Throwable cause) {
        super(message, cause);
    }
}