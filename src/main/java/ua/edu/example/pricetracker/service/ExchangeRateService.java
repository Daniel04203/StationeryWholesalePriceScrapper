package ua.edu.example.pricetracker.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.edu.example.pricetracker.model.ExchangeRate;
import ua.edu.example.pricetracker.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private static final String PRIVATBANK_API_URL =
            "https://api.privatbank.ua/p24api/pubinfo?exchange&coursid=5";
    private static final String USD = "USD";
    private static final long CACHE_HOURS = 1;

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                               RestTemplate restTemplate) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Returns the current USD sell rate (UAH per 1 USD).
     * Uses cached rate if less than 1 hour old, otherwise fetches from PrivatBank.
     */
    public BigDecimal getUsdSellRate() {
        Optional<ExchangeRate> cached = exchangeRateRepository
                .findTopByCurrencyCodeOrderByFetchedAtDesc(USD);

        if (cached.isPresent() &&
                cached.get().getFetchedAt().isAfter(LocalDateTime.now().minusHours(CACHE_HOURS))) {
            return cached.get().getSellRate();
        }

        return fetchAndCacheRate();
    }

    /**
     * Converts UAH price to USD using the current sell rate.
     */
    public BigDecimal convertUahToUsd(BigDecimal priceUah) {
        BigDecimal sellRate = getUsdSellRate();
        return priceUah.divide(sellRate, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal fetchAndCacheRate() {
        try {
            PrivatBankRate[] rates = restTemplate.getForObject(PRIVATBANK_API_URL, PrivatBankRate[].class);

            if (rates != null) {
                for (PrivatBankRate rate : rates) {
                    if (USD.equals(rate.ccy)) {
                        ExchangeRate exchangeRate = new ExchangeRate();
                        exchangeRate.setCurrencyCode(USD);
                        exchangeRate.setBuyRate(new BigDecimal(rate.buy));
                        exchangeRate.setSellRate(new BigDecimal(rate.sale));
                        exchangeRateRepository.save(exchangeRate);
                        return new BigDecimal(rate.sale);
                    }
                }
            }

            throw new RuntimeException("Не вдалося отримати курс USD з PrivatBank API");
        } catch (Exception e) {
            // Fallback: try cached rate regardless of age
            Optional<ExchangeRate> anyCached = exchangeRateRepository
                    .findTopByCurrencyCodeOrderByFetchedAtDesc(USD);
            if (anyCached.isPresent()) {
                return anyCached.get().getSellRate();
            }
            throw new RuntimeException("Не вдалося отримати курс валюти", e);
        }
    }

    /**
     * DTO for PrivatBank API response.
     */
    private static class PrivatBankRate {
        @JsonProperty("ccy")
        public String ccy;

        @JsonProperty("base_ccy")
        public String baseCcy;

        @JsonProperty("buy")
        public String buy;

        @JsonProperty("sale")
        public String sale;
    }
}
