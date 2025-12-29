package pl.milkcounter.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MilkCounterApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MilkCounterApp.class.getResource("dashboard.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        stage.setTitle("Milk Counter - Magazyn Mleka");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
