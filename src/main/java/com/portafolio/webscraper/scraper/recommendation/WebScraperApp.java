package com.portafolio.webscraper.scraper.recommendation;

import com.portafolio.webscraper.model.ProductInfo;
import com.portafolio.webscraper.scraper.ProductScraper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebScraperApp {
    private static final Map<String, String> SUPPORTED_STORES = Map.of(
            "amazon.com", "Amazon",
            "ebay.com", "eBay",
            "walmart.com", "Walmart",
            "bestbuy.com", "Best Buy"
    );
    private static final String NO_TITLE_FOUND = "No title found";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ProductScraper scraper = new ProductScraper();
            AIPriceRecommendationEngine recommender = new AIPriceRecommendationEngine();

            printWelcomeMessage();

            while (true) {
                String input = getUserInput(scanner);
                if (input.equalsIgnoreCase("salir")) break;

                try {
                    long startTime = System.nanoTime();
                    List<ProductInfo> products = searchProducts(input, scraper);
                    displayResults(input, products, recommender, scanner);
                    logSearchDuration(startTime);
                } catch (Exception e) {
                    handleError(input, e);
                }
            }
        }
        System.out.println("\n¡Gracias por usar nuestro buscador de mejores precios!");
    }

    private static List<ProductInfo> searchProducts(String input, ProductScraper scraper) {
        System.out.println("\n🔍 Iniciando búsqueda en múltiples tiendas...");
        String sanitizedInput = input.trim();

        if (sanitizedInput.startsWith("http")) {
            return searchProductsFromUrl(sanitizedInput, scraper);
        } else {
            return searchProductsFromName(sanitizedInput, scraper);
        }
    }

    private static List<ProductInfo> searchProductsFromName(String productName, ProductScraper scraper) {
        return SUPPORTED_STORES.keySet().parallelStream()
                .map(domain -> scrapeFromStore(productName, domain, scraper))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<ProductInfo> searchProductsFromUrl(String url, ProductScraper scraper) {
        List<ProductInfo> foundProducts = new ArrayList<>();
        String originalDomain = getDomainFromUrl(url);

        ProductInfo initialProduct = scrapeInitialUrl(url, originalDomain, scraper);
        if (initialProduct == null) {
            System.out.println("⚠️ No se pudo obtener un producto válido desde la URL para continuar la búsqueda.");
            return foundProducts;
        }
        foundProducts.add(initialProduct);

        String cleanProductName = normalizeProductName(initialProduct.getName());
        System.out.printf("\nBuscando productos similares a '%s' en otras tiendas...\n", cleanProductName);

        List<ProductInfo> otherProducts = SUPPORTED_STORES.keySet().parallelStream()
                .filter(domain -> !domain.equals(originalDomain))
                .map(domain -> scrapeFromStore(cleanProductName, domain, scraper))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        foundProducts.addAll(otherProducts);
        return foundProducts;
    }

    private static ProductInfo scrapeFromStore(String productName, String domain, ProductScraper scraper) {
        String storeName = SUPPORTED_STORES.get(domain);
        System.out.printf("Buscando en %-10s... ", storeName);
        try {
            ProductInfo product = scraper.scrapeFromSearch(productName, domain);

            Optional<String> validationError = getValidationError(product);
            if (validationError.isEmpty()) {
                System.out.printf("✅ Encontrado (%s)%n", product.getFormattedPrice());
                return product;
            } else {
                System.out.printf("⚠️ Descartado (%s)%n", validationError.get());
                return null;
            }
        } catch (Exception e) {
            logStoreError(storeName, e);
            return null;
        }
    }

    private static ProductInfo scrapeInitialUrl(String url, String domain, ProductScraper scraper) {
        if (domain == null) return null;
        System.out.println("Analizando URL de " + SUPPORTED_STORES.get(domain) + "...");
        try {
            ProductInfo product = scraper.scrapeProduct(url);
            if (getValidationError(product).isEmpty()) {
                System.out.println("✅ Producto extraído de la URL inicial.");
                return product;
            }
        } catch (Exception e) {
            logStoreError(SUPPORTED_STORES.get(domain), e);
        }
        return null;
    }

    private static Optional<String> getValidationError(ProductInfo product) {
        if (product == null) return Optional.of("Producto nulo");
        if (product.getName() == null || product.getName().isBlank() || product.getName().equalsIgnoreCase(NO_TITLE_FOUND)) {
            return Optional.of("Título no encontrado");
        }
        if (product.getCurrentPrice() == null || product.getCurrentPrice() <= 0) {
            return Optional.of("Precio no válido");
        }
        String lowerCaseName = product.getName().toLowerCase();
        if (lowerCaseName.contains("renewed") || lowerCaseName.contains("refurbished") || lowerCaseName.contains("usado")) {
            return Optional.of("No es nuevo");
        }
        return Optional.empty();
    }

    private static String normalizeProductName(String fullProductName) {
        if (fullProductName == null) return "";
        Pattern pattern = Pattern.compile("^(.*?)(?i)(\\s+\\d+g?b|\\s+-\\s+|\\s*\\(|\\s*,|renewed|refurbished|unlocked)");
        Matcher matcher = pattern.matcher(fullProductName);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return fullProductName.length() > 50 ? fullProductName.substring(0, 50) : fullProductName;
    }

    private static void logStoreError(String storeName, Exception e) {
        String rootCauseMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        String errorMessage = rootCauseMessage != null ? rootCauseMessage.trim() : "Error desconocido";

        if (errorMessage.contains("No se encontraron resultados")) {
            System.out.println("❌ No se encontraron resultados.");
        } else {
            if (errorMessage.length() > 80) {
                errorMessage = errorMessage.substring(0, 80) + "...";
            }
            System.err.printf("❌ Error (%s)%n", errorMessage);
        }
    }

    private static void displayResults(String input, List<ProductInfo> products,
                                       AIPriceRecommendationEngine recommender, Scanner scanner) {
        if (products.isEmpty()) {
            System.out.println("\n⚠️ No se encontraron productos válidos para tu búsqueda.");
            return;
        }
        displayResultsPage(input, products, recommender);
        System.out.println("\nPresione Enter para realizar una nueva búsqueda...");
        scanner.nextLine();
        clearConsole();
    }

    private static String getDomainFromUrl(String url) {
        return SUPPORTED_STORES.keySet().stream()
                .filter(url::contains)
                .findFirst()
                .orElse(null);
    }

    private static void printWelcomeMessage() {
        System.out.println("🛒 BUSCADOR INTELIGENTE DE MEJORES PRECIOS 🛒");
        System.out.println("---------------------------------------------");
        System.out.println("Ingresa un nombre de producto (ej: 'iPhone 15 Pro Max')");
        System.out.println("O una URL de producto de Amazon, eBay, Walmart o Best Buy.");
    }

    private static String getUserInput(Scanner scanner) {
        System.out.print("\nIngrese URL o nombre del producto (o 'salir' para terminar): ");
        return scanner.nextLine();
    }

    private static void logSearchDuration(long startTime) {
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        System.out.printf("\nBúsqueda completada en %d ms\n", duration);
    }

    private static void handleError(String input, Exception e) {
        System.err.println("\n❌ Ocurrió un error inesperado durante el proceso: " + e.getMessage());
        e.printStackTrace();
    }

    private static void displayResultsPage(String searchQuery, List<ProductInfo> products,
                                           AIPriceRecommendationEngine recommender) {
        printSearchResultsHeader(searchQuery, products);
        displayBestProduct(recommender, products);
        displayPriceComparison(products);
    }

    private static void printSearchResultsHeader(String searchQuery, List<ProductInfo> validProducts) {
        System.out.println("\n================================================");
        System.out.println("         RESULTADOS DE BÚSQUEDA OPTIMIZADA      ");
        System.out.println("================================================");
        System.out.printf("🔎 Término buscado: %s\n", searchQuery);
        System.out.printf("🏪 Productos encontrados: %d\n", validProducts.size());
        System.out.println("------------------------------------------------");
    }

    private static void displayBestProduct(AIPriceRecommendationEngine recommender, List<ProductInfo> validProducts) {
        ProductInfo bestOption = recommender.recommendBestProduct(validProducts);
        if (bestOption == null) return;

        System.out.println("\n⭐ MEJOR OPCIÓN RECOMENDADA ⭐");
        System.out.println("------------------------------------------");
        // CORRECCIÓN: Usar getStoreName() en lugar de getSeller()
        System.out.printf("🏪 Tienda: %s\n", bestOption.getStoreName());
        System.out.printf("📛 Nombre: %s\n", bestOption.getName());
        System.out.printf("💲 Precio: %s\n", bestOption.getFormattedPrice());
        if (bestOption.getRating() != null) {
            System.out.printf("⭐ Rating: %.1f/5 (%d reviews)\n", bestOption.getRating(), bestOption.getSafeReviewCount());
        }
        // CORRECCIÓN: Usar getProductUrl() en lugar de getSourceUrl()
        System.out.printf("🔗 Enlace: %s\n", shortenUrl(bestOption.getProductUrl()));
        System.out.println("------------------------------------------");
        recommender.displayAnalysis(bestOption, validProducts);
    }

    private static void displayPriceComparison(List<ProductInfo> products) {
        System.out.println("\n🔍 COMPARATIVA DE PRECIOS:");
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("%-15s | %-12s | %-10s | %s\n", "TIENDA", "PRECIO", "RATING", "DISPONIBLE");
        System.out.println("-----------------------------------------------------------------");

        products.stream()
                .sorted(Comparator.comparingDouble(ProductInfo::getCurrentPrice))
                .forEach(p -> System.out.printf("%-15s | %-12s | %-10s | %s\n",
                        // CORRECCIÓN: Usar getStoreName() en lugar de getSeller()
                        p.getStoreName(),
                        p.getFormattedPrice(),
                        p.getRating() != null ? String.format("%.1f/5", p.getRating()) : "N/A",
                        // CORRECCIÓN: Usar isAvailable() en lugar de isInStock()
                        p.isAvailable() ? "✅ Sí" : "❌ No"));
        System.out.println("-----------------------------------------------------------------");
    }

    private static String shortenUrl(String url) {
        if (url == null || url.length() < 60) return url;
        return url.substring(0, 45) + "..." + url.substring(url.length() - 10);
    }

    private static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; ++i) System.out.println();
        }
    }
}