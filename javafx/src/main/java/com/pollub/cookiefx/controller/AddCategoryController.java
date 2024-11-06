package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollub.cookiefx.dto.CategoryDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class AddCategoryController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @Setter
    private Runnable refreshCallback;

    @FXML
    void handleSaveCategory() {
        String nazwa = nameField.getText().trim();
        String opis = descriptionArea.getText().trim();

        if (nazwa.isEmpty() || opis.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nazwa i opis kategorii są wymagane.");
            return;
        }

        CategoryDTO category = new CategoryDTO();
        category.setNazwa(nazwa);
        category.setOpis(opis);

        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost postRequest = new HttpPost("http://localhost:8080/api/categories");
                postRequest.setHeader("Content-Type", "application/json");

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    postRequest.setHeader("Authorization", "Bearer " + token);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(category);

                StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                postRequest.setEntity(entity);

                try (var response = httpClient.execute(postRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 201) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kategoria została dodana.");
                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }
                            closeWindow();
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się dodać kategorii. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {

                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas dodawania kategorii." + e));
            }
        }).start();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
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
