package com.portafolio.webscraper.controller;

import com.portafolio.webscraper.services.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    // 1. Añadimos un Logger para registrar la actividad del controlador.
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/searches")
    // 2. Cambiamos el tipo de retorno a ResponseEntity para un control total.
    public ResponseEntity<Map<String, Integer>> getSearchStatistics() {
        try {
            logger.info("Solicitud recibida para obtener estadísticas de búsqueda.");
            Map<String, Integer> searchCounts = statsService.getSearchCounts();
            logger.info("Estadísticas obtenidas exitosamente con {} entradas.", searchCounts.size());

            // Devolvemos los datos con un estado 200 OK.
            return ResponseEntity.ok(searchCounts);

        } catch (Exception e) {
            // 3. Si algo sale mal en el servicio, lo registramos y devolvemos un error controlado.
            logger.error("Error al obtener las estadísticas de búsqueda.", e);

            // Devolvemos un estado 500 Internal Server Error con un cuerpo vacío.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", -1)); // Cuerpo opcional
        }
    }
}