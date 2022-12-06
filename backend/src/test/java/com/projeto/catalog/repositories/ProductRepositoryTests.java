package com.projeto.catalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.projeto.catalog.entities.Product;
import com.projeto.catalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repository;
	private long exintingId;
	private long nonExintingId;
	private long countTotalProducts;

	@BeforeEach
	void setUp() throws Exception {
		exintingId = 1L;
		nonExintingId = 1000L;
		countTotalProducts = 25L;
	}

	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		Product product = Factory.createProduct();
		product.setId(null);

		product = repository.save(product);

		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());

	}

	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {

		repository.deleteById(exintingId);

		Optional<Product> result = repository.findById(exintingId);

		Assertions.assertFalse(result.isPresent());
	}

	@Test
	public void deleteShouldThrowEmptyResultDataAccessExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExintingId);
		});

	}

	@Test
	public void findByIdShouldReturnNonEmptyOptionalWhenIdExists() {

		Optional<Product> result = repository.findById(exintingId);
		
		Assertions.assertTrue(result.isPresent());
	}
	
	@Test
	public void findByIdShouldReturnEmptyOptionalWhenIdDoesNotExist() {

		Optional<Product> result = repository.findById(nonExintingId);
		
		Assertions.assertTrue(result.isEmpty());
	}
}
