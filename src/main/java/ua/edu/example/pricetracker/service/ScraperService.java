package ua.edu.example.pricetracker.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import ua.edu.example.pricetracker.dto.ScrapedProductData;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class ScraperService {

    /**
     * Scrapes product data from a ks-market.com.ua product page.
     *
     * @param url the product URL
     * @return scraped product data
     * @throws IllegalArgumentException if URL is invalid or not from ks-market
     * @throws RuntimeException         if scraping fails
     */
    public ScrapedProductData scrapeProduct(String url) {
        validateUrl(url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            // Product name: <h1 class="inbreadcrumb">...</h1>
            Element titleElement = doc.selectFirst("h1.inbreadcrumb");
            if (titleElement == null) {
                throw new RuntimeException("Не вдалося отримати дані про товар");
            }
            String name = titleElement.text().trim();

            // Product image: <img id="mainImage" src="...">
            String imageUrl = null;
            Element imgElement = doc.selectFirst("img#mainImage");
            if (imgElement != null) {
                imageUrl = imgElement.absUrl("src");
                if (imageUrl.isEmpty()) {
                    imageUrl = imgElement.attr("src");
                }
            }

            // Price: <meta itemprop="price" content="11.7">
            BigDecimal priceUah;
            Element priceMeta = doc.selectFirst("meta[itemprop=price]");
            if (priceMeta != null) {
                String priceStr = priceMeta.attr("content").trim();
                priceUah = new BigDecimal(priceStr);
            } else {
                // Fallback: try span.update_price_options
                Element priceSpan = doc.selectFirst("span.update_price_options");
                if (priceSpan != null) {
                    String priceText = priceSpan.text().replaceAll("[^\\d.,]", "").replace(",", ".").trim();
                    priceUah = new BigDecimal(priceText);
                } else {
                    throw new RuntimeException("Не вдалося отримати ціну товару");
                }
            }

            // Stock: <div class="nalich"> contains "Є в наявності" or "Есть в наличии"
            boolean inStock = false;
            Element stockElement = doc.selectFirst("div.nalich");
            if (stockElement != null) {
                String stockText = stockElement.text().toLowerCase();
                inStock = stockText.contains("є в наявності") || stockText.contains("есть в наличии");
            }

            return new ScrapedProductData(name, imageUrl, priceUah, inStock);

        } catch (IOException e) {
            throw new RuntimeException("Товар не знайдено за вказаним посиланням", e);
        }
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Будь ласка, введіть коректне посилання");
        }
        if (!url.startsWith("https://ks-market.com.ua/ua/")) {
            throw new IllegalArgumentException("Підтримуються лише посилання формату https://ks-market.com.ua/ua/...");
        }
    }
}
