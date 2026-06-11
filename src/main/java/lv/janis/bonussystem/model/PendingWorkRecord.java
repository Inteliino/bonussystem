package lv.janis.bonussystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class PendingWorkRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    // 06:00-18:00 or 18:00-06:00
    private String workPeriod;

    private String shiftName;
    private String tabelesNr;
    private String employeeName;

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

    // PENDING, APPROVED, REJECTED
    private String status;

    // MANUAL, HTML
    private String importSource;

    private LocalDateTime importedAt;

    private String approvedBy;

    private LocalDateTime approvedAt;

    public PendingWorkRecord() {
        this.status = "PENDING";
        this.importSource = "MANUAL";
        this.importedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getWorkPeriod() {
        return workPeriod;
    }

    public String getShiftName() {
        return shiftName;
    }

    public String getTabelesNr() {
        return tabelesNr;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public double getRoboti() {
        return roboti;
    }

    public double getCirsana() {
        return cirsana;
    }

    public double getCirsanaPilnie() {
        return cirsanaPilnie;
    }

    public double getCirsanaNepilnie() {
        return cirsanaNepilnie;
    }

    public double getPilnie() {
        return pilnie;
    }

    public double getNepilnie() {
        return nepilnie;
    }

    public double getPirmaPakape() {
        return pirmaPakape;
    }

    public double getOtraPakape() {
        return otraPakape;
    }

    public double getTreshaPakape() {
        return treshaPakape;
    }

    public double getCeturtaPakape() {
        return ceturtaPakape;
    }

    public String getStatus() {
        return status;
    }

    public String getImportSource() {
        return importSource;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setWorkPeriod(String workPeriod) {
        this.workPeriod = workPeriod;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public void setTabelesNr(String tabelesNr) {
        this.tabelesNr = tabelesNr;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setRoboti(double roboti) {
        this.roboti = roboti;
    }

    public void setCirsana(double cirsana) {
        this.cirsana = cirsana;
    }

    public void setCirsanaPilnie(double cirsanaPilnie) {
        this.cirsanaPilnie = cirsanaPilnie;
    }

    public void setCirsanaNepilnie(double cirsanaNepilnie) {
        this.cirsanaNepilnie = cirsanaNepilnie;
    }

    public void setPilnie(double pilnie) {
        this.pilnie = pilnie;
    }

    public void setNepilnie(double nepilnie) {
        this.nepilnie = nepilnie;
    }

    public void setPirmaPakape(double pirmaPakape) {
        this.pirmaPakape = pirmaPakape;
    }

    public void setOtraPakape(double otraPakape) {
        this.otraPakape = otraPakape;
    }

    public void setTreshaPakape(double treshaPakape) {
        this.treshaPakape = treshaPakape;
    }

    public void setCeturtaPakape(double ceturtaPakape) {
        this.ceturtaPakape = ceturtaPakape;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImportSource(String importSource) {
        this.importSource = importSource;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}