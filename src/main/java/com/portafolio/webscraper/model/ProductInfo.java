package com.portafolio.webscraper.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa la información de un producto extraída de una tienda online.
 * Esta clase es el modelo de datos central de la aplicación.
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {

    @NonNull
    private String name;

    // Campos de precio
    private Double currentPrice;
    private Double originalPrice;
    private String currency;

    // Información de la tienda
    private String storeName;
    private String storeId;

    // URLs
    private String imageUrl;
    @NonNull
    private String productUrl;

    // Disponibilidad
    private boolean available;
    private Integer stockQuantity;

    // Valoraciones
    private Double rating;
    private Integer reviewCount;

    // Metadatos
    private LocalDateTime lastUpdated;
    private String productId;
    private String brand;
    private String category;

    // --- MÉTODOS DE LÓGICA Y UTILIDAD ---

    public double getSafeRating() {
        return Optional.ofNullable(rating).orElse(0.0);
    }

    public int getSafeReviewCount() {
        return Optional.ofNullable(reviewCount).orElse(0);
    }

    public String getFormattedPrice() {
        if (currentPrice == null) return "N/A";
        // Asigna un símbolo de moneda por defecto si no se especifica.
        String currencySymbol = Optional.ofNullable(currency).filter(c -> !c.isEmpty()).orElse("$");
        return String.format("%s%.2f", currencySymbol, currentPrice);
    }

    public boolean hasDiscount() {
        return originalPrice != null && currentPrice != null && currentPrice < originalPrice;
    }

    public double getDiscountPercentage() {
        if (!hasDiscount()) return 0.0;
        return ((originalPrice - currentPrice) / originalPrice) * 100;
    }

    /**
     * Valida que el objeto ProductInfo tenga la información mínima indispensable.
     * @return true si el producto es válido, false en caso contrario.
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                productUrl != null && !productUrl.trim().isEmpty() &&
                currentPrice != null && currentPrice > 0 &&
                storeName != null && !storeName.trim().isEmpty();
    }

    // --- MÉTODOS SOBREESCRITOS ---

    /**
     * Lógica de igualdad personalizada: dos productos son iguales si su ID de producto coincide,
     * o si su URL y tienda son las mismas (como fallback).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductInfo that = (ProductInfo) o;
        // Prioriza la comparación por productId si ambos lo tienen.
        if (productId != null && that.productId != null) {
            return productId.equals(that.productId);
        }
        // Fallback a URL + Tienda.
        return productUrl.equals(that.productUrl) && storeName.equals(that.storeName);
    }

    /**
     * Genera el hash code usando la misma lógica que equals para mantener la consistencia.
     */
    @Override
    public int hashCode() {
        return Objects.hash(productId != null ? productId : productUrl + storeName);
    }
}