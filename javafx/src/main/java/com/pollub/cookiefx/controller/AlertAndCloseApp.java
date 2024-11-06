package com.pollub.cookiefx.controller;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AlertAndCloseApp {


    public static void start() {
        // Utworzenie i konfiguracja alertu
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Błąd Logowania");
        alert.setHeaderText(null); // Usunięcie nagłówka
        alert.setContentText("Co tu robisz? Nie jesteś administratorem.");
        alert.show(); // Wyświetlenie alertu bez blokowania

        // Utworzenie PauseTransition na 30 sekund
        PauseTransition delay = new PauseTransition(Duration.seconds(10));
        delay.setOnFinished(event -> {
            // Zamknięcie aplikacji po upływie 30 sekund
            Platform.exit();
        });
        delay.play(); // Rozpoczęcie odliczania
    }

}
