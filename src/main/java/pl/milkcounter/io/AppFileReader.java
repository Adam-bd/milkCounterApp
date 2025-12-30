package pl.milkcounter.io;

import pl.milkcounter.model.ChildData;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class AppFileReader {
    private static final String FILE_MAGAZYN = "magazyn.csv";
    private static final String FILE_USTAWIENIA = "ustawienia.csv";
    private static final String FILE_HISTORIA = "historia_wagi.csv";

    // Format do zapisu (żeby było ładnie w pliku)
    private final DateTimeFormatter polishFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // --- 1. MAGAZYN (Mleko) ---

    public void saveToFile(MilkStorage milkStorage) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_MAGAZYN))) {
            for (MilkPortion portion : milkStorage.getPortions()) {
                // Zapisujemy w formacie: Data : Ilośćml
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
        File file = new File(FILE_MAGAZYN);
        if (!file.exists()) return milkStorage;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Dzielimy po dwukropku, średniku lub tabulatorze
                String[] parts = line.split("[:;\\t]");

                if (parts.length >= 2) {
                    try {
                        LocalDate date = parseFlexibleDate(parts[0]);
                        // Usuwamy "ml", spacje i ewentualne cudzysłowy
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

    // --- 2. USTAWIENIA DZIECKA ---

    public void saveChildData(ChildData childData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_USTAWIENIA))) {
            // Zapisujemy przy użyciu średnika
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
        File file = new File(FILE_USTAWIENIA);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                // Dzielimy po średniku, dwukropku, tabulatorze lub kresce pionowej
                String[] parts = line.split("[;:\\t|]");

                if (parts.length >= 3) { // Musi być przynajmniej imię, data i waga
                    String name = parts[0].trim();
                    LocalDate dob = parseFlexibleDate(parts[1]);
                    float weight = Float.parseFloat(parts[2].replace(",", ".").replace("\"", ""));

                    // Wartości domyślne, jeśli plik jest krótki (stary format)
                    LocalDate lastSaveDate = LocalDate.now();
                    int savedSum = 0;
                    int savedBottles = 0;
                    String gender = "M";

                    if (parts.length > 3) lastSaveDate = parseFlexibleDate(parts[3]);
                    if (parts.length > 4) savedSum = Integer.parseInt(parts[4].trim());
                    if (parts.length > 5) savedBottles = Integer.parseInt(parts[5].trim());
                    if (parts.length > 6) gender = parts[6].trim().replace("\"", "");

                    // Resetowanie licznika dziennego
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

    // --- 3. HISTORIA WAGI ---

    public void saveWeightHistory(Map<LocalDate, Float> history) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_HISTORIA))) {
            for (Map.Entry<LocalDate, Float> entry : history.entrySet()) {
                // Zapisujemy: Data;Waga
                bw.write(entry.getKey() + ";" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Błąd zapisu historii wagi: " + e.getMessage());
        }
    }

    public Map<LocalDate, Float> loadWeightHistory() {
        Map<LocalDate, Float> history = new TreeMap<>();
        File file = new File(FILE_HISTORIA);

        if (!file.exists()) return history;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // MAGIA: Dzieli po ; lub : lub TAB lub |
                String[] parts = line.split("[;:\\t|]");

                // Awaryjnie: jeśli nie znalazł separatora, a jest przecinek (np. Excel USA), spróbuj po przecinku
                if (parts.length < 2 && line.contains(",")) {
                    // Uwaga: to ryzykowne przy liczbach 5,5 ale próbujemy
                    parts = line.split(",");
                }

                if (parts.length >= 2) {
                    try {
                        // Czyszczenie śmieci (cudzysłowy z excela)
                        String dateStr = parts[0].trim().replace("\"", "");
                        // Zamiana przecinka na kropkę w wadze
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

    // --- POMOCNICZA METODA DO DAT ---
    // Rozumie formaty: 2025-10-15, 15.10.2025, 2025/10/15
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