package lv.janis.bonussystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class WorkRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String shiftName;
    private String tabelesNr;

    private double roboti;
    private double cirsana;
    private double cirsanaPilnie;
    private double cirsanaNepilnie;
    private double pilnie;
    private double nepilnie;
    private double pirmaPakape;
    private double otraPakape;
    private double treshaPakape;
    private double ceturtaPakape;

    private double bonus;
    private double realRati;

    private boolean approved = false;
    private String approvedBy;
    private LocalDateTime approvedAt;

    @ManyToOne
    private Employee employee;

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getTabelesNr() {
        return tabelesNr;
    }

    public void setTabelesNr(String tabelesNr) {
        this.tabelesNr = tabelesNr;
    }

    public double getRoboti() {
        return roboti;
    }

    public void setRoboti(double roboti) {
        this.roboti = roboti;
    }

    public double getCirsana() {
        return cirsana;
    }

    public void setCirsana(double cirsana) {
        this.cirsana = cirsana;
    }

    public double getCirsanaPilnie() {
        return cirsanaPilnie;
    }

    public void setCirsanaPilnie(double cirsanaPilnie) {
        this.cirsanaPilnie = cirsanaPilnie;
    }

    public double getCirsanaNepilnie() {
        return cirsanaNepilnie;
    }

    public void setCirsanaNepilnie(double cirsanaNepilnie) {
        this.cirsanaNepilnie = cirsanaNepilnie;
    }

    public double getPilnie() {
        return pilnie;
    }

    public void setPilnie(double pilnie) {
        this.pilnie = pilnie;
    }

    public double getNepilnie() {
        return nepilnie;
    }

    public void setNepilnie(double nepilnie) {
        this.nepilnie = nepilnie;
    }

    public double getPirmaPakape() {
        return pirmaPakape;
    }

    public void setPirmaPakape(double pirmaPakape) {
        this.pirmaPakape = pirmaPakape;
    }

    public double getOtraPakape() {
        return otraPakape;
    }

    public void setOtraPakape(double otraPakape) {
        this.otraPakape = otraPakape;
    }

    public double getTreshaPakape() {
        return treshaPakape;
    }

    public void setTreshaPakape(double treshaPakape) {
        this.treshaPakape = treshaPakape;
    }

    public double getCeturtaPakape() {
        return ceturtaPakape;
    }

    public void setCeturtaPakape(double ceturtaPakape) {
        this.ceturtaPakape = ceturtaPakape;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public double getRealRati() {
        return realRati;
    }

    public void setRealRati(double realRati) {
        this.realRati = realRati;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}