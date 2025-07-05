package com.portafolio.webscraper.controller;

import com.portafolio.webscraper.model.ProductInfo;
import com.portafolio.webscraper.service.ScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ScraperController {

    private static final Logger logger = LoggerFactory.getLogger(ScraperController.class);
    private final ScraperService scraperService;

    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductInfo>> searchProducts(
            @RequestParam(required = false) String name) {

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Intento de búsqueda sin parámetro 'name'");
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            logger.info("Buscando productos con nombre: {}", name);
            List<ProductInfo> products = scraperService.searchProducts(name);

            if (products.isEmpty()) {
                logger.info("No se encontraron resultados para: {}", name);
                return ResponseEntity.ok().body(Collections.emptyList());
            }

            logger.info("Encontrados {} productos para '{}'", products.size(), name);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error al buscar productos", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}