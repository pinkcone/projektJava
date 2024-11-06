package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pollub.cookiefx.dto.DiscountCodeDTO;
import com.pollub.cookiefx.enums.DiscountType;
import com.pollub.cookiefx.utill.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.LocalDate;

public class AddDiscountCodeController {

    @FXML
    private TextField codeField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField valueField;

    @FXML
    private DatePicker expirationDatePicker;

    @Setter
    private Runnable refreshCallback;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                DiscountType.PERCENTAGE.name(),
                DiscountType.FIXED_AMOUNT.name()
        ));
    }

    @FXML
    void handleSaveDiscountCode() {
        String kod = codeField.getText().trim();
        String typ = typeComboBox.getValue();
        String wartoscStr = valueField.getText().trim();
        LocalDate dataWaznosci = expirationDatePicker.getValue();

        if (kod.isEmpty() || typ == null || typ.isEmpty() || wartoscStr.isEmpty() || dataWaznosci == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wszystkie pola są wymagane.");
            return;
        }

        double wartosc;
        try {
            wartosc = Double.parseDouble(wartoscStr);
            if (wartosc <= 0) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Wartość rabatu musi być dodatnia.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wartość rabatu musi być liczbą.");
            return;
        }

        if (!dataWaznosci.isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Data ważności rabatu musi być przyszła.");
            return;
        }

        DiscountCodeDTO discountCode = new DiscountCodeDTO();
        discountCode.setKod(kod);
        discountCode.setTyp(typ);
        discountCode.setWartosc(wartosc);
        discountCode.setDataWaznosci(dataWaznosci);

        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost postRequest = new HttpPost("http://localhost:8080/api/discount-codes");
                postRequest.setHeader("Content-Type", "application/json");

                String token = Session.getInstance().getToken();
                if (token != null && !token.isEmpty()) {
                    postRequest.setHeader("Authorization", "Bearer " + token);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                String json = objectMapper.writeValueAsString(discountCode);

                StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                postRequest.setEntity(entity);

                try (var response = httpClient.execute(postRequest)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode == 201) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kod rabatowy został dodany.");
                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }
                            closeWindow();
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się dodać kodu rabatowego. Status: " + statusCode + "\n" + responseBody));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas dodawania kodu rabatowego." + e));
            }
        }).start();
    }

    private void closeWindow() {
        Stage stage = (Stage) codeField.getScene().getWindow();
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
