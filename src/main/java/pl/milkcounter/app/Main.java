package pl.milkcounter.app;

import pl.milkcounter.model.MilkPortion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        String dataTekst1 = "25.12.2025";
        String dataTekst2 = "26.06.2026";
        DateTimeFormatter polskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        MilkPortion milkPortion = new MilkPortion(LocalDate.parse(dataTekst1, polskiFormat), 150);
        System.out.println(milkPortion.isExpired(LocalDate.parse(dataTekst2, polskiFormat)));
        System.out.println(milkPortion.toString());

    }
}