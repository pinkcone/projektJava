package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollub.cookiefx.utill.JwtDecoder;
import com.pollub.cookiefx.utill.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
@Setter
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private String token;

    @FXML
    void handleLogin() {
        String email = emailField.getText();
        String haslo = passwordField.getText();

        if (email.isEmpty() || haslo.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Email i hasło nie mogą być puste.");
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AuthRequest authRequest = new AuthRequest(email, haslo);
            String json = objectMapper.writeValueAsString(authRequest);

            System.out.println("Wysyłanie żądania logowania: " + json);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://localhost:8080/api/auth/login")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Otrzymano odpowiedź: " + response.statusCode());

            if (response.statusCode() == 200) {

                    AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
                    this.token = authResponse.getToken();
                    Session.getInstance().setToken(token);
                JwtDecoder.printDecodedPayload(token);
                List<String> roles = JwtDecoder.getUserRoles(token);
                if(roles.contains("ROLE_ADMIN")) {

                    showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zalogowano pomyślnie.");



                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminPanelView.fxml"));
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(loader.load(), 800, 600);
                    stage.setScene(scene);
                    stage.setTitle("Panel Administratora");

                    AdminPanelController adminController = loader.getController();
                    adminController.setToken(token);

                    stage.show();
                }else{AlertAndCloseApp.start();}
            } else if (response.statusCode() == 401) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawny email lub hasło.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas logowania. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas logowania: " + e.getMessage());
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

}
