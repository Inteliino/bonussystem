package lv.janis.bonussystem.model;

import jakarta.persistence.*;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String tabelesNr;
    private String grade;
    private double coefficient;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTabelesNr() {
        return tabelesNr;
    }

    public String getGrade() {
        return grade;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTabelesNr(String tabelesNr) {
        this.tabelesNr = tabelesNr;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }
}