package pl.milkcounter.app;

import pl.milkcounter.logic.SupplySimulator;
import pl.milkcounter.model.Child;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        String date1 = "25.12.2025";
        String date2 = "26.06.2026";
        DateTimeFormatter polskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        MilkPortion milkPortion1 = new MilkPortion(LocalDate.parse(date1, polskiFormat), 150);
        MilkPortion milkPortion2 = new MilkPortion(LocalDate.parse("27.12.2025", polskiFormat), 120);
        MilkPortion milkPortion3 = new MilkPortion(LocalDate.parse("20.10.2025", polskiFormat), 130);
//        System.out.println(milkPortion1.isExpired(LocalDate.parse(dataTekst2, polskiFormat)));
//        System.out.println(milkPortion1.toString());

        MilkStorage storage = new MilkStorage();
        storage.addPortion(milkPortion1);
        storage.addPortion(milkPortion2);
        storage.addPortion(milkPortion3);
        storage.addPortion(new MilkPortion(LocalDate.parse(date2, polskiFormat), 150));

        System.out.println(storage.getPortions());
        System.out.println(storage.getTotal());

        System.out.println(storage.takeMilk(LocalDate.parse("13.05.2026", polskiFormat), 240));
        System.out.println(storage.getTotal());

        Child child = new Child(LocalDate.parse("15.10.2025", polskiFormat), 5.6f);
        child.addDailyLog(900, 8);
        child.addDailyLog(820, 6);
        child.addDailyLog(850, 6);
        child.addDailyLog(830, 6);
        child.addDailyLog(880, 7);

        System.out.println(child.dailyDemandOfMilk());
        System.out.println(child.getDemandForBabyAge(LocalDate.parse("25.07.2026", polskiFormat)));

        SupplySimulator simulator = new SupplySimulator();
        LocalDate endOfSupply = simulator.endOfSupply(storage, child);
        System.out.println("Zapasy skończą się dnia: " + endOfSupply);

    }
}