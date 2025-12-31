package pl.milkcounter.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MilkCounterApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("dashboard.fxml")));

            Scene scene = new Scene(root);

            stage.setTitle("Milk Counter - Asystent Rodzica");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // To wypisze błąd w konsoli, jeśli FXML się nie załaduje
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
