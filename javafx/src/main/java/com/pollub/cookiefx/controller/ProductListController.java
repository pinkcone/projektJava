package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pollub.cookiefx.dto.ProductDTO;
import com.pollub.cookiefx.dto.ProductExportDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import javafx.application.Platform;
import com.pollub.cookiefx.dto.CategoryDTO;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductListController {

    @FXML
    private TableView<ProductDTO> productTableView;

    @FXML
    private TableColumn<ProductDTO, Long> idColumn;

    @FXML
    private TableColumn<ProductDTO, String> nameColumn;

    @FXML
    private TableColumn<ProductDTO, Void> editColumn;

    @FXML
    private TableColumn<ProductDTO, Void> deleteColumn;

    private final ObservableList<ProductDTO> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nazwa"));

        addEditButtonToTable();
        addDeleteButtonToTable();

        fetchProducts();
    }
    private void sendJsonToBackend(String jsonContent) {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost postRequest = new HttpPost("http://localhost:8080/api/products/import/json");
                postRequest.setHeader("Content-Type", "application/json");

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    postRequest.setHeader("Authorization", "Bearer " + token);
                }

                StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);
                postRequest.setEntity(entity);
                System.out.println(entity);
                try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                    int statusCode = response.getCode();


                    if (statusCode == 201 || statusCode == 200) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Produkty zostały zaimportowane pomyślnie.");
                            fetchProducts();
                        });
                    } else {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się zaimportować produktów. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas importowania produktów." +e.getMessage()));
            }
        }).start();
    }

    @FXML
    void handleImportFromJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON z produktami");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        Stage stage = (Stage) productTableView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                String jsonContent = Files.readString(selectedFile.toPath());

                sendJsonToBackend(jsonContent);

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się odczytać pliku JSON." + e.getMessage());
            }
        }
    }

    private void fetchProducts() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet("http://localhost:8080/api/products");

            String token = Session.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                getRequest.setHeader("Authorization", "Bearer " + token);
            }

            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<ProductDTO> products = objectMapper.readValue(responseBody, new TypeReference<>() {
                    });
                    productList.setAll(products);
                    productTableView.setItems(productList);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać produktów. Status: " + statusCode + "\n" + responseBody);
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania produktów." + e.getMessage());
        }
    }

    private void addEditButtonToTable() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edytuj");

            {
                editButton.setOnAction(event -> {
                    ProductDTO product = getTableView().getItems().get(getIndex());
                    openEditProductWindow(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
    }

    private void addDeleteButtonToTable() {
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Usuń");

            {
                deleteButton.setOnAction(event -> {
                    ProductDTO product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void openEditProductWindow(ProductDTO product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProductView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edycja Produktu");

            EditProductController controller = loader.getController();
            controller.setProduct(product);
            controller.setRefreshCallback(this::fetchProducts);

            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć okna edycji produktu." + e.getMessage());
        }
    }

    private void deleteProduct(ProductDTO product) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Potwierdzenie Usunięcia");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Czy na pewno chcesz usunąć produkt: " + product.getNazwa() + "?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String deleteUrl = "http://localhost:8080/api/products/" + product.getId();
                HttpDelete deleteRequest = new HttpDelete(deleteUrl);

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    deleteRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (CloseableHttpResponse response = httpClient.execute(deleteRequest)) {
                    int statusCode = response.getCode();

                    if (statusCode == 204) {
                        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Produkt został usunięty.");
                        fetchProducts();
                    } else {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć produktu. Status: " + statusCode + "\n" + responseBody);
                    }
                }

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas usuwania produktu." + e.getMessage());
            }
        }
    }

    @FXML
    void handleDodajProdukt() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddProductView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Dodaj Produkt");

            AddProductController controller = loader.getController();
            controller.setRefreshCallback(this::fetchProducts);

            stage.showAndWait();

            fetchProducts();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć okna dodawania produktu." +e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    void handleExportToJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz produkty jako JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("produkty.json");
        Stage stage = (Stage) productTableView.getScene().getWindow();
        File saveFile = fileChooser.showSaveDialog(stage);
        if (saveFile != null) {
            try {
                List<ProductDTO> products = fetchAllProductsFromBackend();
                if (products != null) {
                    List<ProductExportDTO> exportProducts = products.stream().map(product -> {
                        ProductExportDTO exportProduct = new ProductExportDTO();
                        exportProduct.setNazwa(product.getNazwa());
                        exportProduct.setOpis(product.getOpis());
                        exportProduct.setCena(product.getCena());
                        exportProduct.setGramatura(product.getGramatura());
                        exportProduct.setIloscNaStanie(product.getIloscNaStanie());

                        List<Long> categoryIds = product.getKategorieIds().stream()
                                .map(CategoryDTO::getId)
                                .collect(Collectors.toList());
                        exportProduct.setKategorieIds(categoryIds);

                        exportProduct.setZdjecieUrl(product.getZdjecieUrl());
                        return exportProduct;
                    }).collect(Collectors.toList());

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportProducts);

                    try (FileWriter fileWriter = new FileWriter(saveFile)) {
                        fileWriter.write(jsonContent);
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Sukces", "Produkty zostały wyeksportowane do pliku JSON.");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się zapisać pliku JSON." + e.getMessage());
            }
        }
    }
    private List<ProductDTO> fetchAllProductsFromBackend() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet("http://localhost:8080/api/products");

            String token = Session.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                getRequest.setHeader("Authorization", "Bearer " + token);
            }

            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue(responseBody, new TypeReference<>() {
                    });
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać produktów. Status: " + statusCode + "\n" + responseBody));
                    return null;
                }
            }

        } catch (Exception e) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania produktów." + e.getMessage()));
            return null;
        }
    }
}
