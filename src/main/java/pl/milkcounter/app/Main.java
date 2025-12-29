package pl.milkcounter.app;

import pl.milkcounter.io.AppFileReader;
import pl.milkcounter.logic.SupplySimulator;
import pl.milkcounter.model.Child;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        DateTimeFormatter polskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // 1. Uruchamiamy menedżera plików
        AppFileReader fileReader = new AppFileReader();

        System.out.println("--- KROK 1: WCZYTYWANIE ---");
        // Zamiast tworzyć "new MilkStorage()", wczytujemy stan z dysku
        MilkStorage storage = fileReader.loadFromFile();
        System.out.println("Stan magazynu po uruchomieniu: " + storage.getTotal() + " ml");

        // 2. Dodajemy mleko (symulacja: mama odciągnęła dzisiaj i kilka dni temu)
        // Uwaga: Te porcje będą się dodawać przy KAŻDYM uruchomieniu programu!
        System.out.println("\n--- KROK 2: DODAWANIE NOWYCH PORCJI ---");

        MilkPortion p1 = new MilkPortion(LocalDate.parse("25.12.2025", polskiFormat), 150);
        MilkPortion p2 = new MilkPortion(LocalDate.parse("27.12.2025", polskiFormat), 120);
        MilkPortion p3 = new MilkPortion(LocalDate.parse("20.10.2025", polskiFormat), 130);

        storage.addPortion(p1);
        storage.addPortion(p2);
        storage.addPortion(p3);

        System.out.println("Dodano nowe mleko. Aktualny stan: " + storage.getTotal() + " ml");
        // Opcjonalnie: podgląd listy, żeby zobaczyć czy są stare i nowe
        // System.out.println(storage.getPortions());

        // 3. Konfiguracja dziecka (tak jak miałeś)
        System.out.println("\n--- KROK 3: OBLICZENIA I SYMULACJA ---");
        Child child = new Child(LocalDate.parse("15.10.2025", polskiFormat), 5.6f);
        child.addDailyLog(900, 8);
        child.addDailyLog(820, 6);
        child.addDailyLog(850, 6);
        child.addDailyLog(830, 6);
        child.addDailyLog(880, 7);

        System.out.println("Dzienne zapotrzebowanie (dziś): " + child.dailyDemandOfMilk() + " ml");
        System.out.println("Zapotrzebowanie w lipcu 2026: " + child.getDemandForBabyAge(LocalDate.parse("25.07.2026", polskiFormat)) + " ml");

        // 4. Symulacja zapasów
        SupplySimulator simulator = new SupplySimulator();
        LocalDate endOfSupply = simulator.endOfSupply(storage, child);
        System.out.println(">>> Zapasy skończą się dnia: " + endOfSupply + " <<<");

        // 5. ZAPIS (Najważniejszy moment!)
        System.out.println("\n--- KROK 4: ZAPISYWANIE ---");
        fileReader.saveToFile(storage);
    }
}