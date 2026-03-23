package ua.edu.example.pricetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.edu.example.pricetracker.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductUrl(String productUrl);

    List<Product> findByIsDeletedFalse();

    List<Product> findByIsDeletedFalseOrderByPriceUahAsc();

    List<Product> findByIsDeletedFalseOrderByPriceUahDesc();
}
