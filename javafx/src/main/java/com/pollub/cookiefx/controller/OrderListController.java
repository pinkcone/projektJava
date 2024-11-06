package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pollub.cookiefx.dto.OrderDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OrderListController {
    @FXML
    private TableView<OrderDTO> orderTableView;

    @FXML
    private TableColumn<OrderDTO, Long> idColumn;
    @FXML
    private TableColumn<OrderDTO, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<OrderDTO, String> statusColumn;
    @FXML
    private TableColumn<OrderDTO, Double> totalPriceColumn;
    @FXML
    private TableColumn<OrderDTO, Long> userIdColumn;
    @FXML
    private TableColumn<OrderDTO, String> addressColumn;
    @FXML
    private TableColumn<OrderDTO, String> phoneColumn;
    @FXML
    private TableColumn<OrderDTO, String> itemsColumn;

    private final ObservableList<OrderDTO> orderList = FXCollections.observableArrayList();

    private final List<String> availableStatuses = Arrays.asList(
            "NOWE",
            "W_TRAKCIE_PRZETWARZANIA",
            "WYSŁANE",
            "DOSTARCZONE",
            "ANULOWANE"
    );

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datazamowienia"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("calkowitaCena"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("uzytkownikId"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("adres"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("numerTelefonu"));

        itemsColumn.setCellValueFactory(order -> {
            String itemsDescription = order.getValue().getPozycjeZamowienia().stream()
                    .map(item -> "ID: " + item.getProduktId() +
                            ", Nazwa: " + item.getProdukt().getNazwa() +
                            ", Ilość: " + item.getIlosc())
                    .reduce((item1, item2) -> item1 + "\n" + item2)
                    .orElse("Brak produktów");
            return new SimpleStringProperty(itemsDescription);
        });

        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(availableStatuses)));

        statusColumn.setOnEditCommit(event -> {
            OrderDTO order = event.getRowValue();
            String newStatus = event.getNewValue();

            if (newStatus != null && !newStatus.equals(order.getStatus())) {
                updateOrderStatus(order.getId(), newStatus);
            }
        });

        orderTableView.setEditable(true);

        fetchOrders();
    }

    @FXML
    private void fetchOrders() {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet getRequest = new HttpGet("http://localhost:8080/api/orders");
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
                        List<OrderDTO> orders = objectMapper.readValue(responseBody, new TypeReference<>() {});
                        Platform.runLater(() -> {
                            orderList.setAll(orders);
                            orderTableView.setItems(orderList);
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać zamówień. Status: " + statusCode + "\n" + responseBody));
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania zamówień." + e.getMessage()));
            }
        }).start();
    }

    private void updateOrderStatus(Long orderId, String newStatus) {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String updateUrl = "http://localhost:8080/api/orders/" + orderId + "/status";
                HttpPut updateRequest = new HttpPut(updateUrl);

                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(Map.of("status", newStatus));

                updateRequest.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(json, ContentType.APPLICATION_JSON));

                String token = getToken();
                if (token != null && !token.isEmpty()) {
                    updateRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (CloseableHttpResponse response = httpClient.execute(updateRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 200) {
                        Platform.runLater(() -> {
                            for (OrderDTO order : orderList) {
                                if (order.getId().equals(orderId)) {
                                    order.setStatus(newStatus);
                                    orderTableView.refresh();
                                    break;
                                }
                            }
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Status zamówienia został zaktualizowany pomyślnie.");
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas aktualizacji statusu. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas aktualizacji statusu." + e.getMessage()));
            }
        }).start();
    }

    private String getToken() {
        return Session.getInstance().getToken();
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
