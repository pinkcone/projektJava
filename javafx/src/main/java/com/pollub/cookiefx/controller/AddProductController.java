package com.pollub.cookiefx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollub.cookiefx.dto.CategoryDTO;
import com.pollub.cookiefx.dto.ProductCreateDTO;
import com.pollub.cookiefx.utill.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AddProductController {

    @FXML
    private TextField nazwaField;

    @FXML
    private TextArea opisArea;

    @FXML
    private TextField cenaField;

    @FXML
    private TextField gramaturaField;

    @FXML
    private TextField iloscNaStanieField;

    @FXML
    private ListView<CheckBox> kategorieListView;

    @FXML
    private Button wybierzZdjecieButton;

    @FXML
    private Label wybraneZdjecieLabel;

    private File wybraneZdjecie;

    @Setter
    private String token;


    @Setter
    private Runnable refreshCallback;

    @FXML
    public void initialize() {
        fetchCategories();
    }

    private void fetchCategories() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet("http://localhost:8080/api/categories");

            String token = getToken();
            if (token != null && !token.isEmpty()) {
                getRequest.setHeader("Authorization", "Bearer " + token);
            }

            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<CategoryDTO> categories;
                    categories = objectMapper.readValue(responseBody, new TypeReference<>() {
                    });

                    ObservableList<CheckBox> items = FXCollections.observableArrayList();
                    for (CategoryDTO category : categories) {
                        CheckBox checkBox = new CheckBox(category.getNazwa());
                        checkBox.setUserData(category.getId());
                        items.add(checkBox);
                    }

                    kategorieListView.setItems(items);

                    kategorieListView.setCellFactory(param -> new ListCell<>() {
                        @Override
                        protected void updateItem(CheckBox item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                            } else {
                                setGraphic(item);
                            }
                        }
                    });

                } else {
                    showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się pobrać kategorii. Status: " + statusCode + "\n" + responseBody);
                }
            }

        } catch (Exception e) {

            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas pobierania kategorii." + e.getMessage());
        }
    }

    private String getToken() {
        return Session.getInstance().getToken();
    }

    @FXML
    void handleWybierzZdjecie() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz zdjęcie produktu");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) wybierzZdjecieButton.getScene().getWindow();
        wybraneZdjecie = fileChooser.showOpenDialog(stage);
        if (wybraneZdjecie != null) {
            wybraneZdjecieLabel.setText(wybraneZdjecie.getName());
        }
    }

    @FXML
    void handleDodajProdukt() {
        String nazwa = nazwaField.getText();
        String opis = opisArea.getText();
        String cenaStr = cenaField.getText();
        String gramaturaStr = gramaturaField.getText();
        String iloscNaStanieStr = iloscNaStanieField.getText();

        if (nazwa.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nazwa produktu jest wymagana.");
            return;
        }

        BigDecimal cena;
        try {
            cena = new BigDecimal(cenaStr);
            if (cena.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Cena musi być liczbą większą od zera.");
            return;
        }

        BigDecimal gramatura = null;
        if (!gramaturaStr.isEmpty()) {
            try {
                gramatura = new BigDecimal(gramaturaStr);
                if (gramatura.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Gramatura musi być liczbą większą od zera.");
                return;
            }
        }

        Integer iloscNaStanie = null;
        if (!iloscNaStanieStr.isEmpty()) {
            try {
                iloscNaStanie = Integer.parseInt(iloscNaStanieStr);
                if (iloscNaStanie < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Ilość na stanie musi być liczbą nieujemną.");
                return;
            }
        }

        List<Long> kategorieIds = new ArrayList<>();
        for (CheckBox checkBox : kategorieListView.getItems()) {
            if (checkBox.isSelected()) {
                Long id = (Long) checkBox.getUserData();
                kategorieIds.add(id);
            }
        }

        if (kategorieIds.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Musisz wybrać co najmniej jedną kategorię.");
            return;
        }

        ProductCreateDTO productCreateDTO = new ProductCreateDTO(nazwa, opis, cena, gramatura, iloscNaStanie, kategorieIds);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost("http://localhost:8080/api/products");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.addTextBody("nazwa", productCreateDTO.getNazwa(), ContentType.TEXT_PLAIN);
            builder.addTextBody("opis", productCreateDTO.getOpis() != null ? productCreateDTO.getOpis() : "", ContentType.TEXT_PLAIN);
            builder.addTextBody("cena", productCreateDTO.getCena().toString(), ContentType.TEXT_PLAIN);
            builder.addTextBody("gramatura", productCreateDTO.getGramatura() != null ? productCreateDTO.getGramatura().toString() : "", ContentType.TEXT_PLAIN);
            builder.addTextBody("iloscNaStanie", productCreateDTO.getIloscNaStanie() != null ? productCreateDTO.getIloscNaStanie().toString() : "", ContentType.TEXT_PLAIN);

            if (productCreateDTO.getKategorieIds() != null && !productCreateDTO.getKategorieIds().isEmpty()) {
                for (Long kategoriaId : productCreateDTO.getKategorieIds()) {
                    builder.addTextBody("kategorieIds", kategoriaId.toString(), ContentType.TEXT_PLAIN);
                }
            }

            if (wybraneZdjecie != null && wybraneZdjecie.exists()) {
                String mimeType = Files.probeContentType(wybraneZdjecie.toPath());
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                builder.addBinaryBody(
                        "zdjecie",
                        Files.readAllBytes(wybraneZdjecie.toPath()),
                        ContentType.parse(mimeType),
                        wybraneZdjecie.getName()
                );
            }

            uploadFile.setEntity(builder.build());

            String token = getToken();
            if (token != null && !token.isEmpty()) {
                uploadFile.setHeader("Authorization", "Bearer " + token);
            }

            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 201) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukces", "Produkt został dodany pomyślnie.");

                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }

                    resetForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas dodawania produktu. Status: " + statusCode + "\n" + responseBody);
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas wysyłania żądania." + e.getMessage());
        }
    }

    private void resetForm() {
        nazwaField.clear();
        opisArea.clear();
        cenaField.clear();
        gramaturaField.clear();
        iloscNaStanieField.clear();

        for (CheckBox checkBox : kategorieListView.getItems()) {
            checkBox.setSelected(false);
        }
        wybraneZdjecieLabel.setText("Brak wybranego zdjęcia");
        wybraneZdjecie = null;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
