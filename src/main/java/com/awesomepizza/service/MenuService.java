package com.awesomepizza.service;

import com.awesomepizza.repository.PizzaRepository;
import com.awesomepizza.web.dto.PizzaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    private final PizzaRepository pizzaRepository;

    public MenuService(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    @Transactional(readOnly = true)
    public List<PizzaResponse> getMenu() {
        return pizzaRepository.findAllByOrderByNameAsc().stream()
                .map(PizzaResponse::from)
                .toList();
    }
}
