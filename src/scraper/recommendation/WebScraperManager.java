package scraper.recommendation;

import model.ProductInfo;
import scraper.ProductScraper;

import java.util.Scanner;

public class WebScraperManager {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductScraper scraper = new ProductScraper();

        System.out.println("=== Sistema de Búsqueda de Productos ===");

        while(true) {
            System.out.print("\nIngrese URL del producto (o 'salir' para terminar): ");
            String url = scanner.nextLine();

            if(url.equalsIgnoreCase("salir")) break;

            try {
                ProductInfo product = scraper.scrapeProduct(url);
                displayProductInfo(product);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Sistema terminado.");
    }

    private static void displayProductInfo(ProductInfo product) {
        System.out.println("\n📊 Información del Producto:");
        System.out.println("----------------------------------");
        System.out.println("Nombre: " + product.getName());
        System.out.println("Precio: " + product.getFormattedPrice());
        System.out.println("Vendedor: " + product.getSeller());
        System.out.println("Disponible: " + (product.isInStock() ? "Sí" : "No"));

        if(product.getRating() != null) {
            System.out.println("Rating: " + product.getRating() + "/5");
        }

        if(product.getReviewCount() != null) {
            System.out.println("Reviews: " + product.getReviewCount());
        }

        System.out.println("URL: " + product.getSourceUrl());
    }
}