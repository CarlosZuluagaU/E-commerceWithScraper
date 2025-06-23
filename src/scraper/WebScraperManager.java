package scraper;

import scraper.strategies.AmazonScraper;
import scraper.strategies.ScraperStrategy;
import model.ProductInfo;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebScraperManager {

    public static void main(String[] args) {
        List<String> productUrls = List.of(
                "https://www.amazon.com/dp/B08N5KWB9H",
                "https://www.amazon.com/dp/B07FK8SQDQ"
        );

        // Usamos ProductScraper que maneja las estrategias
        ProductScraper productScraper = new ProductScraper();

        long startTime = System.nanoTime();

        List<ProductInfo> products = productUrls.parallelStream()
                .map(url -> {
                    try {
                        return productScraper.scrapeProduct(url);
                    } catch (ScraperStrategy.ScraperException e) {
                        System.err.println("Error scraping " + url + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        // Mostrar resultados
        products.forEach(product -> {
            System.out.println("\n=== Producto ===");
            System.out.println("Nombre: " + product.getName());
            System.out.println("Precio: $" + product.getCurrentPrice());
            System.out.println("Vendedor: " + product.getSeller());
            System.out.println("En stock: " + (product.isInStock() ? "Sí" : "No"));
            System.out.println("URL: " + product.getSourceUrl());
        });

        System.out.println("\nTiempo total de scraping: " + duration + "ms");
        System.out.println("Productos obtenidos: " + products.size());
    }
}