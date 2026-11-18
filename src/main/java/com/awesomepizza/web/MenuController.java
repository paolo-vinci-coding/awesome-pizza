package com.awesomepizza.web;

import com.awesomepizza.service.MenuService;
import com.awesomepizza.web.dto.PizzaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    @Operation(summary = "List of pizzas available to order")
    public List<PizzaResponse> getMenu() {
        return menuService.getMenu();
    }
}
