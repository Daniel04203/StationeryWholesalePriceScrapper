package ua.edu.example.pricetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTO {

    private Long id;
    private String name;
    private String productUrl;
    private String imageUrl;
    private BigDecimal priceUah;
    private BigDecimal priceUsd;
    private boolean inStock;
    private LocalDateTime updatedAt;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String productUrl, String imageUrl,
                      BigDecimal priceUah, BigDecimal priceUsd, boolean inStock,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.priceUah = priceUah;
        this.priceUsd = priceUsd;
        this.inStock = inStock;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPriceUah() { return priceUah; }
    public void setPriceUah(BigDecimal priceUah) { this.priceUah = priceUah; }

    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
