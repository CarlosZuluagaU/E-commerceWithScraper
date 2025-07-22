package com.portafolio.webscraper.controller;

import com.portafolio.webscraper.model.ProductInfo; // Asegúrate de que esta clase existe y es tu DTO de producto
import com.portafolio.webscraper.service.ScraperService; // Asegúrate de que esta clase existe y contiene la lógica de búsqueda
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/products") // Define el prefijo base para las rutas de este controlador (ej. /api/products/search)
public class ScraperController {

    private static final Logger logger = LoggerFactory.getLogger(ScraperController.class); // Logger para registrar eventos
    private final ScraperService scraperService; // Inyección de dependencia del servicio de scraping

    // Constructor para la inyección de dependencias de ScraperService
    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /**
     * Endpoint para buscar productos por nombre.
     * Acceso: GET /api/products/search?name={nombre_producto}
     *
     * @param name El nombre del producto a buscar, recibido como parámetro de la URL.
     * @return ResponseEntity con una lista de ProductInfo si la búsqueda es exitosa,
     * o un error 400/500 si hay problemas.
     */
    @GetMapping("/search") // Define el endpoint GET para /api/products/search
    public ResponseEntity<List<ProductInfo>> searchProducts(
            @RequestParam(required = false) String name) { // Espera un parámetro de URL llamado 'name'

        // Valida si el parámetro 'name' está ausente o vacío
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Intento de búsqueda sin parámetro 'name'");
            // Devuelve un 400 Bad Request con una lista vacía si el parámetro es inválido
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            logger.info("Buscando productos con nombre: {}", name);
            // Llama al servicio de scraping para obtener la lista de productos
            List<ProductInfo> products = scraperService.searchProducts(name);

            // Si no se encontraron productos, registra y devuelve una respuesta OK con lista vacía
            if (products.isEmpty()) {
                logger.info("No se encontraron resultados para: {}", name);
                return ResponseEntity.ok().body(Collections.emptyList());
            }

            // Si se encontraron productos, registra y devuelve una respuesta OK con los productos
            logger.info("Encontrados {} productos para '{}'", products.size(), name);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            // Captura cualquier excepción durante la búsqueda, la registra y devuelve un error 500
            logger.error("Error al buscar productos", e);
            return ResponseEntity.internalServerError().build(); // Devuelve un 500 Internal Server Error
        }
    }

    // Nota: Si necesitas habilitar CORS, puedes añadir @CrossOrigin(origins = "http://localhost:5173")
    // a esta clase o configurarlo globalmente en una clase WebConfig.
}