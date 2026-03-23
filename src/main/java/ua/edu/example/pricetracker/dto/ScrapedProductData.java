package ua.edu.example.pricetracker.dto;

import java.math.BigDecimal;

public class ScrapedProductData {

    private String name;
    private String imageUrl;
    private BigDecimal priceUah;
    private boolean inStock;

    public ScrapedProductData() {}

    public ScrapedProductData(String name, String imageUrl, BigDecimal priceUah, boolean inStock) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.priceUah = priceUah;
        this.inStock = inStock;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPriceUah() { return priceUah; }
    public void setPriceUah(BigDecimal priceUah) { this.priceUah = priceUah; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
}
