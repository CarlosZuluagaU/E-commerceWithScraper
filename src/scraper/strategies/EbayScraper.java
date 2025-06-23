package scraper.strategies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import model.ProductInfo;
import scraper.WebDownloader;

public class EbayScraper implements ScraperStrategy {

    // Selectores CSS actualizados para eBay (2023)
    private static final String PRICE_SELECTOR = ".x-price-primary > span";
    private static final String TITLE_SELECTOR = ".x-item-title__mainTitle";
    private static final String IMAGE_SELECTOR = ".ux-image-carousel-item img";
    private static final String SELLER_SELECTOR = ".ux-seller-section__item--seller";

    @Override
    public ProductInfo scrape(String url) throws ScraperException {
        try {
            Document doc = getDocument(url);
            ProductInfo product = extractProductData(doc, url);
            validateProductData(product);
            return product;
        } catch (ScraperException e) {
            throw e;
        } catch (Exception e) {
            throw new ScraperException("Error scraping eBay: " + e.getMessage(), e);
        }
    }

    private Document getDocument(String url) throws ScraperException {
        try {
            String html = WebDownloader.downloadHtml(url);
            return Jsoup.parse(html);
        } catch (Exception e) {
            throw new ScraperException("Error al descargar el HTML: " + e.getMessage(), e);
        }
    }

    private ProductInfo extractProductData(Document doc, String url) {
        ProductInfo product = new ProductInfo();
        product.setSourceUrl(url);

        extractTitle(doc, product);
        extractPrice(doc, product);
        extractImage(doc, product);
        extractSellerInfo(doc, product);

        return product;
    }

    private void validateProductData(ProductInfo product) throws ScraperException {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new ScraperException("Nombre del producto no encontrado");
        }
        if (product.getCurrentPrice() <= 0) {
            throw new ScraperException("Precio del producto no válido");
        }
    }

    private void extractTitle(Document doc, ProductInfo product) {
        Element titleElement = doc.selectFirst(TITLE_SELECTOR);
        if (titleElement != null) {
            product.setName(titleElement.text().trim());
        }
    }

    private void extractPrice(Document doc, ProductInfo product) {
        Element priceElement = doc.selectFirst(PRICE_SELECTOR);
        if (priceElement != null) {
            try {
                String priceText = priceElement.text()
                        .replaceAll("[^\\d.,]", "")
                        .replace(",", ".");
                double price = Double.parseDouble(priceText);
                product.setCurrentPrice(price);
            } catch (NumberFormatException e) {
                System.err.println("Formato de precio inválido: " + priceElement.text());
            }
        }
    }

    private void extractImage(Document doc, ProductInfo product) {
        Element imageElement = doc.selectFirst(IMAGE_SELECTOR);
        if (imageElement != null) {
            String imageUrl = imageElement.attr("src");
            if (imageUrl.startsWith("http")) {
                product.setImageUrl(imageUrl);
            }
        }
    }

    private void extractSellerInfo(Document doc, ProductInfo product) {
        Element sellerElement = doc.selectFirst(SELLER_SELECTOR);
        if (sellerElement != null) {
            product.setSeller(sellerElement.text().trim());
        }
    }
}