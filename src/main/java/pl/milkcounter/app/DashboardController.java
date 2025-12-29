package pl.milkcounter.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.milkcounter.io.AppFileReader;
import pl.milkcounter.logic.SupplySimulator;
import pl.milkcounter.model.Child;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    @FXML private Label totalStockLabel;
    @FXML private Label dailyDemandLabel;
    @FXML private Label runoutDateLabel;

    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;

    @FXML private TableView<MilkPortion> milkTable;
    @FXML private TableColumn<MilkPortion, LocalDate> dateColumn;
    @FXML private TableColumn<MilkPortion, Integer> amountColumn;

    private MilkStorage storage;
    private Child child;
    private AppFileReader fileReader;
    private SupplySimulator simulator;

    public void initialize() {
        fileReader = new AppFileReader();
        simulator = new SupplySimulator();

        storage = fileReader.loadFromFile();

        //konfiguracja dziecka
        child = new Child(LocalDate.of(2025, 10, 15), 5.6f);
        child.addDailyLog(900, 7); // Dodajemy historię, żeby algorytm miał co liczyć
        child.addDailyLog(850, 6);

        //ustawianie kolumny w tabeli
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfFreezing"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("portionOfMilk"));

        dateColumn.setCellFactory(column -> new TableCell<MilkPortion, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                }
            }
        });
        refreshView();
    }

    //przycisk ZAPISZ
    @FXML
    private void handleAddAction() {
        try {
            //pobieramy dane z formularza
            LocalDate date = datePicker.getValue();
            String amountText = amountField.getText();

            //sprawdzamy czy pola nie są puste
            if (date != null && !amountText.isEmpty()) {
                int amount = Integer.parseInt(amountText);

                //dodajemy mleko do magazynu
                storage.addPortion(new MilkPortion(date, amount));

                //zapisujemy zmiany na dysku!
                fileReader.saveToFile(storage);

                //czyścimy pola formularza
                amountField.clear();
                datePicker.setValue(null);

                refreshView();
            }
        } catch (NumberFormatException e) {
            System.out.println("Błąd: Wpisano tekst zamiast liczby!");
        }
    }

    //odświeżanie ekranu
    private void refreshView() {
        //wrzucamy listę mleka do tabeli
        milkTable.getItems().setAll(storage.getPortions());

        //aktualizujemy główny licznik
        totalStockLabel.setText(storage.getTotal() + " ml");

        //aktualizujemy zapotrzebowanie dziecka
        int demand = child.getDemandForBabyAge(LocalDate.now());
        dailyDemandLabel.setText(demand + " ml/dzień");

        //obliczamy datę końca zapasów
        LocalDate endDate = simulator.endOfSupply(storage, child);
        runoutDateLabel.setText(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
}