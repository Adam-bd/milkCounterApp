package pl.milkcounter.io;

import pl.milkcounter.model.ChildData;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class AppFileReader {
    private File fileMagazyn;
    private File fileUstawienia;
    private File fileHistoria;

    private final DateTimeFormatter polishFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public AppFileReader(String folderPath) {
        this.fileMagazyn = Paths.get(folderPath, "magazyn.csv").toFile();
        this.fileUstawienia = Paths.get(folderPath, "ustawienia.csv").toFile();
        this.fileHistoria = Paths.get(folderPath, "historia_wagi.csv").toFile();

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    //MAGAZYN
    public void saveToFile(MilkStorage milkStorage) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileMagazyn))) {
            for (MilkPortion portion : milkStorage.getPortions()) {
                //format-> Data : Ilośćml
                String formattedDate = portion.getDateOfFreezing().format(polishFormat);
                String line = formattedDate + ":" + portion.getPortionOfMilk() + "ml";
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Zapisano stan magazynu.");
        } catch (IOException e) {
            System.out.println("Błąd zapisu magazynu: " + e.getMessage());
        }
    }

    public MilkStorage loadFromFile() {
        MilkStorage milkStorage = new MilkStorage();
        File file = new File(String.valueOf(fileMagazyn));
        if (!file.exists()) return milkStorage;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("[:;\\t]");

                if (parts.length >= 2) {
                    try {
                        LocalDate date = parseFlexibleDate(parts[0]);
                        String amountStr = parts[1].toLowerCase().replace("ml", "").replace("\"", "").trim();
                        int amount = Integer.parseInt(amountStr);

                        if (date != null) {
                            milkStorage.addPortion(new MilkPortion(date, amount));
                        }
                    } catch (Exception e) {
                        System.out.println("Pominięto błędną linię magazynu: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Błąd odczytu magazynu: " + e.getMessage());
        }
        return milkStorage;
    }

    //DZIECKO
    public void saveChildData(ChildData childData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileUstawienia))) {
            String line = String.format("%s;%s;%s;%s;%d;%d;%s",
                    childData.getName(),
                    childData.getBirthDate().toString(),
                    String.valueOf(childData.getBabyWeight()),
                    LocalDate.now().toString(),
                    childData.getSavedDailySum(),
                    childData.getSavedBottlesCount(),
                    childData.getGender()
            );
            writer.write(line);
        } catch (IOException e) {
            System.out.println("Błąd zapisu ustawień: " + e.getMessage());
        }
    }

    public ChildData loadChildData() {
        File file = new File(String.valueOf(fileUstawienia));
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split("[;:\\t|]");

                if (parts.length >= 3) { //imie, data, waga
                    String name = parts[0].trim();
                    LocalDate dob = parseFlexibleDate(parts[1]);
                    float weight = Float.parseFloat(parts[2].replace(",", ".").replace("\"", ""));
                    LocalDate lastSaveDate = LocalDate.now();
                    int savedSum = 0;
                    int savedBottles = 0;
                    String gender = "M";

                    if (parts.length > 3) lastSaveDate = parseFlexibleDate(parts[3]);
                    if (parts.length > 4) savedSum = Integer.parseInt(parts[4].trim());
                    if (parts.length > 5) savedBottles = Integer.parseInt(parts[5].trim());
                    if (parts.length > 6) gender = parts[6].trim().replace("\"", "");

                    if (lastSaveDate != null && !lastSaveDate.equals(LocalDate.now())) {
                        savedSum = 0;
                        savedBottles = 0;
                        lastSaveDate = LocalDate.now();
                    }

                    return new ChildData(name, dob, weight, lastSaveDate, savedSum, savedBottles, gender);
                }
            }
        } catch (Exception e) {
            System.out.println("Błąd odczytu ustawień: " + e.getMessage());
        }
        return null;
    }

    public void saveWeightHistory(Map<LocalDate, Float> history) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileHistoria))) {
            for (Map.Entry<LocalDate, Float> entry : history.entrySet()) {
                bw.write(entry.getKey() + ";" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Błąd zapisu historii wagi: " + e.getMessage());
        }
    }

    public Map<LocalDate, Float> loadWeightHistory() {
        Map<LocalDate, Float> history = new TreeMap<>();
        File file = new File(String.valueOf(fileHistoria));

        if (!file.exists()) return history;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("[;:\\t|]");

                if (parts.length >= 2) {
                    try {
                        String dateStr = parts[0].trim().replace("\"", "");
                        String weightStr = parts[1].trim().replace("\"", "").replace(",", ".");

                        LocalDate date = parseFlexibleDate(dateStr);
                        float weight = Float.parseFloat(weightStr);

                        if (date != null) {
                            history.put(date, weight);
                        }
                    } catch (Exception e) {
                        System.out.println("Pominięto błędną linię historii: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Błąd odczytu historii wagi: " + e.getMessage());
        }
        return history;
    }

    //metoda do rozpoznawania różnych formatów dat
    private LocalDate parseFlexibleDate(String dateStr) {
        dateStr = dateStr.trim().replace("\"", "");
        try {
            if (dateStr.contains(".")) {
                return LocalDate.parse(dateStr, polishFormat); // 15.10.2025
            } else if (dateStr.contains("/")) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            } else {
                return LocalDate.parse(dateStr); // Standard ISO: 2025-10-15
            }
        } catch (Exception e) {
            return null; // Nie udało się odczytać daty
        }
    }
}