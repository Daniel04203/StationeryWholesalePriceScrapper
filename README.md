# KS-Market Price Tracker

A simple Spring Boot application to monitor stationery prices from [ks-market.com.ua](https://ks-market.com.ua/ua/).
It tracks price changes, stock availability, and automatically converts UAH prices to USD.

## How to Run

1. Make sure you have **Java 21+** and **Maven** installed.
2. Run the application from the project root:
   ```bash
   mvn spring-boot:run
   ```
3. Open **http://localhost:8080** in your web browser.

## How to Use

1. Copy a product link from `https://ks-market.com.ua/ua/`. (Example: `https://ks-market.com.ua/ua/product/ruchka-sharikovaya-bic-round-stic-928497.html`)
2. Paste it into the input field on the home page and click **Додати**.
3. Use the **Управління** page to refresh prices, sort, or remove items.

## Database Access

The application uses an in-memory H2 database. While the app is running, you can view the raw data by visiting **http://localhost:8080/h2-console** with these credentials:

- **JDBC URL**: `jdbc:h2:mem:pricetracker`
- **User Name**: `sa`
- **Password**: _(leave blank)_
