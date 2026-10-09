package com.xzie.user_service.controller;


import com.xzie.user_service.dao.ProductRepo;
import com.xzie.user_service.model.Product;
import com.xzie.user_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class ProductController
{

    @Autowired
    ProductService service;

    @Autowired
    ProductRepo productRepo;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAll()
    {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/products/{id}")
    public Optional<Product> getId(@PathVariable Long id)
    {
        return service.getId(id);
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Product> addProduct(@RequestBody Product product, Authentication auth)
    {
        product.setSellerName(auth.getName());
        return ResponseEntity.ok(service.addOrUpdateProduct(product));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product in, Authentication auth) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !p.getSellerName().equals(auth.getName()))
            throw new AccessDeniedException("Not your product");

        p.setName(in.getName());
        p.setDescription(in.getDescription());
        p.setPrice(in.getPrice());
        p.setStock(in.getStock());
        p.setCategory(in.getCategory());
        return ResponseEntity.ok(service.addOrUpdateProduct(p));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id)
    {
        return ResponseEntity.ok(service.deleteProduct(id));
    }

    @GetMapping("/seller/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ArrayList<Product>> products(Authentication auth)
    {
        return ResponseEntity.ok(service.findBySellerName(auth.getName()));
    }

    @PostMapping("/products/{id}/image")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public Map<String, String> upload(@PathVariable Long id,
                                      @RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        Product p = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !p.getSellerName().equals(auth.getName()))
            throw new AccessDeniedException("Not your product");
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new RuntimeException("Please upload an image file");

        p.setImageName(file.getOriginalFilename());
        p.setImageType(file.getContentType());
        p.setImageData(file.getBytes());
        productRepo.save(p);
        return Map.of("message", "Image uploaded");
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        Product p = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        if (p.getImageData() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(p.getImageType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(p.getImageData());
    }

}
