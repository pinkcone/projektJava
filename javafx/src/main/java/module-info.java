module com.pollub.cookiefx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.slf4j;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static lombok;
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.databind;
    exports com.pollub.cookiefx;
    exports com.pollub.cookiefx.controller to javafx.fxml, com.fasterxml.jackson.databind;

    opens com.pollub.cookiefx.controller to javafx.fxml, com.fasterxml.jackson.databind;
    opens com.pollub.cookiefx.dto to com.fasterxml.jackson.databind, javafx.base;
    exports com.pollub.cookiefx.enums to com.fasterxml.jackson.databind, javafx.fxml;
    opens com.pollub.cookiefx.enums to com.fasterxml.jackson.databind, javafx.fxml;
}
