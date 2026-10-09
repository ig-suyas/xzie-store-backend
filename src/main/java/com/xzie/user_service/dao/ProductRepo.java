package com.xzie.user_service.dao;

import com.xzie.user_service.dto.ChatProduct;
import com.xzie.user_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface ProductRepo extends JpaRepository<Product,Long> {
    ArrayList<Product> findBySellerName(String name);

    //not the image this time (fast)
    @Query("select new com.xzie.user_service.dto.ChatProduct(p.id, p.name, p.price, p.stock, p.category, p.description) from Product p order by p.id")
    List<ChatProduct> findForChat();
}
