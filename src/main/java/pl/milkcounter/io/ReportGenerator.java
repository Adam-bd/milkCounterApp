package pl.milkcounter.io;

import pl.milkcounter.model.ChildData;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class ReportGenerator {

    public void generateReport(ChildData child, Map<LocalDate, Float> weightHistory, int dailyDemand) {
        String fileName = "Raport_Dla_Lekarza_" + child.getName() + ".txt";
        File file = new File(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 1. NAGŁÓWEK
            writer.write("==========================================");
            writer.newLine();
            writer.write(" RAPORT ZDROWIA DZIECKA - MILK COUNTER");
            writer.newLine();
            writer.write(" Data generowania: " + LocalDate.now());
            writer.newLine();
            writer.write("==========================================");
            writer.newLine();
            writer.newLine();

            // 2. DANE DZIECKA
            long months = ChronoUnit.MONTHS.between(child.getBirthDate(), LocalDate.now());
            long weeks = ChronoUnit.WEEKS.between(child.getBirthDate(), LocalDate.now());

            writer.write("DZIECKO: " + child.getName());
            writer.newLine();
            writer.write("Data urodzenia: " + child.getBirthDate());
            writer.newLine();
            writer.write("Wiek: " + months + " mies. (" + weeks + " tyg.)");
            writer.newLine();
            writer.write("Płeć: " + ("F".equals(child.getGender()) ? "Dziewczynka" : "Chłopiec"));
            writer.newLine();
            writer.newLine();

            // 3. KARMIENIE
            writer.write("------------------------------------------");
            writer.newLine();
            writer.write(" STATYSTYKI KARMIENIA");
            writer.newLine();
            writer.write("------------------------------------------");
            writer.newLine();
            writer.write("Zapotrzebowanie (norma dla wieku): " + dailyDemand + " ml/dzień");
            writer.newLine();
            writer.write("Spożycie dzisiaj: " + child.getSavedDailySum() + " ml");
            writer.newLine();
            writer.newLine();

            // 4. WAGA I WZROST
            writer.write("------------------------------------------");
            writer.newLine();
            writer.write(" HISTORIA WAGI");
            writer.newLine();
            writer.write("------------------------------------------");
            writer.newLine();

            if (weightHistory.isEmpty()) {
                writer.write("Brak danych o wadze.");
            } else {
                // Obliczanie przyrostu
                Map.Entry<LocalDate, Float> firstEntry = weightHistory.entrySet().iterator().next();
                float startWeight = firstEntry.getValue();
                float currentWeight = child.getBabyWeight();
                float gain = currentWeight - startWeight;

                writer.write(String.format("Obecna waga: %.2f kg", currentWeight));
                writer.newLine();
                writer.write(String.format("Całkowity przyrost: %.2f kg (od %s)", gain, firstEntry.getKey()));
                writer.newLine();
                writer.newLine();
                writer.write("Szczegółowy dziennik:");
                writer.newLine();

                for (Map.Entry<LocalDate, Float> entry : weightHistory.entrySet()) {
                    writer.write(" - " + entry.getKey() + ": " + entry.getValue() + " kg");
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("==========================================");
            writer.write("\nWygenerowano przez aplikację MilkCounter.");

        } catch (IOException e) {
            System.out.println("Błąd zapisu raportu: " + e.getMessage());
        }

        // 5. AUTOMATYCZNE OTWARCIE PLIKU
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            System.out.println("Nie udało się otworzyć pliku automatycznie.");
        }
    }
}