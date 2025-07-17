package test.utpl.msvc_sales.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import test.utpl.msvc_sales.entities.Items;
import test.utpl.msvc_sales.services.ItemsService;

@RestController
@RequestMapping("/api/items")
public class ItemsController {

    private final ItemsService itemsService;

    public ItemsController(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    @GetMapping
    public List<Items> getAllItems() {
        return itemsService.getAllItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable String id) {
        System.err.println("ID DEL PATH");
        System.err.println(id);
        Optional<Items> item = itemsService.getItemById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(item.get());
        } else {
            return ResponseEntity.status(404)
                    .body(Collections.singletonMap("message", "El producto no existe en el micro de productos"));
        }
    }

}
