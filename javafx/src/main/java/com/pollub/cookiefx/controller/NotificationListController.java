package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pollub.cookiefx.dto.NotificationDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationListController {

    @FXML
    private TableView<NotificationDTO> notificationTableView;

    @FXML
    private TableColumn<NotificationDTO, Long> idColumn;

    @FXML
    private TableColumn<NotificationDTO, String> contentColumn;

    @FXML
    private TableColumn<NotificationDTO, LocalDateTime> createdAtColumn;

    @FXML
    private TableColumn<NotificationDTO, Boolean> readColumn;

    private final ObservableList<NotificationDTO> notificationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("tresc"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("dataUtworzenia"));
        readColumn.setCellValueFactory(new PropertyValueFactory<>("przeczytane"));

        TableColumn<NotificationDTO, Void> actionColumn = new TableColumn<>("Akcje");
        actionColumn.setPrefWidth(150);

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button markAsReadButton = new Button("Oznacz jako przeczytane");

            {
                markAsReadButton.setOnAction(event -> {
                    NotificationDTO notification = getTableView().getItems().get(getIndex());
                    if (!notification.isPrzeczytane()) {
                        markAsRead(notification.getId());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    NotificationDTO notification = getTableView().getItems().get(getIndex());
                    markAsReadButton.setDisable(notification.isPrzeczytane());
                    setGraphic(markAsReadButton);
                }
            }
        });

        notificationTableView.getColumns().add(actionColumn);

        Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(60), event -> fetchNotifications()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        fetchNotifications();
    }

    @FXML
    private void fetchNotifications() {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet getRequest = new HttpGet("http://localhost:8080/api/notifications");
                String token = getToken();
                if (token != null && !token.isEmpty()) {
                    getRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (var response = httpClient.execute(getRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 200) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.registerModule(new JavaTimeModule());
                        List<NotificationDTO> notifications = objectMapper.readValue(responseBody, new TypeReference<>() {});
                        Platform.runLater(() -> {
                            notificationList.setAll(notifications);
                            notificationTableView.setItems(notificationList);
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać powiadomień. Status: " + statusCode + "\n" + responseBody));
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania powiadomień." + e.getMessage()));
            }
        }).start();
    }

    private void markAsRead(Long notificationId) {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String markAsReadUrl = "http://localhost:8080/api/notifications/" + notificationId + "/markAsRead";
                HttpPost postRequest = new HttpPost(markAsReadUrl);

                postRequest.setEntity(new StringEntity("", ContentType.APPLICATION_JSON));

                String token = getToken();
                if (token != null && !token.isEmpty()) {
                    postRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (var response = httpClient.execute(postRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 200) {
                        Platform.runLater(() -> {
                            for (NotificationDTO notification : notificationList) {
                                if (notification.getId().equals(notificationId)) {
                                    notification.setPrzeczytane(true);
                                    notificationTableView.refresh();
                                    break;
                                }
                            }
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Powiadomienie zostało oznaczone jako przeczytane.");
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas oznaczania powiadomienia jako przeczytane. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (IOException | ParseException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas oznaczania powiadomienia jako przeczytane." + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void markAllAsRead() {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String markAllUrl = "http://localhost:8080/api/notifications/markAllAsRead";
                HttpPost postRequest = new HttpPost(markAllUrl);

                postRequest.setEntity(new StringEntity("", ContentType.APPLICATION_JSON));

                String token = getToken();
                if (token != null && !token.isEmpty()) {
                    postRequest.setHeader("Authorization", "Bearer " + token);
                }

                try (var response = httpClient.execute(postRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 200) {
                        Platform.runLater(() -> {
                            notificationList.forEach(notification -> notification.setPrzeczytane(true));
                            notificationTableView.refresh();
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Wszystkie powiadomienia zostały oznaczone jako przeczytane.");
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się oznaczyć powiadomień jako przeczytane. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (IOException | ParseException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas oznaczania powiadomień jako przeczytane." + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) notificationTableView.getScene().getWindow();
        stage.close();
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
