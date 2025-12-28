package pl.milkcounter.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

public class Child {
    private final LocalDate birthDate;
    private float babyWeight;
    private List<Integer> historyOfConsumption = new ArrayList<>();

    public Child(LocalDate birthDate, float babyWeight) {
        this.birthDate = birthDate;
        this.babyWeight = babyWeight;
    }

    public void setBabyWeight(float babyWeight) {
        this.babyWeight = babyWeight;
    }

    public void addDailyLog(int totalMilkDrunk) {
        historyOfConsumption.add(totalMilkDrunk);

        //średnia (wypitego mleka dziennie) z ostatnich 7 dni
        if (historyOfConsumption.size() > 7) {
            historyOfConsumption.removeFirst();
        }
    }

    public int dailyDemandOfMilk() {
        if (historyOfConsumption.isEmpty()){
            return round(babyWeight * 150); //150ml na kg masy ciała
        }

        int sum = 0;
        for(int amount : historyOfConsumption){
            sum += amount;
        }
        return sum / historyOfConsumption.size();
    }
}
