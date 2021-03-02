package com.example.demo.controllers;

import com.example.demo.SareetaApplication;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@RestController
@RequestMapping("/api/item")
public class ItemController {

	@Autowired
	private ItemRepository itemRepository;
	
	@GetMapping
	public ResponseEntity<List<Item>> getItems() {
		SareetaApplication.logger.info("[ItemController] Retrieving all items");
		return ResponseEntity.ok(itemRepository.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Item> getItemById(@PathVariable Long id) {
		SareetaApplication.logger.info("[ItemController] Retrieving item with id "+id);
		return ResponseEntity.of(itemRepository.findById(id));
	}
	
	@GetMapping("/name/{name}")
	public ResponseEntity<List<Item>> getItemsByName(@PathVariable String name) {
		SareetaApplication.logger.info("[ItemController] Retrieving item by name "+name);
		List<Item> items = itemRepository.findByName(name);
		if (items == null) {
			SareetaApplication.logger.warn("[ItemController] Item with name "+name+" not found!");
			return  ResponseEntity.notFound().build();
		}
		SareetaApplication.logger.info("[ItemController] Successfully retrieve item with name "+name);
		return ResponseEntity.ok(items);
	}
	
}
