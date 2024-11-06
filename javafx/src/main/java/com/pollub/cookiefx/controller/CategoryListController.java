package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollub.cookiefx.dto.CategoryDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class CategoryListController {

    @FXML
    private TableView<CategoryDTO> categoryTableView;

    @FXML
    private TableColumn<CategoryDTO, Long> idColumn;

    @FXML
    private TableColumn<CategoryDTO, String> nameColumn;

    @FXML
    private TableColumn<CategoryDTO, String> descriptionColumn;

    @FXML
    private TableColumn<CategoryDTO, Void> editColumn;

    @FXML
    private TableColumn<CategoryDTO, Void> deleteColumn;

    private final ObservableList<CategoryDTO> categoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("opis"));

        addEditButtonToTable();
        addDeleteButtonToTable();

        fetchCategories();
    }

    private void fetchCategories() {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet getRequest = new HttpGet("http://localhost:8080/api/categories");

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    getRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (var response = httpClient.execute(getRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 200) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<CategoryDTO> categories = objectMapper.readValue(responseBody, new TypeReference<>() {
                        });
                        Platform.runLater(() -> {
                            categoryList.setAll(categories);
                            categoryTableView.setItems(categoryList);
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać kategorii. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania kategorii." + e.getMessage()));
            }
        }).start();
    }

    private void addEditButtonToTable() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edytuj");

            {
                editButton.setOnAction(event -> {
                    CategoryDTO category = getTableView().getItems().get(getIndex());
                    openEditCategoryWindow(category);
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
                    CategoryDTO category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
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

    private void openEditCategoryWindow(CategoryDTO category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditCategoryView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edycja Kategorii");

            EditCategoryController controller = loader.getController();
            controller.setCategory(category);
            controller.setRefreshCallback(this::fetchCategories);

            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć okna edycji kategorii." + e.getMessage());
        }
    }

    private void deleteCategory(CategoryDTO category) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Potwierdzenie Usunięcia");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Czy na pewno chcesz usunąć kategorię: " + category.getNazwa() + "?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    String deleteUrl = "http://localhost:8080/api/categories/" + category.getId();
                    HttpDelete deleteRequest = new HttpDelete(deleteUrl);

                    String token = Session.getInstance().getToken();
                    if (token != null && !token.isEmpty()) {
                        deleteRequest.setHeader("Authorization", "Bearer " + token);
                    }

                    try (var response = httpClient.execute(deleteRequest)) {
                        int statusCode = response.getCode();
                        if (statusCode == 204) {
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kategoria została usunięta.");
                            fetchCategories();
                        } else {
                            String responseBody = EntityUtils.toString(response.getEntity());
                            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć kategorii. Status: " + statusCode + "\n" + responseBody);
                        }


                    }

                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas usuwania kategorii." + e.getMessage()));
                }
            }).start();
        }
    }

    @FXML
    void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddCategoryView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Dodaj Kategorię");

            AddCategoryController controller = loader.getController();
            controller.setRefreshCallback(this::fetchCategories);

            stage.showAndWait();
            fetchCategories();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć okna dodawania kategorii." + e.getMessage());
        }
    }

    @FXML
    void handleImportCategories() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON z kategoriami");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        Stage stage = (Stage) categoryTableView.getScene().getWindow();
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

    @FXML
    void handleExportCategories() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz miejsce do zapisania pliku JSON z kategoriami");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        Stage stage = (Stage) categoryTableView.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            new Thread(() -> {
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet getRequest = new HttpGet("http://localhost:8080/api/categories");

                    String token = Session.getInstance().getToken();
                    if (token != null && !token.isEmpty()) {
                        getRequest.setHeader("Authorization", "Bearer " + token);
                    }

                    try (var response = httpClient.execute(getRequest)) {
                        int statusCode = response.getCode();
                        String responseBody = EntityUtils.toString(response.getEntity());

                        if (statusCode == 200) {
                            Files.writeString(selectedFile.toPath(), responseBody);
                            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kategorie zostały wyeksportowane pomyślnie."));
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać kategorii. Status: " + statusCode + "\n" + responseBody));
                        }
                    }

                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas eksportowania kategorii." + e.getMessage()));
                }
            }).start();
        }
    }

    private void sendJsonToBackend(String jsonContent) {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost postRequest = new HttpPost("http://localhost:8080/api/categories/import/json");
                postRequest.setHeader("Content-Type", "application/json");

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    postRequest.setHeader("Authorization", "Bearer " + token);
                }

                StringEntity entity = new StringEntity(jsonContent, ContentType.APPLICATION_JSON);
                postRequest.setEntity(entity);

                try (var response = httpClient.execute(postRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 201 || statusCode == 200) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kategorie zostały zaimportowane pomyślnie.");
                            fetchCategories();
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się zaimportować kategorii. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas importowania kategorii." + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
