package pl.milkcounter.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MilkStorage {
    private List<MilkPortion> portions;

    public MilkStorage() {
        this.portions = new ArrayList<>();
    }

    public void addPortion(MilkPortion portion) {
        portions.add(portion);
        portions.sort(Comparator.comparing(MilkPortion::getDateOfFreezing));
    }

    public int getTotal() {
        int totalMilk = 0;
        for(MilkPortion portion : portions){
            totalMilk += portion.getPortionOfMilk();
        }
        return totalMilk;
    }

    public List<MilkPortion> getPortions() {
        return portions;
    }

    public List<MilkPortion> takeMilk(LocalDate todayDate, int amountOfMilkNeeded) {
        List<MilkPortion> portionsToTakeOut = new ArrayList<>(); //take out of freezer
        List<MilkPortion> portionsToThrowOut = new ArrayList<>();
        int amountOfMilkCollected = 0;

        for(MilkPortion portion : portions) {
            if (portion.isExpired(todayDate)) {
                System.out.println("Uwaga!!! Znaleziono mleko po terminie. Wyrzuć mleko z dnia: " + portion.getDateOfFreezing());
                portionsToThrowOut.add(portion);
                continue;
            }

            portionsToTakeOut.add(portion);
            amountOfMilkCollected += portion.getPortionOfMilk();

            if (amountOfMilkCollected >= amountOfMilkNeeded) {
                break;
            }
        }
        portions.removeAll(portionsToTakeOut);
        portions.removeAll(portionsToThrowOut);
        return portionsToTakeOut;
    }

    public MilkStorage copy() {
        MilkStorage copiedStorage = new MilkStorage();
        for (MilkPortion portion : portions) {
            copiedStorage.addPortion(portion);
        }
        return copiedStorage;
    }

    public boolean isEmpty(){
        return portions.isEmpty();
    }
}
