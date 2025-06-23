package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    public String getFormattedPrice() {
        return String.format("$%.2f", currentPrice);
    }
}