package lv.janis.bonussystem.model;

import jakarta.persistence.*;

@Entity
public class GradeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gradeName;
    private double coefficient;
    private double pricePerUnit;

    public Long getId() { return id; }

    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
}