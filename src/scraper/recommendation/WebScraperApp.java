package scraper.recommendation;

import model.ProductInfo;
import scraper.ProductScraper;
import scraper.recommendation.AIPriceRecommendationEngine;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebScraperApp {
    private static final Map<String, String> STORE_NAMES = Map.of(
            "amazon.com", "Amazon",
            "ebay.com", "eBay",
            "walmart.com", "Walmart",
            "bestbuy.com", "Best Buy"
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductScraper scraper = new ProductScraper();
        AIPriceRecommendationEngine recommender = new AIPriceRecommendationEngine();

        System.out.println("🛒 BUSCADOR INTELIGENTE DE MEJORES PRECIOS 🛒");
        System.out.println("---------------------------------------------");

        while(true) {
            System.out.print("\nIngrese URL o nombre del producto (o 'salir' para terminar): ");
            String input = scanner.nextLine().trim();

            if(input.equalsIgnoreCase("salir")) break;

            try {
                long startTime = System.nanoTime();

                List<ProductInfo> products = searchProducts(input, scraper);

                if(products.isEmpty()) {
                    System.out.println("\nNo se encontraron resultados para: " + input);
                    continue;
                }

                displayResultsPage(input, products, recommender);

                long endTime = System.nanoTime();
                long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                System.out.printf("\nTiempo de búsqueda: %d ms\n", duration);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("\n¡Gracias por usar nuestro buscador de mejores precios!");
    }

    private static List<ProductInfo> searchProducts(String input, ProductScraper scraper) {
        boolean isUrl = input.startsWith("http");
        List<String> searchUrls = isUrl ? generateSearchUrls(input) : generateSearchUrlsFromName(input);

        return searchUrls.parallelStream()
                .map(url -> {
                    try {
                        System.out.println("Buscando en: " + getStoreNameFromUrl(url));
                        return scraper.scrapeProduct(url);
                    } catch (Exception e) {
                        System.err.println("Error en " + url + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<String> generateSearchUrls(String originalUrl) {
        String productName = extractProductNameFromUrl(originalUrl);
        return generateSearchUrlsFromName(productName);
    }

    private static List<String> generateSearchUrlsFromName(String productName) {
        String query = productName.replace(" ", "+");
        return List.of(
                "https://www.amazon.com/s?k=" + query,
                "https://www.ebay.com/sch/i.html?_nkw=" + query,
                "https://www.walmart.com/search?q=" + query,
                "https://www.bestbuy.com/site/searchpage.jsp?st=" + query
        );
    }

    private static String extractProductNameFromUrl(String url) {
        try {
            if(url.contains("amazon.com/dp/")) {
                return url.split("/dp/")[1].split("/")[0];
            } else if(url.contains("ebay.com/itm/")) {
                return url.split("/itm/")[1].split("\\?")[0];
            } else if(url.contains("walmart.com/ip/")) {
                return url.split("/ip/")[1].split("/")[0];
            } else if(url.contains("bestbuy.com/sku/")) {
                return url.split("/sku/")[1].split("/")[0];
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo nombre de producto: " + e.getMessage());
        }
        return url.replaceAll("[^a-zA-Z0-9]", " ");
    }

    private static String getStoreNameFromUrl(String url) {
        for (Map.Entry<String, String> entry : STORE_NAMES.entrySet()) {
            if(url.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Tienda desconocida";
    }

    private static void displayResultsPage(String searchQuery, List<ProductInfo> products,
                                           AIPriceRecommendationEngine recommender) {
        clearConsole();

        System.out.println("==================================================");
        System.out.println("       🏆 RESULTADOS DE BÚSQUEDA INTELIGENTE      ");
        System.out.println("==================================================");
        System.out.printf("Búsqueda: %s\n", searchQuery);
        System.out.printf("Productos encontrados: %d\n", products.size());
        System.out.println("--------------------------------------------------");

        ProductInfo bestOption = recommender.recommendBestProduct(products);
        displayBestOption(bestOption);
        recommender.displayAnalysis(bestOption, products);
        displayPriceComparison(products);

        System.out.println("==================================================");
        System.out.println("Presione Enter para continuar...");
        new Scanner(System.in).nextLine();
    }

    private static void displayBestOption(ProductInfo product) {
        System.out.println("\n⭐ MEJOR OPCIÓN ENCONTRADA ⭐");
        System.out.println("------------------------------------------");
        System.out.printf("🏪 Tienda: %s\n", product.getSeller());
        System.out.printf("📛 Nombre: %s\n", product.getName());
        System.out.printf("💲 Precio: %s\n", product.getFormattedPrice());

        if(product.getRating() != null) {
            System.out.printf("⭐ Rating: %.1f/5 (%d reviews)\n",
                    product.getRating(), product.getSafeReviewCount());
        }

        System.out.printf("🖼️ Imagen: %s\n", shortenUrl(product.getImageUrl()));
        System.out.printf("🔗 Enlace: %s\n", shortenUrl(product.getSourceUrl()));
        System.out.printf("📅 Fecha: %s\n", product.getScrapedAt());
        System.out.println("------------------------------------------");
    }

    private static void displayPriceComparison(List<ProductInfo> products) {
        System.out.println("\n🔍 COMPARATIVA DE PRECIOS:");
        System.out.println("------------------------------------------");
        System.out.printf("%-15s | %-10s | %-8s | %s\n",
                "TIENDA", "PRECIO", "RATING", "DISPONIBLE");
        System.out.println("------------------------------------------");

        products.stream()
                .sorted(Comparator.comparingDouble(ProductInfo::getCurrentPrice))
                .forEach(p -> System.out.printf("%-15s | %-10s | %-8s | %s\n",
                        p.getSeller(),
                        p.getFormattedPrice(),
                        p.getRating() != null ? String.format("%.1f/5", p.getRating()) : "N/A",
                        p.isInStock() ? "✅ Sí" : "❌ No"));
    }

    private static String shortenUrl(String url) {
        if(url == null || url.length() < 50) return url;
        return url.substring(0, 30) + "..." + url.substring(url.length() - 15);
    }

    private static void clearConsole() {
        try {
            if(System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback si no se puede limpiar la consola
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }
}