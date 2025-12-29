package pl.milkcounter.io;

import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AppFileReader {
    private static final String FILE = "magazyn.csv";
    DateTimeFormatter polishFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void saveToFile(MilkStorage milkStorage) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE));){
            for (MilkPortion portion : milkStorage.getPortions()){
                String formattedDate = portion.getDateOfFreezing().format(polishFormat);
                String line = formattedDate + ": " + portion.getPortionOfMilk() + "ml";
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Zapisano stan magazynu do pliku: " + FILE);
        } catch (IOException e) {
            System.out.println("Błąd zapisu do pliku. Powód: " + e.getMessage());
        }
    }

    public MilkStorage loadFromFile() {
        MilkStorage milkStorage = new MilkStorage();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null){
                String[] substring = line.split(":");
                if (substring.length == 2){
                    LocalDate date = LocalDate.parse(substring[0], polishFormat);
                    String amountSubstring = substring[1].trim();
                    amountSubstring = amountSubstring.replace("ml", "");
                    int amountOfMilk = Integer.parseInt(amountSubstring);
                    milkStorage.addPortion(new MilkPortion(date, amountOfMilk));
                }
            }
            System.out.println("Wczytano dane z pliku: " + FILE);
        } catch (IOException e) {
            System.out.println("Brak pliku z danymi lub błąd odczytu.");
        }
        return milkStorage;
    }
}
