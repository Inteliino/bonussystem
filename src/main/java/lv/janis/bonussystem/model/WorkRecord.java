package lv.janis.bonussystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

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

    // Parastie nepilnie, koeficients 1.0
    private double nepilnie;

    private double pirmaPakape;
    private double otraPakape;
    private double treshaPakape;
    private double ceturtaPakape;

    private double realRati;
    private double bonus;

    @ManyToOne
    private Employee employee;

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getShiftName() { return shiftName; }
    public String getTabelesNr() { return tabelesNr; }

    public double getRoboti() { return roboti; }
    public double getCirsana() { return cirsana; }
    public double getCirsanaPilnie() { return cirsanaPilnie; }
    public double getCirsanaNepilnie() { return cirsanaNepilnie; }

    public double getPilnie() { return pilnie; }
    public double getNepilnie() { return nepilnie; }

    public double getPirmaPakape() { return pirmaPakape; }
    public double getOtraPakape() { return otraPakape; }
    public double getTreshaPakape() { return treshaPakape; }
    public double getCeturtaPakape() { return ceturtaPakape; }

    public double getRealRati() { return realRati; }
    public double getBonus() { return bonus; }
    public Employee getEmployee() { return employee; }

    public void setId(Long id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public void setTabelesNr(String tabelesNr) { this.tabelesNr = tabelesNr; }

    public void setRoboti(double roboti) { this.roboti = roboti; }
    public void setCirsana(double cirsana) { this.cirsana = cirsana; }
    public void setCirsanaPilnie(double cirsanaPilnie) { this.cirsanaPilnie = cirsanaPilnie; }
    public void setCirsanaNepilnie(double cirsanaNepilnie) { this.cirsanaNepilnie = cirsanaNepilnie; }

    public void setPilnie(double pilnie) { this.pilnie = pilnie; }
    public void setNepilnie(double nepilnie) { this.nepilnie = nepilnie; }

    public void setPirmaPakape(double pirmaPakape) { this.pirmaPakape = pirmaPakape; }
    public void setOtraPakape(double otraPakape) { this.otraPakape = otraPakape; }
    public void setTreshaPakape(double treshaPakape) { this.treshaPakape = treshaPakape; }
    public void setCeturtaPakape(double ceturtaPakape) { this.ceturtaPakape = ceturtaPakape; }

    public void setRealRati(double realRati) { this.realRati = realRati; }
    public void setBonus(double bonus) { this.bonus = bonus; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}