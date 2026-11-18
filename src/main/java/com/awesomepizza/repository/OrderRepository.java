package com.awesomepizza.repository;

import com.awesomepizza.domain.OrderStatus;
import com.awesomepizza.domain.PizzaOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<PizzaOrder, Long> {

    Optional<PizzaOrder> findByCode(String code);

    boolean existsByCode(String code);

    Optional<PizzaOrder> findFirstByStatusOrderByIdAsc(OrderStatus status);

    List<PizzaOrder> findByStatusOrderByIdAsc(OrderStatus status);

}
