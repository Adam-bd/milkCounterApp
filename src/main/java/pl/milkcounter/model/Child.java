package pl.milkcounter.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

public class Child {
    private final LocalDate birthDate;
    private float babyWeight;

    private List<Integer> historyOfVolumeCount = new ArrayList<>();
    private List<Integer> historyOfBottleCount = new ArrayList<>();

    public Child(LocalDate birthDate, float babyWeight) {
        this.birthDate = birthDate;
        this.babyWeight = babyWeight;
    }

    public void setBabyWeight(float babyWeight) {
        this.babyWeight = babyWeight;
    }

    public void addDailyLog(int totalMilkDrunk, int numberOfFeedings) {
        historyOfVolumeCount.add(totalMilkDrunk);
        historyOfBottleCount.add(numberOfFeedings);

        //średnia (wypitego mleka dziennie) z ostatnich 7 dni
        if (historyOfVolumeCount.size() > 7) {
            historyOfVolumeCount.removeFirst();
            historyOfBottleCount.removeFirst();
        }
    }

    private int calculateAveragePortionSize() {
        if (historyOfVolumeCount.isEmpty()) {
            return 150;
        }

        long totalMilkSum = 0;
        long totalFeedingsSum = 0;

        //sumujemy całe mleko z tygodnia
        for (int vol : historyOfVolumeCount) {
            totalMilkSum += vol;
        }
        //sumujemy wszystkie karmienia z tygodnia
        for (int count : historyOfBottleCount) {
            totalFeedingsSum += count;
        }

        //średnia
        if (totalFeedingsSum == 0) {
            return 150; //zabezpieczenie przed dzieleniem przez 0
        }

        return (int) (totalMilkSum / totalFeedingsSum);
    }

    public int dailyDemandOfMilk() {
        if (historyOfVolumeCount.isEmpty()){
            return round(babyWeight * 150);
        }

        int sum = 0;
        for(int amount : historyOfVolumeCount){
            sum += amount;
        }
        return sum / historyOfVolumeCount.size();
    }

    public int getDemandForBabyAge(LocalDate todaysDate) {
        Period age = Period.between(birthDate, todaysDate);
        int ageInMonths = age.getMonths() + (age.getYears() * 12);

        int currentDemand = dailyDemandOfMilk();
        int bottleSize = calculateAveragePortionSize();
        if (ageInMonths < 6) {
            //mniej niż 6 miesięcy jest na mleku matki lub modyfikowanym
            return currentDemand;
        }
        else if (ageInMonths <= 8) {
            // -8 miesiąc: Odchodzi 1 butelka (zupka)
            return Math.max(0, currentDemand - bottleSize);
        }
        else if (ageInMonths <= 10) {
            //9-10 miesiąc: Odchodzą 2 butelki
            return Math.max(0, currentDemand - (2 * bottleSize));
        }
        else {
            // >10 miesiąca: Odchodzą 3 butelki
            return Math.max(0, currentDemand - (3 * bottleSize));
        }
    }
}
