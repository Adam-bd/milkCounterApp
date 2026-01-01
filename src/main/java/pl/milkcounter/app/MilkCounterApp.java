package pl.milkcounter.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class MilkCounterApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("dashboard.fxml")));
            Scene scene = new Scene(root);
            stage.setTitle("Milk Counter - Asystent Rodzica");
            stage.setScene(scene);
            Image icon = new Image(getClass().getResourceAsStream("images/logo.png"));
            stage.getIcons().add(icon);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
