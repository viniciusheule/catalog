package com.projeto.catalog.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.projeto.catalog.dto.ProductDTO;
import com.projeto.catalog.entities.Category;
import com.projeto.catalog.entities.Product;
import com.projeto.catalog.repositories.CategoryRepository;
import com.projeto.catalog.repositories.ProductRepository;
import com.projeto.catalog.services.exceptions.DatabaseException;
import com.projeto.catalog.services.exceptions.ResourceNotFoundException;
import com.projeto.catalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository repository;

	@Mock
	private CategoryRepository categoryRepository;

	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private Category category;
	private ProductDTO productDTO;

	@SuppressWarnings("deprecation")
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(product));

		when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

		when(repository.save(ArgumentMatchers.any())).thenReturn(product);

		when(repository.findById(existingId)).thenReturn(Optional.of(product));
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		when(repository.getOne(existingId)).thenReturn(product);
		when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

		when(categoryRepository.getOne(existingId)).thenReturn(category);
		when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

		doNothing().when(repository).deleteById(existingId);

		doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);

		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}

	@Test
	public void updateShouldReturnProductDTOWhenIdExists() {

		ProductDTO result = service.update(existingId, productDTO);

		Assertions.assertNotNull(result);
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, productDTO);
		});
	}

	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {

		ProductDTO result = service.findById(existingId);

		Assertions.assertNotNull(result);
	}

	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}

	@Test
	public void findAllPagedShouldReturnPage() {

		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> result = service.findAllPaged(pageable);

		Assertions.assertNotNull(result);

		verify(repository, times(1)).findAll(pageable);

	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});

		verify(repository, times(1)).deleteById(existingId);
	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});

		verify(repository, times(1)).deleteById(nonExistingId);
	}

	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});

		verify(repository, times(1)).deleteById(dependentId);
	}

}
