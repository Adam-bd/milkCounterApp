package pl.milkcounter.model;

import java.time.LocalDate;

public class MedicalAppointment {
    private LocalDate dateOfAppointment;
    private String description;

    public MedicalAppointment(LocalDate dateOfVisit, String description) {
        this.dateOfAppointment = dateOfVisit;
        this.description = description;
    }

    public LocalDate getDateOfAppointment() {
        return dateOfAppointment;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return dateOfAppointment + " - " + description;
    }
}
