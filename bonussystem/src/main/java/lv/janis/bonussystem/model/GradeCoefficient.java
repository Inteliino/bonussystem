package lv.janis.bonussystem.model;

import jakarta.persistence.*;

@Entity
public class GradeCoefficient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gradeName;
    private double coefficient;
    private double eurPerUnit;

    public Long getId() {
        return id;
    }

    public String getGradeName() {
        return gradeName;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public double getEurPerUnit() {
        return eurPerUnit;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public void setEurPerUnit(double eurPerUnit) {
        this.eurPerUnit = eurPerUnit;
    }
}