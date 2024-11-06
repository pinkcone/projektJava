package com.pollub.cookie.service;

import com.pollub.cookie.dto.CategoryDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.Category;
import com.pollub.cookie.model.Product;
import com.pollub.cookie.repository.CategoryRepository;
import com.pollub.cookie.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Tworzy nową kategorię na podstawie CategoryDTO.
     *
     * @param categoryDTO Dane kategorii
     * @return Utworzona kategoria jako CategoryDTO
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = mapToEntity(categoryDTO);
        List<Product> produkty = mapProduktyIdsToEntities(categoryDTO.getProduktyIds());
        category.setProdukty(produkty);


        produkty.forEach(product -> product.getKategorie().add(category));

        Category savedCategory = categoryRepository.save(category);


        productRepository.saveAll(produkty);

        return mapToDTO(savedCategory);
    }

    /**
     * Importuje kategorie z JSON-a.
     *
     * @param categoryDTOs Lista kategorii do importu
     * @return Lista zaimportowanych kategorii jako CategoryDTO
     */
    @Transactional
    public List<CategoryDTO> importCategoriesFromJson(List<CategoryDTO> categoryDTOs) {
        List<Category> categories = categoryDTOs.stream()
                .map(this::mapToEntityWithProducts)
                .collect(Collectors.toList());

        List<Category> savedCategories = categoryRepository.saveAll(categories);


        for (Category category : savedCategories) {
            List<Product> produkty = category.getProdukty();
            produkty.forEach(product -> product.getKategorie().add(category));
        }


        List<Product> allProducts = savedCategories.stream()
                .flatMap(category -> category.getProdukty().stream())
                .collect(Collectors.toList());
        productRepository.saveAll(allProducts);

        return savedCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera kategorię po ID.
     *
     * @param id ID kategorii
     * @return Kategoria jako CategoryDTO
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria nie znaleziona o ID: " + id));
        return mapToDTO(category);
    }

    /**
     * Pobiera wszystkie kategorie.
     *
     * @return Lista kategorii jako CategoryDTO
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Aktualizuje istniejącą kategorię.
     *
     * @param id          ID kategorii do aktualizacji
     * @param categoryDTO Nowe dane kategorii
     * @return Zaktualizowana kategoria jako CategoryDTO
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria nie znaleziona o ID: " + id));

        existingCategory.setNazwa(categoryDTO.getNazwa());
        existingCategory.setOpis(categoryDTO.getOpis());


        existingCategory.getProdukty().forEach(product -> product.getKategorie().remove(existingCategory));
        existingCategory.getProdukty().clear();

        List<Product> noweProdukty = mapProduktyIdsToEntities(categoryDTO.getProduktyIds());
        existingCategory.setProdukty(noweProdukty);


        noweProdukty.forEach(product -> product.getKategorie().add(existingCategory));

        Category updatedCategory = categoryRepository.save(existingCategory);


        productRepository.saveAll(noweProdukty);

        return mapToDTO(updatedCategory);
    }

    /**
     * Usuwa kategorię po ID.
     *
     * @param id ID kategorii do usunięcia
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria nie znaleziona o ID: " + id));


        category.getProdukty().forEach(product -> product.getKategorie().remove(category));

        categoryRepository.deleteById(id);


        productRepository.saveAll(category.getProdukty());
    }

    /**
     * Mapuje CategoryDTO na encję Category.
     *
     * @param categoryDTO Dane kategorii
     * @return Encja Category
     */
    private Category mapToEntity(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setNazwa(categoryDTO.getNazwa());
        category.setOpis(categoryDTO.getOpis());
        return category;
    }

    /**
     * Mapuje encję Category na CategoryDTO.
     *
     * @param category Encja Category
     * @return CategoryDTO
     */
    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setNazwa(category.getNazwa());
        categoryDTO.setOpis(category.getOpis());

        List<Long> produktyIds = category.getProdukty() != null
                ? category.getProdukty().stream()
                .map(Product::getId)
                .collect(Collectors.toList())
                : new ArrayList<>();
        categoryDTO.setProduktyIds(produktyIds);

        return categoryDTO;
    }

    /**
     * Mapuje listę ID produktów na encje Product.
     *
     * @param produktyIds Lista ID produktów
     * @return Lista encji Product
     */
    private List<Product> mapProduktyIdsToEntities(List<Long> produktyIds) {
        if (produktyIds == null || produktyIds.isEmpty()) {
            return new ArrayList<>();
        }

        return productRepository.findAllById(produktyIds);
    }

    /**
     * Mapuje CategoryDTO na encję Category z przypisanymi produktami.
     *
     * @param categoryDTO Dane kategorii
     * @return Encja Category
     */
    private Category mapToEntityWithProducts(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setNazwa(categoryDTO.getNazwa());
        category.setOpis(categoryDTO.getOpis());

        List<Product> produkty = mapProduktyIdsToEntities(categoryDTO.getProduktyIds());
        category.setProdukty(produkty);

        return category;
    }
}
