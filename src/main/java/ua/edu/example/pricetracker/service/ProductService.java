package ua.edu.example.pricetracker.service;

import org.springframework.stereotype.Service;
import ua.edu.example.pricetracker.dto.ProductDTO;
import ua.edu.example.pricetracker.dto.ScrapedProductData;
import ua.edu.example.pricetracker.model.Product;
import ua.edu.example.pricetracker.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ScraperService scraperService;
    private final ExchangeRateService exchangeRateService;
    private final PriceHistoryService priceHistoryService;

    public ProductService(ProductRepository productRepository,
                          ScraperService scraperService,
                          ExchangeRateService exchangeRateService,
                          PriceHistoryService priceHistoryService) {
        this.productRepository = productRepository;
        this.scraperService = scraperService;
        this.exchangeRateService = exchangeRateService;
        this.priceHistoryService = priceHistoryService;
    }

    /**
     * Adds a new product by scraping its URL.
     */
    public ProductDTO addProduct(String url) {
        if (url != null) {
            url = url.trim();
        }

        // Check if product already exists
        Optional<Product> existing = productRepository.findByProductUrl(url);
        if (existing.isPresent()) {
            Product p = existing.get();
            if (!p.isDeleted()) {
                throw new IllegalArgumentException("Цей товар вже додано до списку");
            }
            // Re-activate soft-deleted product and refresh its data
            p.setDeleted(false);
            return refreshProductInternal(p);
        }

        ScrapedProductData scraped = scraperService.scrapeProduct(url);

        BigDecimal priceUsd = exchangeRateService.convertUahToUsd(scraped.getPriceUah());

        Product product = new Product();
        product.setName(scraped.getName());
        product.setProductUrl(url);
        product.setImageUrl(scraped.getImageUrl());
        product.setPriceUah(scraped.getPriceUah());
        product.setPriceUsd(priceUsd);
        product.setInStock(scraped.isInStock());
        product = productRepository.save(product);

        // Record initial price in history
        priceHistoryService.recordPrice(product, scraped.getPriceUah(), priceUsd);

        return toDTO(product);
    }

    /**
     * Refreshes price data for an existing product.
     */
    public ProductDTO refreshProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не знайдено"));

        if (product.isDeleted()) {
            throw new RuntimeException("Товар було видалено");
        }

        return refreshProductInternal(product);
    }

    /**
     * Soft-deletes a product.
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не знайдено"));

        product.setDeleted(true);
        productRepository.save(product);
    }

    /**
     * Returns all non-deleted products, optionally sorted by price.
     */
    public List<ProductDTO> getProducts(String sort) {
        List<Product> products;

        if ("asc".equalsIgnoreCase(sort)) {
            products = productRepository.findByIsDeletedFalseOrderByPriceUahAsc();
        } else if ("desc".equalsIgnoreCase(sort)) {
            products = productRepository.findByIsDeletedFalseOrderByPriceUahDesc();
        } else {
            products = productRepository.findByIsDeletedFalse();
        }

        return products.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // --- Internal helpers ---

    private ProductDTO refreshProductInternal(Product product) {
        ScrapedProductData scraped = scraperService.scrapeProduct(product.getProductUrl());
        BigDecimal priceUsd = exchangeRateService.convertUahToUsd(scraped.getPriceUah());

        boolean priceChanged = product.getPriceUah() == null ||
                               product.getPriceUah().compareTo(scraped.getPriceUah()) != 0 ||
                               product.getPriceUsd() == null ||
                               product.getPriceUsd().compareTo(priceUsd) != 0;

        product.setName(scraped.getName());
        product.setImageUrl(scraped.getImageUrl());
        product.setPriceUah(scraped.getPriceUah());
        product.setPriceUsd(priceUsd);
        product.setInStock(scraped.isInStock());
        product = productRepository.save(product);

        if (priceChanged) {
            priceHistoryService.recordPrice(product, scraped.getPriceUah(), priceUsd);
        }

        return toDTO(product);
    }

    private ProductDTO toDTO(Product p) {
        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getProductUrl(),
                p.getImageUrl(),
                p.getPriceUah(),
                p.getPriceUsd(),
                p.isInStock(),
                p.getUpdatedAt()
        );
    }
}
