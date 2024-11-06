package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pollub.cookiefx.dto.NotificationDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

import lombok.Setter;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class AdminPanelController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab notificationsTab;

    @FXML
    private Label welcomeLabel;

    private int unreadNotificationsCount = 0;

    private Timeline notificationChecker;

    @Setter
    private String token;


    @FXML
    public void initialize() {
        welcomeLabel.setText("Witaj, administrator!");

        notificationChecker = new Timeline(new KeyFrame(Duration.seconds(60), event -> checkForNewNotifications()));
        notificationChecker.setCycleCount(Timeline.INDEFINITE);
        notificationChecker.play();

        checkForNewNotifications();

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == notificationsTab) {
                clearTabHighlight();
            }
        });
    }

    @FXML
    void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 250));
            stage.setTitle("Logowanie");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się wylogować." + e.getMessage());
        }
    }

    private void checkForNewNotifications() {
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

                        long count = notifications.stream().filter(n -> !n.isPrzeczytane()).count();

                        Platform.runLater(() -> {
                            if (count > 0) {
                                if (unreadNotificationsCount < count) {
                                    unreadNotificationsCount = (int) count;
                                    highlightNotificationsTab();
                                }
                            } else {
                                clearTabHighlight();
                            }
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

    private void highlightNotificationsTab() {
        notificationsTab.getStyleClass().add("tab-highlighted");

        Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> notificationsTab.getStyleClass().add("tab-highlighted")),
                new KeyFrame(Duration.seconds(0.5), e -> notificationsTab.getStyleClass().remove("tab-highlighted")),
                new KeyFrame(Duration.seconds(1), e -> notificationsTab.getStyleClass().add("tab-highlighted")),
                new KeyFrame(Duration.seconds(1.5), e -> notificationsTab.getStyleClass().remove("tab-highlighted"))
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        blinkTimeline.play();

        notificationsTab.setUserData(blinkTimeline);
    }

    private void clearTabHighlight() {
        notificationsTab.getStyleClass().remove("tab-highlighted");

        Object userData = notificationsTab.getUserData();
        if (userData instanceof Timeline) {
            ((Timeline) userData).stop();
            notificationsTab.setUserData(null);
        }

        unreadNotificationsCount = 0;
    }

    private String getToken() {
        return Session.getInstance().getToken();
    }

    private void showAlert(Alert.AlertType error, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

}
