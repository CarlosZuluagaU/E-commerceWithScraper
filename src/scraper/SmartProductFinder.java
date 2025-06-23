package scraper;

import scraper.recommendation.AIPriceRecommendationEngine;
import model.ProductInfo;
import java.util.*;
import java.util.stream.*;

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

        while(true) {
            System.out.print("\nIngrese el producto a buscar (o 'salir' para terminar): ");
            String query = scanner.nextLine();

            if(query.equalsIgnoreCase("salir")) break;

            searchAndRecommendProducts(query);
        }
        scanner.close();
    }

    private void searchAndRecommendProducts(String query) {
        System.out.println("\n🛒 Buscando '" + query + "' en múltiples tiendas...");

        // 1. Búsqueda en múltiples plataformas
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

        // 2. Recomendación con IA
        if(products.isEmpty()) {
            System.out.println("No se encontraron productos");
            return;
        }

        ProductInfo bestOption = recommendationEngine.recommendBestProduct(products);
        displayRecommendation(bestOption, products);
    }

    private List<String> generateSearchUrls(String query) {
        return List.of(
                "https://www.amazon.com/s?k=" + query.replace(" ", "+"),
                "https://www.ebay.com/sch/i.html?_nkw=" + query.replace(" ", "+"),
                "https://www.walmart.com/search?q=" + query.replace(" ", "+")
        );
    }

    private void displayRecommendation(ProductInfo best, List<ProductInfo> all) {
        System.out.println("\n✨ Recomendación basada en IA:");
        System.out.println("---------------------------------");
        System.out.println("MEJOR OPCIÓN: " + best.getName());
        System.out.println("PRECIO: $" + best.getCurrentPrice());
        System.out.println("VENDEDOR: " + best.getSeller());
        System.out.println("ENLACE: " + best.getSourceUrl());

        System.out.println("\n🔍 Todas las opciones encontradas:");
        all.stream()
                .sorted(Comparator.comparingDouble(ProductInfo::getCurrentPrice))
                .forEach(p -> System.out.printf(
                        "• %s - $%.2f (%s)\n",
                        p.getName(),
                        p.getCurrentPrice(),
                        p.getSourceUrl().split("/")[2]));
    }
    public class Main {
        public static void main(String[] args) {
            new SmartProductFinder().startInteractiveSearch();
        }
    }
}