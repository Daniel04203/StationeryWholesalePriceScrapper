package ua.edu.example.pricetracker.service;

import org.springframework.stereotype.Service;
import ua.edu.example.pricetracker.model.PriceHistory;
import ua.edu.example.pricetracker.model.Product;
import ua.edu.example.pricetracker.repository.PriceHistoryRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;

    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    /**
     * Records a new price snapshot for the given product.
     */
    public void recordPrice(Product product, BigDecimal priceUah, BigDecimal priceUsd) {
        PriceHistory history = new PriceHistory();
        history.setProduct(product);
        history.setPriceUah(priceUah);
        history.setPriceUsd(priceUsd);
        priceHistoryRepository.save(history);
    }

    /**
     * Returns the price history for a product, newest first.
     */
    public List<PriceHistory> getHistoryForProduct(Long productId) {
        return priceHistoryRepository.findByProductIdOrderByRecordedAtDesc(productId);
    }
}
