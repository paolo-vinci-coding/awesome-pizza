package com.awesomepizza.repository;

import com.awesomepizza.domain.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    List<Pizza> findAllByOrderByNameAsc();
}
