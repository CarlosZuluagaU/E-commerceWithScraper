package com.portafolio.webscraper.service;

import com.portafolio.webscraper.model.ProductInfo;
import java.util.List;

public interface ScraperService {
    List<ProductInfo> searchProducts(String productName);
}