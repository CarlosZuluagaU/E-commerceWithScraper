package scraper.strategies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import model.ProductInfo;
import scraper.WebDownloader;

public class GenericScraper implements ScraperStrategy {
    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            // Descargar y parsear HTML
            String html = WebDownloader.downloadHtml(url);
            Document doc = Jsoup.parse(html);

            // Construir objeto ProductInfo
            return ProductInfo.builder()
                    .name(extractMeta(doc, "og:title", "title"))
                    .currentPrice(extractPrice(doc))
                    .sourceUrl(url)
                    .inStock(true).build();

        } catch (Exception e) {
            throw new ScraperException("Error en scraping genérico: " + e.getMessage());
        }
    }

    /**
     * Extrae metadatos del documento HTML con múltiples opciones de selectores
     * @param doc Documento Jsoup
     * @param selectors Varargs con los selectores a probar (en orden de prioridad)
     * @return Valor del primer selector que encuentre o "No title" si no encuentra nada
     */
    private String extractMeta(Document doc, String... selectors) {
        // Primero verifica meta tags
        for (String selector : selectors) {
            Element meta = doc.selectFirst("meta[property=" + selector + "]");
            if (meta != null) {
                return meta.attr("content").trim();
            }
        }

        // Luego verifica elementos HTML directos
        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                return element.text().trim();
            }
        }

        return "No title";
    }

    /**
     * Extrae el precio del documento usando patrones comunes
     * @param doc Documento Jsoup
     * @return Precio encontrado o 0.0 si no encuentra
     */
    private double extractPrice(Document doc) {
        // 1. Buscar en meta tags (común en tiendas online)
        Element metaPrice = doc.selectFirst("meta[property=og:price:amount]");
        if (metaPrice != null) {
            try {
                return Double.parseDouble(metaPrice.attr("content"));
            } catch (NumberFormatException e) {
                System.err.println("Formato de precio meta inválido");
            }
        }

        // 2. Buscar elementos con clases comunes para precios
        String[] priceSelectors = {
                "[itemprop=price]",
                ".price",
                ".current-price",
                ".value",
                "[content=price]"
        };

        for (String selector : priceSelectors) {
            Element priceElement = doc.selectFirst(selector);
            if (priceElement != null) {
                try {
                    String priceText = priceElement.text()
                            .replaceAll("[^\\d.,]", "")
                            .replace(",", ".");
                    return Double.parseDouble(priceText);
                } catch (NumberFormatException e) {
                    System.err.println("Formato de precio inválido: " + priceElement.text());
                }
            }
        }

        return 0.0;
    }
}