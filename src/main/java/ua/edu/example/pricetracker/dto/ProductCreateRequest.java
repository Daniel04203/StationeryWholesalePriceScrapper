package ua.edu.example.pricetracker.dto;

public class ProductCreateRequest {

    private String url;

    public ProductCreateRequest() {}

    public ProductCreateRequest(String url) {
        this.url = url;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
