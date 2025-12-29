package pl.milkcounter.logic;

import pl.milkcounter.model.Child;
import pl.milkcounter.model.MilkPortion;
import pl.milkcounter.model.MilkStorage;

import java.time.LocalDate;
import java.util.List;

public class SupplySimulator {
    public LocalDate endOfSupply(MilkStorage storage, Child child) {
        MilkStorage copiedStorage = storage.copy();

        LocalDate date = LocalDate.now();
        int daysCount = 0;

        while(!copiedStorage.isEmpty()){
            int milkDemand = child.dailyDemandOfMilk();

            List<MilkPortion> portions = copiedStorage.takeMilk(date, milkDemand);

            int collectedAmount = 0;
            for (MilkPortion portion : portions) {
                collectedAmount += portion.getPortionOfMilk();
            }

            //jeśli jest mniej danego dnia niż zapotrzebowanie to zapas w tym dniu się kończy
            if (collectedAmount < milkDemand) {
                return date;
            }
            //w przeciwnym wpadku zapas strcza na kolejny dzień
            daysCount++;
            //ustawiamy datę na kolejny dzień
            date = date.plusDays(1);
        }
        return date;
    }
}
