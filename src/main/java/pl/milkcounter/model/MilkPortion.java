package pl.milkcounter.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MilkPortion {
    private LocalDate dateOfFreezing;
    private int portionOfMilk; //in milliliters
    private DateTimeFormatter polskiFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public MilkPortion (LocalDate dateOfFreezing, int portionOfMilk) {
        this.dateOfFreezing = dateOfFreezing;
        this.portionOfMilk = portionOfMilk;
    }

    public boolean isExpired(LocalDate dateToCheck) {
        LocalDate expirationDate = this.dateOfFreezing.plusMonths(6);
        return dateToCheck.isAfter(expirationDate);
    }

    public LocalDate getDateOfFreezing() {
        return dateOfFreezing;
    }

    public int getPortionOfMilk() {
        return portionOfMilk;
    }

    @Override
    public String toString() {
        return "Mleko (" + portionOfMilk + "ml) z dnia " + dateOfFreezing;
    }
}
