package pl.milkcounter.app;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.w3c.dom.css.CSSStyleRule;
import pl.milkcounter.io.AppFileReader;
import pl.milkcounter.io.ReportGenerator;
import pl.milkcounter.logic.SupplySimulator;
import pl.milkcounter.model.ChildData;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DashboardController {
    @FXML private Label totalStockLabel;
    @FXML private Label dailyDemandLabel;
    @FXML private Label runoutDateLabel;
    @FXML private Label expiredLabel;

    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;

    @FXML private TableView<MilkPortion> milkTable;
    @FXML private TableColumn<MilkPortion, LocalDate> dateColumn;
    @FXML private TableColumn<MilkPortion, Integer> amountColumn;

    @FXML private TextField babyName;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField weightField;
    @FXML private TextField eatenAmountField; //ile zjadło (ml) na porcję
    @FXML private Label todayStatsLabel;
    @FXML private Label ageLabel;
    @FXML private RadioButton boyRadio;
    @FXML private RadioButton girlRadio;
    @FXML private LineChart<String, Number> weightChart;
    @FXML private HBox historyInputBox;
    @FXML private DatePicker historyDatePicker;
    @FXML private TextField historyWeightField;
    @FXML private ListView<String> suggestionListView;
    @FXML private Label suggestionSummaryLabel;
    @FXML private VBox assistantPanel;
    @FXML private Button toggleAssistantButton;

    private MilkStorage storage;
    private ChildData child;
    private AppFileReader fileReader;
    private SupplySimulator simulator;
    private Map<LocalDate, Float> weightHistory;
    private List<MilkPortion> currentSugestion = new ArrayList<>();
    private int todayMilkSum = 0;
    private int todayBottlesCount = 0;

    private final DateTimeFormatter polishFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter shortDateFormat = DateTimeFormatter.ofPattern("dd.MM");

    public void initialize() {
        fileReader = new AppFileReader();
        simulator = new SupplySimulator();

        //wczytanie danych z pliku
        storage = fileReader.loadFromFile();
        ChildData loadedChild = fileReader.loadChildData();
        weightHistory = fileReader.loadWeightHistory();

        if(loadedChild != null){
            child = loadedChild;
            todayMilkSum = child.getSavedDailySum();
            todayBottlesCount = child.getSavedBottlesCount();
        } else {
            child = new ChildData("Dzidziuś", LocalDate.now().minusMonths(1), 3.5f, LocalDate.now(), 0, 0, "M");
        }

        if (weightHistory.isEmpty()) {
            weightHistory.put(LocalDate.now(), child.getBabyWeight());
        }

        child.addDailyLog(900, 7);

        milkTable.setRowFactory(tv -> new TableRow<MilkPortion>() {
            @Override
            protected void updateItem(MilkPortion item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isExpired(LocalDate.now())) {
                    setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c;"); //na czerwono
                } else {
                    setStyle("");
                }
            }
        });

        //ustawianie kolumn w Tabeli (Magazyn)
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfFreezing"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("portionOfMilk"));
        //formatowanie daty w tabeli (dd.MM.yyyy)
        dateColumn.setCellFactory(column -> new TableCell<MilkPortion, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(polishFormat));
                }
            }
        });

        //ustawienie wartości w polach formularza (Dziecko)
        babyName.setText(child.getName());
        birthDatePicker.setValue(child.getBirthDate());
        weightField.setText(String.valueOf(child.getBabyWeight()));

        if ("F".equals(child.getGender())) {
            girlRadio.setSelected(true);
        } else {
            boyRadio.setSelected(true);
        }

        updateChart();
        setupChildListener();
        refreshView();
        updateTodayLabel();
    }

    private void setupChildListener() {
        babyName.textProperty().addListener((observable, oldValue, newValue) -> {
            child.setName(newValue);
            saveCurrentState();
            refreshView();
        });
        birthDatePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                child.setBirthDate(newDate);
                saveCurrentState();
                refreshView();
            }
        });

        weightField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String normalizedValue = newValue.replace(",", ".");
                float newWeight = Float.parseFloat(normalizedValue);
                child.setBabyWeight(newWeight);
                weightHistory.put(LocalDate.now(), newWeight);
                fileReader.saveWeightHistory(weightHistory);
                updateChart();
                saveCurrentState();
                refreshView();
            } catch (NumberFormatException e) {
                //nic nie rób
            }
        });
        boyRadio.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                child.setGender("M");
                saveCurrentState();
                refreshView();
            }
        });
        girlRadio.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                child.setGender("F");
                saveCurrentState();
                refreshView();
            }
        });
    }

    //przycisk ZAPISZ
    @FXML
    private void handleAddAction() {
        try {
            //pobieramy dane
            LocalDate date = datePicker.getValue();
            String amountText = amountField.getText();

            //sprawdzamy czy pola nie są puste
            if (date != null && !amountText.isEmpty()) {
                int amount = Integer.parseInt(amountText);
                storage.addPortion(new MilkPortion(date, amount));
                fileReader.saveToFile(storage);
                amountField.clear();
                datePicker.setValue(null);

                refreshView();
            }
        } catch (NumberFormatException e) {
            showError("Błąd", "Wpisano tekst zamiast liczby!");
            System.out.println("Błąd: Wpisano tekst zamiast liczby!");
        }
    }

    //odświeżanie ekranu
    private void refreshView() {
        milkTable.getItems().setAll(storage.getPortions());
        totalStockLabel.setText(storage.getTotal() + " ml");

        // Obsługa ostrzeżenia o przeterminowaniu
        int expiredTotal = storage.getExpiredTotal();
        if (expiredLabel != null) {
            if (expiredTotal > 0) {
                expiredLabel.setText("UWAGA: " + expiredTotal + " ml przeterminowane!");
                expiredLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                expiredLabel.setText("Stan zapasów OK");
                expiredLabel.setStyle("-fx-text-fill: green;");
            }
        }

        int demand = child.getDemandForBabyAge(LocalDate.now());
        dailyDemandLabel.setText(demand + " ml/dzień");

        LocalDate endDate = simulator.endOfSupply(storage, child);
        runoutDateLabel.setText(endDate.format(polishFormat));

        updateAgeLabel();
        updateTodayLabel();
        updateSuggestionPanel();
    }

    @FXML
    private void handleAddLogAction() {
        try {
            String mlText = eatenAmountField.getText();

            if (!mlText.isEmpty()) {
                int singlePortion = Integer.parseInt(mlText);
                todayMilkSum += singlePortion;
                todayBottlesCount++; //założenie: 1 dodanie = 1 butelka
                child.addDailyLog(todayMilkSum, todayBottlesCount);

                eatenAmountField.clear();
                saveCurrentState();
                updateTodayLabel();
                refreshView();

                System.out.println("Dodano porcję: " + singlePortion + ". Razem dziś: " + todayMilkSum);
            }
        } catch (NumberFormatException e) {
            showError("Błąd", "Wpisz poprawną liczbę!");
            System.out.println("Błąd: Wpisz poprawną liczbę!");
        }
    }

    //metoda pomocnicza do wyświetlania postępu dnia
    private void updateTodayLabel() {
        if (todayStatsLabel != null) {
            String name = child.getName();

            String verb = girlRadio.isSelected() ? "wypiła" : "wypił";

            todayStatsLabel.setText(name + " " + verb + " dzisiaj: "
                    + todayMilkSum + " ml (" + todayBottlesCount + " butelek)");
        }
    }

    private void saveCurrentState() {
        String currentGender = girlRadio.isSelected() ? "F" : "M";
        ChildData dataToSave = new ChildData(
                child.getName(),
                child.getBirthDate(),
                child.getBabyWeight(),
                LocalDate.now(),
                todayMilkSum,
                todayBottlesCount,
                currentGender
        );
        //zapis
        fileReader.saveChildData(dataToSave);
        this.child = dataToSave;
    }

    @FXML
    private void handleUnfreezeAction() {
        MilkPortion selected = milkTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            storage.unfreezeMilk(selected);
            fileReader.saveToFile(storage);
            refreshView();
        }
    }

    @FXML
    private void handleRemoveExpiredAction() {
        int expiredCount = storage.getExpiredTotal();
        if (expiredCount > 0) {
            storage.removeAllExpired();
            fileReader.saveToFile(storage);
            refreshView();
            System.out.println("Usunięto " + expiredCount + " ml zepsutego mleka.");
        }
    }

    private void updateAgeLabel() {
        if (child.getBirthDate() != null && ageLabel != null) {
            LocalDate birth = child.getBirthDate();
            LocalDate now = LocalDate.now();
            String name = child.getName();
            String pronoun = girlRadio.isSelected() ? "urodziła" : "urodził";

            if (birth.isAfter(now)) {
                ageLabel.setText(name + " jeszcze się nie " + pronoun + "!");
                return;
            }

            long toalMonths = ChronoUnit.MONTHS.between(birth, now);
            long totalWeeks = ChronoUnit.WEEKS.between(birth, now);
            long totalDays = ChronoUnit.DAYS.between(birth, now);

            String ageText = String.format("%s ma teraz: %d mies., czyli %d tyg., czyli %d dni.",
                    name, toalMonths, totalWeeks, totalDays);

            ageLabel.setText(ageText);
        }
    }

    private void updateChart() {
        weightChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Waga (kg)");

        //przechodzimy przez historię i dodajemy punkty
        for (Map.Entry<LocalDate, Float> entry : weightHistory.entrySet()) {
            String dateLabel = entry.getKey().format(shortDateFormat);
            series.getData().add(new XYChart.Data<>(dateLabel, entry.getValue()));
        }
        weightChart.getData().add(series);

        for(XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                String string = data.getYValue() + "kg, dnia: " + data.getXValue();
                Tooltip tooltip = new Tooltip(string);
                tooltip.setShowDelay(javafx.util.Duration.millis(100));
                Tooltip.install(node, tooltip);

                node.setOnMouseEntered(event -> {
                    node.setStyle("-fx-scale-x: 1.5; -fx-scale-y: 1.5; -fx-background-color: #ff0000;");
                });

                node.setOnMouseExited(event -> {
                    node.setStyle("");
                });
            }
        }
    }

    @FXML
    private void handleToggleWeightHistoryPanel() {
        boolean isVisible = historyInputBox.isVisible();
        historyInputBox.setVisible(!isVisible);
        historyInputBox.setManaged(!isVisible);

        if (!isVisible) {
            historyDatePicker.setValue(null);
            historyWeightField.clear();
        }
    }

    @FXML
    private void handleSaveWeightHistory() {
        try {
            LocalDate date = historyDatePicker.getValue();
            String weightText = historyWeightField.getText();

            if(date.isAfter(LocalDate.now())) {
                showError("Błędna data", "Chcesz dodać wagę w dniu, którego jeszcze nie ma!");
                System.out.println("Chcesz dodać wagę w dniu, którego jeszcze nie ma!");
                return;
            }

            if (date != null && weightText != null){
                float weight = Float.parseFloat(weightText.replace(",", "."));
                weightHistory.put(date, weight);
                fileReader.saveWeightHistory(weightHistory);
                updateChart();
                System.out.println("Dodano wagę: " + weight + "kg z dnia: " + date);
                handleToggleWeightHistoryPanel();
            }
        } catch (NumberFormatException e) {
            showError("Błąd", "Wpisz poprawną wagę!");
            System.out.println("Błąd: Wpisz poprawną wagę!");
        }
    }

    //error dla użytkownika
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateSuggestionPanel() {
        if(suggestionListView == null) {
            return;
        }

        int demand = child.getDemandForBabyAge(LocalDate.now());
        currentSugestion = storage.suggestPortionsToUnfreeze(demand);
        suggestionListView.getItems().clear();
        int currentVolume = 0;

        for (MilkPortion portion : currentSugestion) {
            currentVolume += portion.getPortionOfMilk();
            String string = portion.getDateOfFreezing().format(shortDateFormat) + ": " + portion.getPortionOfMilk() +"ml";
            suggestionListView.getItems().add(string);
        }

        suggestionSummaryLabel.setText("Razem: " + currentVolume + "ml");
        if(currentVolume >= demand) {
            suggestionSummaryLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            suggestionSummaryLabel.setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleUnfreezeSuggestion() {
        if(currentSugestion.isEmpty()) {
            showError("Pusto", "Brak woreczków do zaplanowania!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Rozmrozić sugerowany zestaw?");
        alert.setContentText("Usuniemy " + currentSugestion.size() + " woreczków z magazynu.");
        if(alert.showAndWait().get() == ButtonType.OK) {
            for(MilkPortion portion : currentSugestion) {
                storage.unfreezeMilk(portion);
            }
            fileReader.saveToFile(storage);
            refreshView();
        }
    }

    @FXML private void handleGeneratingReport() {
        ReportGenerator report = new ReportGenerator();
        int demand = child.getDemandForBabyAge(LocalDate.now());
        report.generateReport(child, weightHistory, demand);
    }

    @FXML
    private void handleToggleAssistant() {
        boolean isVisible = assistantPanel.isVisible();
        assistantPanel.setVisible(!isVisible);
        assistantPanel.setManaged(!isVisible); //wyśrodkowanie pozostałego

        if (isVisible) {
            toggleAssistantButton.setText("Pokaż Plan na Jutro ⬇");
            toggleAssistantButton.setStyle("-fx-font-size: 10px; -fx-background-color: #e0e0e0;"); // Szary
        } else {
            toggleAssistantButton.setText("Ukryj Plan na Jutro ⬆");
            toggleAssistantButton.setStyle("-fx-font-size: 10px; -fx-background-color: #ffe0b2;"); // Pomarańczowy
        }
    }
}