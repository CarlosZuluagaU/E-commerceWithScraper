package com.portafolio.webscraper.scraper; // Asegúrate de que el paquete sea correcto

import com.portafolio.webscraper.model.ProductInfo;
import com.portafolio.webscraper.scraper.recommendation.AIPriceRecommendationEngine;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SmartProductFinder {
    private final ProductScraper productScraper;
    private final RecommendationEngine recommendationEngine;

    public SmartProductFinder() {
        this.productScraper = new ProductScraper();
        this.recommendationEngine = new AIPriceRecommendationEngine();
    }

    public void startInteractiveSearch() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("🔍 Smart Product Finder - IA Powered");

        while (true) {
            System.out.print("\nIngrese el producto a buscar (o 'salir' para terminar): ");
            String query = scanner.nextLine();

            if (query.equalsIgnoreCase("salir")) break;

            searchAndRecommendProducts(query);
        }
        scanner.close();
    }

    private void searchAndRecommendProducts(String query) {
        System.out.println("\n🛒 Buscando '" + query + "' en múltiples tiendas...");

        List<String> searchUrls = generateSearchUrls(query);
        List<ProductInfo> products = searchUrls.parallelStream()
                .map(url -> {
                    try {
                        return productScraper.scrapeProduct(url);
                    } catch (Exception e) {
                        System.err.println("Error buscando: " + url);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            System.out.println("No se encontraron productos");
            return;
        }

        ProductInfo bestOption = recommendationEngine.recommendBestProduct(products);
        displayRecommendation(bestOption, products);
    }

    private List<String> generateSearchUrls(String query) {
        // Asegurarse de que el query está codificado para URL
        String encodedQuery = query.replace(" ", "+");
        return List.of(
                "https://www.amazon.com/s?k=" + encodedQuery,
                "https://www.ebay.com/sch/i.html?_nkw=" + encodedQuery,
                "https://www.walmart.com/search?q=" + encodedQuery
        );
    }

    private void displayRecommendation(ProductInfo best, List<ProductInfo> all) {
        if (best == null) {
            System.out.println("No se pudo determinar una recomendación.");
            return;
        }
        System.out.println("\n✨ Recomendación basada en IA:");
        System.out.println("---------------------------------");
        System.out.println("MEJOR OPCIÓN: " + best.getName());
        System.out.println("PRECIO: " + best.getFormattedPrice()); // Usando el método formateado
        // CORRECCIÓN: Usar getStoreName() en lugar de getSeller()
        System.out.println("VENDEDOR: " + best.getStoreName());
        // CORRECCIÓN: Usar getProductUrl() en lugar de getSourceUrl()
        System.out.println("ENLACE: " + best.getProductUrl());

        System.out.println("\n🔍 Todas las opciones encontradas:");
        all.stream()
                .sorted(Comparator.comparingDouble(ProductInfo::getCurrentPrice))
                .forEach(p -> System.out.printf(
                        "• %s - %s (%s)\n",
                        p.getName(),
                        p.getFormattedPrice(),
                        // CORRECCIÓN: Usar getProductUrl() en lugar de getSourceUrl()
                        p.getProductUrl().split("/")[2])); // Extrae el dominio
    }

    // Nota: Esta clase Main es para ejecutar este archivo de forma independiente.
    // Tu aplicación Spring Boot se ejecuta desde WebScraperApplication.java
    public static class Main {
        public static void main(String[] args) {
            new SmartProductFinder().startInteractiveSearch();
        }
    }
}