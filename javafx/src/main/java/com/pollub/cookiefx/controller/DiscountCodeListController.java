package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pollub.cookiefx.dto.DiscountCodeDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DiscountCodeListController {

    @FXML
    private TableView<DiscountCodeDTO> discountCodeTableView;

    @FXML
    private TableColumn<DiscountCodeDTO, Long> idColumn;

    @FXML
    private TableColumn<DiscountCodeDTO, String> codeColumn;

    @FXML
    private TableColumn<DiscountCodeDTO, String> typeColumn;

    @FXML
    private TableColumn<DiscountCodeDTO, Double> valueColumn;

    @FXML
    private TableColumn<DiscountCodeDTO, LocalDate> expirationColumn;

    @FXML
    private TableColumn<DiscountCodeDTO, Void> editColumn;

    @FXML
    private TableColumn<DiscountCodeDTO, Void> deleteColumn;

    private final ObservableList<DiscountCodeDTO> discountCodeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("kod"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typ"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("wartosc"));
        expirationColumn.setCellValueFactory(new PropertyValueFactory<>("dataWaznosci"));

        addEditButtonToTable();
        addDeleteButtonToTable();

        fetchDiscountCodes();
    }

    private void fetchDiscountCodes() {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet getRequest = new HttpGet("http://localhost:8080/api/discount-codes");

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    getRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (var response = httpClient.execute(getRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 200) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.registerModule(new JavaTimeModule());
                        List<DiscountCodeDTO> discountCodes = objectMapper.readValue(responseBody, new TypeReference<>() {
                        });
                        Platform.runLater(() -> {
                            discountCodeList.setAll(discountCodes);
                            discountCodeTableView.setItems(discountCodeList);
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać kodów rabatowych. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania kodów rabatowych." + e.getMessage()));
            }
        }).start();
    }

    private void addEditButtonToTable() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edytuj");

            {
                editButton.setOnAction(event -> {
                    DiscountCodeDTO discountCode = getTableView().getItems().get(getIndex());
                    openEditDiscountCodeWindow(discountCode);
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
                    DiscountCodeDTO discountCode = getTableView().getItems().get(getIndex());
                    deleteDiscountCode(discountCode);
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

    private void openEditDiscountCodeWindow(DiscountCodeDTO discountCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditDiscountCodeView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edycja Kodu Rabatowego");

            EditDiscountCodeController controller = loader.getController();
            controller.setDiscountCode(discountCode);
            controller.setRefreshCallback(this::fetchDiscountCodes);

            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć okna edycji kodu rabatowego." + e.getMessage());
        }
    }

    private void deleteDiscountCode(DiscountCodeDTO discountCode) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Potwierdzenie Usunięcia");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Czy na pewno chcesz usunąć kod rabatowy: " + discountCode.getKod() + "?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    String deleteUrl = "http://localhost:8080/api/discount-codes/" + discountCode.getId();
                    HttpDelete deleteRequest = new HttpDelete(deleteUrl);

                    String token = Session.getInstance().getToken();
                    if (token != null && !token.isEmpty()) {
                        deleteRequest.setHeader("Authorization", "Bearer " + token);
                    }

                    try (var response = httpClient.execute(deleteRequest)) {
                        int statusCode = response.getCode();


                        if (statusCode == 204) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kod rabatowy został usunięty.");
                                fetchDiscountCodes();
                            });
                        } else {
                            String responseBody = EntityUtils.toString(response.getEntity());
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć kodu rabatowego. Status: " + statusCode + "\n" + responseBody));
                        }
                    }

                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas usuwania kodu rabatowego." +e.getMessage()));
                }
            }).start();
        }
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

    public void handleAddDiscountCode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddDiscountCodeView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Dodaj Kategorię");

            AddDiscountCodeController controller = loader.getController();
            controller.setRefreshCallback(this::fetchDiscountCodes);

            stage.showAndWait();

            fetchDiscountCodes();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć okna dodawania kategorii." +e.getMessage());
        }
    }
}

