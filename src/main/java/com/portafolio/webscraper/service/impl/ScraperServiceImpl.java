package com.portafolio.webscraper.service.impl;

import com.portafolio.webscraper.model.ProductInfo;
import com.portafolio.webscraper.service.ScraperService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperServiceImpl implements ScraperService {

    @Override
    public List<ProductInfo> searchProducts(String productName) {
        List<ProductInfo> products = new ArrayList<>();

        ProductInfo p1 = ProductInfo.builder()
                .name(productName + " Ejemplo 1")
                .currentPrice(1999.00)  // Usando el campo correcto (Double)
                .originalPrice(2199.00) // Precio original si hay descuento
                .currency("$")          // Símbolo de moneda
                .storeName("Amazon")
                .available(true)
                .rating(4.5)
                .imageUrl("https://via.placeholder.com/300x200")
                .productUrl("#")
                .lastUpdated(LocalDateTime.now())
                .build();

        products.add(p1);

        return products;
    }
}