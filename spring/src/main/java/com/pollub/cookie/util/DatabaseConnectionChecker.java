package com.pollub.cookie.util;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionChecker {

    private final DataSource dataSource;

    public DatabaseConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void checkConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                System.out.println("Połączenie z bazą danych zostało nawiązane pomyślnie.");
            } else {
                System.out.println("Nie udało się nawiązać połączenia z bazą danych.");
            }
        } catch (Exception e) {
            System.out.println("Błąd podczas nawiązywania połączenia z bazą danych: " + e.getMessage());
        }
    }
}
