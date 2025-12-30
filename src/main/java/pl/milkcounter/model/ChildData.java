package pl.milkcounter.model;

import java.time.LocalDate;

public class ChildData extends Child {
    private LocalDate lastSaveDate;
    private int savedDailySum;
    private int savedBottlesCount;
    private String gender;

    public ChildData(String name, LocalDate birthDate, float babyWeight, LocalDate lastSaveDate, int savedDailySum, int savedBottlesCount, String gender) {
        super(name, birthDate, babyWeight);
        this.lastSaveDate = lastSaveDate;
        this.savedDailySum = savedDailySum;
        this.savedBottlesCount = savedBottlesCount;
        this.gender = gender;
    }

    public LocalDate getLastSaveDate() {
        return lastSaveDate;
    }

    public int getSavedDailySum() {
        return savedDailySum;
    }

    public int getSavedBottlesCount() {
        return savedBottlesCount;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}