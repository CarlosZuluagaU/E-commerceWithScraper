package com.portafolio.webscraper.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final Map<String, Integer> searchCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastSearchTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> relatedSearches = new ConcurrentHashMap<>();

    /**
     * Registra una búsqueda y actualiza las estadísticas
     * @param searchTerm Término de búsqueda
     * @param relatedTerms Términos relacionados (opcional)
     */
    public synchronized void recordSearch(String searchTerm, String... relatedTerms) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return;
        }

        String normalizedTerm = normalizeTerm(searchTerm);

        // Actualiza contador
        searchCounts.merge(normalizedTerm, 1, Integer::sum);

        // Registra timestamp
        lastSearchTimestamps.put(normalizedTerm, LocalDateTime.now());

        // Registra términos relacionados
        if (relatedTerms != null && relatedTerms.length > 0) {
            relatedSearches.computeIfAbsent(normalizedTerm, k -> ConcurrentHashMap.newKeySet()) // Usar un Set concurrente es más seguro
                    .addAll(Arrays.stream(relatedTerms)
                            .filter(Objects::nonNull)
                            .map(this::normalizeTerm)
                            .collect(Collectors.toSet()));
        }
    }

    // --- MÉTODOS PARA OBTENER ESTADÍSTICAS ---

    /**
     * ¡NUEVO MÉTODO!
     * Obtiene el mapa de conteo de búsquedas para ser usado por StatsController.
     * Devuelve una copia inmutable para proteger los datos internos.
     */
    public Map<String, Integer> getSearchCounts() {
        return Collections.unmodifiableMap(new HashMap<>(searchCounts));
    }

    /**
     * Obtiene estadísticas completas de búsqueda
     */
    public SearchStats getSearchStats() {
        return new SearchStats(
                Collections.unmodifiableMap(new HashMap<>(searchCounts)),
                Collections.unmodifiableMap(new HashMap<>(lastSearchTimestamps)),
                Collections.unmodifiableMap(new HashMap<>(relatedSearches))
        );
    }

    /**
     * Obtiene las búsquedas más populares ordenadas
     * @param limit Número máximo de resultados (opcional)
     */
    public List<SearchTermCount> getTopSearches(Integer limit) {
        return searchCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit != null && limit > 0 ? limit : Long.MAX_VALUE) // Lógica de límite mejorada
                .map(entry -> new SearchTermCount(
                        entry.getKey(),
                        entry.getValue(),
                        lastSearchTimestamps.get(entry.getKey())))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene términos relacionados a una búsqueda
     */
    public Set<String> getRelatedSearches(String term) {
        return relatedSearches.getOrDefault(normalizeTerm(term), Collections.emptySet());
    }

    /**
     * Normaliza términos de búsqueda
     */
    private String normalizeTerm(String term) {
        return term.toLowerCase().trim();
    }

    // --- Clases de Datos (DTOs) ---
    // Sugerencia: Convertir estas clases a "records" de Java 17+ para un código más limpio.

    public static class SearchStats {
        // ... (código existente)
        private final Map<String, Integer> searchCounts;
        private final Map<String, LocalDateTime> lastSearches;
        private final Map<String, Set<String>> relatedSearches;

        public SearchStats(Map<String, Integer> searchCounts,
                           Map<String, LocalDateTime> lastSearches,
                           Map<String, Set<String>> relatedSearches) {
            this.searchCounts = searchCounts;
            this.lastSearches = lastSearches;
            this.relatedSearches = relatedSearches;
        }

        // Getters
        public Map<String, Integer> getSearchCounts() { return searchCounts; }
        public Map<String, LocalDateTime> getLastSearches() { return lastSearches; }
        public Map<String, Set<String>> getRelatedSearches() { return relatedSearches; }
    }

    public static class SearchTermCount {
        // ... (código existente)
        private final String term;
        private final int count;
        private final LocalDateTime lastSearched;

        public SearchTermCount(String term, int count, LocalDateTime lastSearched) {
            this.term = term;
            this.count = count;
            this.lastSearched = lastSearched;
        }

        // Getters
        public String getTerm() { return term; }
        public int getCount() { return count; }
        public LocalDateTime getLastSearched() { return lastSearched; }
    }
}