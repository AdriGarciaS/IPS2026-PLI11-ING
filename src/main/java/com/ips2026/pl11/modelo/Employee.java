package com.ips2026.pl11.modelo;

import java.time.LocalDate;
import java.time.Period;

public class Employee {

	private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String nationalId;
    private final LocalDate birthDate;
    private final String phoneNumber;
    private final String category; // "SPORTS" or "NON_SPORTS"
    private final String position;
    private final double grossAnnualSalary;

    public Employee(Integer id, String firstName, String lastName, String nationalId,
                    LocalDate birthDate, String phoneNumber, String category,
                    String position, double grossAnnualSalary) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.category = category;
        this.position = position;
        this.grossAnnualSalary = grossAnnualSalary;
    }

    public boolean isUnderage() {
        if (birthDate == null) {
            return false;
        }
        return Period.between(birthDate, LocalDate.now()).getYears() < 18;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCategory() {
        return category;
    }

    public String getPosition() {
        return position;
    }

    public double getGrossAnnualSalary() {
        return grossAnnualSalary;
    }
}
