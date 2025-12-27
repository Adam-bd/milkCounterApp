package pl.milkcounter.model;

import java.time.LocalDate;

public class MilkPortion {
    private LocalDate dateOfFreezing;
    private int portionOfMilk; //in milliliters

    MilkPortion (LocalDate dateOfFreezing, int portionOfMilk) {
        this.dateOfFreezing = dateOfFreezing;
        this.portionOfMilk = portionOfMilk;
    }

}
