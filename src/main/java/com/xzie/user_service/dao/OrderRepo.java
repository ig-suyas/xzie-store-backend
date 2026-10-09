package com.xzie.user_service.dao;

import com.xzie.user_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByUsernameOrderByIdDesc(String username);
    List<Order> findAllByOrderByIdDesc();

    @Query("select distinct o from Order o join o.items i where i.sellerName = :seller order by o.id desc")
    List<Order> findForSeller(@Param("seller") String seller);
}
