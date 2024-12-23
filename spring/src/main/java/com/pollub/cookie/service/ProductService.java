package com.pollub.cookie.service;

import com.pollub.cookie.dto.CategoryDTO;
import com.pollub.cookie.dto.ProductCreateDTO;
import com.pollub.cookie.dto.ProductDTO;
import com.pollub.cookie.dto.ProductImportJsonDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.Category;
import com.pollub.cookie.model.Product;
import com.pollub.cookie.repository.CategoryRepository;
import com.pollub.cookie.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Tworzy nowy produkt na podstawie ProductDTO i pliku zdjęcia.
     *
     * @param productCreateDTO  Dane produktu
     * @param zdjecieFile Plik zdjęcia
     * @return Utworzony produkt jako ProductDTO
     */
    @Transactional
    public ProductDTO createProduct(ProductCreateDTO productCreateDTO, MultipartFile zdjecieFile) {
        System.out.println("Creating product with data: " + productCreateDTO);
        String zdjecieFileName = null;


        if (zdjecieFile != null && !zdjecieFile.isEmpty()) {

            String contentType = zdjecieFile.getContentType();
            assert contentType != null;
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Przesłany plik nie jest obrazem.");
            }

            if (zdjecieFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Rozmiar pliku przekracza dopuszczalny limit 5 MB.");
            }

            try {
                zdjecieFileName = saveFile(zdjecieFile);
            } catch (IOException e) {
                throw new RuntimeException("Błąd podczas zapisywania pliku: " + e.getMessage());
            }
        }

        Product product = mapToEntity(productCreateDTO, zdjecieFileName);
        List<Category> kategorie = mapKategorieIdsToEntities(productCreateDTO.getKategorieIds());
        product.setKategorie(kategorie);

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    private Product mapToEntity(ProductCreateDTO productCreateDTO, String zdjecieFileName) {
        Product product = new Product();
        product.setNazwa(productCreateDTO.getNazwa());
        product.setOpis(productCreateDTO.getOpis());
        product.setGramatura((productCreateDTO.getGramatura()));
        product.setIloscNaStanie(productCreateDTO.getIloscNaStanie());
        product.setCena(productCreateDTO.getCena());
        product.setZdjecie(zdjecieFileName); // Ustaw nazwę pliku zdjęcia
        return product;
    }

    /**
     * Zapisuje plik na serwerze i zwraca jego nazwę.
     *
     * @param file Plik do zapisania
     * @return Nazwa zapisanego pliku
     * @throws IOException Jeśli wystąpi błąd podczas zapisu pliku
     */
    private String saveFile(MultipartFile file) throws IOException {

        String uploadDir = "uploads/images/";
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            try{uploadDirFile.mkdirs();}catch (Exception e){
                System.out.println("nie udalo sie tworzyc folderu");
            }
        }


        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = UUID.randomUUID() + "." + fileExtension;


        Path filePath = Paths.get(uploadDir, newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


        return newFileName;
    }

    /**
     * Pobiera rozszerzenie pliku z nazwy pliku.
     *
     * @param fileName Nazwa pliku
     * @return Rozszerzenie pliku
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex + 1);
        }
        return extension;
    }

    /**
     * Pobiera produkt po ID.
     *
     * @param id ID produktu
     * @return Produkt jako ProductDTO
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie znaleziony o ID: " + id));
        return mapToDTO(product);
    }

    /**
     * Pobiera wszystkie produkty.
     *
     * @return Lista produktów jako ProductDTO
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Aktualizuje istniejący produkt.
     *
     * @param id         ID produktu do aktualizacji
     * @param productCreateDTO Nowe dane produktu
     * @return Zaktualizowany produkt jako ProductDTO
     */
    @Transactional
    public ProductDTO updateProduct(Long id, ProductCreateDTO productCreateDTO, MultipartFile zdjecieFile) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie znaleziony o ID: " + id));

        existingProduct.setNazwa(productCreateDTO.getNazwa());
        existingProduct.setOpis(productCreateDTO.getOpis());
        existingProduct.setGramatura(productCreateDTO.getGramatura());
        existingProduct.setIloscNaStanie(productCreateDTO.getIloscNaStanie());
        existingProduct.setCena(productCreateDTO.getCena());

        List<Category> kategorie = mapKategorieIdsToEntities(productCreateDTO.getKategorieIds());
        existingProduct.setKategorie(kategorie);

        if (zdjecieFile != null && !zdjecieFile.isEmpty()) {
            String contentType = zdjecieFile.getContentType();
            assert contentType != null;
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Przesłany plik nie jest obrazem.");
            }


            if (zdjecieFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Rozmiar pliku przekracza dopuszczalny limit 5 MB.");
            }

            try {

                String zdjecieFileName = saveFile(zdjecieFile);


                existingProduct.setZdjecie(zdjecieFileName);
            } catch (IOException e) {
                throw new RuntimeException("Błąd podczas zapisywania pliku: " + e.getMessage());
            }
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }


    /**
     * Usuwa produkt po ID.
     *
     * @param id ID produktu do usunięcia
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produkt nie znaleziony o ID: " + id);
        }
        productRepository.deleteById(id);
    }





    /**
     * Importuje listę produktów z JSON.
     *
     * @param productImportJsonDTOs Lista danych produktów do importu
     * @return Lista utworzonych produktów jako ProductDTO
     */
    @Transactional
    public List<ProductDTO> importProductsFromJson(@Valid List<ProductImportJsonDTO> productImportJsonDTOs) {
        log.info("Rozpoczynanie importu {} produktów.", productImportJsonDTOs.size());

        if (productImportJsonDTOs == null || productImportJsonDTOs.isEmpty()) {
            log.warn("Przekazano pustą listę produktów do importu.");
            return Collections.emptyList();
        }

        // Logowanie szczegółowych danych produktów przed mapowaniem
        productImportJsonDTOs.forEach(dto -> log.debug("Importowany produkt: {}", dto));

        List<Product> products = productImportJsonDTOs.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());

        log.info("Mapa produktów z DTO na encje zakończona. Zapis produktów do bazy danych.");

        try {
            products = productRepository.saveAll(products);
            log.info("Produkty zostały pomyślnie zapisane do bazy danych.");
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania produktów do bazy danych: {}", e.getMessage(), e);
            throw e;
        }

        // Logowanie zapisanych produktów
        products.forEach(product -> log.debug("Zapisany produkt: {}", product));

        List<ProductDTO> createdProducts = products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        log.info("Import produktów zakończony sukcesem. Zaimportowanych produktów: {}", createdProducts.size());

        return createdProducts;
    }

    private Product mapToEntity(ProductImportJsonDTO productImportJsonDTO) {
        log.debug("Mapowanie ProductImportJsonDTO do Product: {}", productImportJsonDTO);

        Product product = new Product();
        product.setNazwa(productImportJsonDTO.getNazwa());
        product.setOpis(productImportJsonDTO.getOpis());
        product.setCena(productImportJsonDTO.getCena());
        product.setGramatura(productImportJsonDTO.getGramatura());
        product.setIloscNaStanie(productImportJsonDTO.getIloscNaStanie());
        product.setZdjecie(productImportJsonDTO.getZdjecieUrl());

        List<Category> categories = mapKategorieIdsToEntities(productImportJsonDTO.getKategorieIds());
        product.setKategorie(categories);

        log.debug("Zmapowany produkt: {}", product);
        return product;
    }

    /**
     * Mapuje listę ID kategorii na encje Category.
     *
     * @param kategorieIds Lista ID kategorii
     * @return Lista encji Category
     */
    private List<Category> mapKategorieIdsToEntities(List<Long> kategorieIds) {
        if (kategorieIds == null || kategorieIds.isEmpty()) {
            log.debug("Lista ID kategorii jest pusta lub null. Zwracanie pustej listy kategorii.");
            return new ArrayList<>();
        }

        log.debug("Mapowanie listy ID kategorii: {}", kategorieIds);

        List<Category> categories = categoryRepository.findAllById(kategorieIds);

        if (categories.isEmpty()) {
            log.warn("Nie znaleziono kategorii dla podanych ID: {}", kategorieIds);
        } else {
            log.debug("Znalezione kategorie: {}", categories);
        }

        return categories;
    }

    /**
     * Mapuje encję Product na ProductDTO.
     *
     * @param product Encja Product
     * @return ProductDTO
     */
    private ProductDTO mapToDTO(Product product) {
        log.debug("Mapowanie Product do ProductDTO: {}", product);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setNazwa(product.getNazwa());
        productDTO.setOpis(product.getOpis());
        productDTO.setGramatura(product.getGramatura());
        productDTO.setZdjecie(product.getZdjecie());
        productDTO.setIloscNaStanie(product.getIloscNaStanie());
        productDTO.setCena(product.getCena());

        List<CategoryDTO> categoryDTOs = product.getKategorie() != null
                ? product.getKategorie().stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList())
                : new ArrayList<>();

        productDTO.setKategorie(categoryDTOs);
        log.debug("Zmapowany ProductDTO: {}", productDTO);
        return productDTO;
    }

    private CategoryDTO mapCategoryToDTO(Category category) {
        log.debug("Mapowanie Category do CategoryDTO: {}", category);

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setNazwa(category.getNazwa());
        dto.setOpis(category.getOpis());

        log.debug("Zmapowany CategoryDTO: {}", dto);
        return dto;
    }
}
