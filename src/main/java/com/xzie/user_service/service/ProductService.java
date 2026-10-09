package com.xzie.user_service.service;


import com.xzie.user_service.dao.ProductRepo;
import com.xzie.user_service.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService
{
    @Autowired
    ProductRepo repo;


    public List<Product> getAll() {
        return repo.findAll();
    }

    public Optional<Product> getId(Long id) {
        return repo.findById(id);
    }

    public Product addOrUpdateProduct(Product product) {
        return repo.save(product);
    }


    public Product deleteProduct(Long id) {
        Product product = repo.findById(id).orElse(null);
        if(product != null)
            repo.deleteById(id);
        return product;
    }

    public ArrayList<Product> findBySellerName(String name) {
        return repo.findBySellerName(name);
    }
}
