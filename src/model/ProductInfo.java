package model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {
    private String productId;
    private String name;
    private double currentPrice;
    private String imageUrl;
    private String sourceUrl;
    private LocalDateTime scrapedAt;
    private String seller;
    private boolean inStock;
    private Double rating;  // Nuevo campo nullable
    private Integer reviewCount; // Nuevo campo nullable

    public String getFormattedPrice() {
        return String.format("$%.2f", currentPrice);
    }

    // Método seguro para obtener rating
    public double getSafeRating() {
        return rating != null ? rating : 0.0;
    }

    // Método seguro para obtener conteo de reviews
    public int getSafeReviewCount() {
        return reviewCount != null ? reviewCount : 0;
    }
}