package ua.edu.example.pricetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.edu.example.pricetracker.dto.ProductCreateRequest;
import ua.edu.example.pricetracker.dto.ProductDTO;
import ua.edu.example.pricetracker.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Products management page.
     */
    @GetMapping("/products")
    public String productsPage(@RequestParam(value = "sort", required = false) String sort,
                               Model model) {
        model.addAttribute("products", productService.getProducts(sort));
        model.addAttribute("currentSort", sort);
        return "products";
    }

    /**
     * REST: Add a new product by URL.
     */
    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProduct(@RequestBody ProductCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductDTO product = productService.addProduct(request.getUrl());
            response.put("success", true);
            response.put("message", "Товар успішно додано");
            response.put("product", product);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Не вдалося додати товар");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * REST: Refresh a product's price.
     */
    @PutMapping("/api/products/{id}/refresh")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> refreshProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductDTO product = productService.refreshProduct(id);
            response.put("success", true);
            response.put("message", "Ціну оновлено!");
            response.put("product", product);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Не вдалося оновити ціну");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * REST: Soft-delete a product.
     */
    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(id);
            response.put("success", true);
            response.put("message", "Товар видалено");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Не вдалося видалити товар");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * REST: Get products list with optional sorting.
     */
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(value = "sort", required = false) String sort) {
        return ResponseEntity.ok(productService.getProducts(sort));
    }
}
