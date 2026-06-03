package lv.janis.bonussystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bonus_rule")
public class BonusRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double normValue;
    private double stepValue;
    private double priceValue;
    private double overNormValue;
    private double overNormPrice;

    public BonusRule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getNormValue() {
        return normValue;
    }

    public void setNormValue(double normValue) {
        this.normValue = normValue;
    }

    public double getStepValue() {
        return stepValue;
    }

    public void setStepValue(double stepValue) {
        this.stepValue = stepValue;
    }

    public double getPriceValue() {
        return priceValue;
    }

    public void setPriceValue(double priceValue) {
        this.priceValue = priceValue;
    }

    public double getOverNormValue() {
        return overNormValue;
    }

    public void setOverNormValue(double overNormValue) {
        this.overNormValue = overNormValue;
    }

    public double getOverNormPrice() {
        return overNormPrice;
    }

    public void setOverNormPrice(double overNormPrice) {
        this.overNormPrice = overNormPrice;
    }
}