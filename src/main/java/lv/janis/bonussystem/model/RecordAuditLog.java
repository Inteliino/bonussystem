package lv.janis.bonussystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class RecordAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private String username;
    private String action;
    private Long recordId;
    private String employeeName;
    private String shiftName;
    private LocalDate recordDate;

    @Column(length = 3000)
    private String oldValue;

    @Column(length = 3000)
    private String newValue;

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public Long getRecordId() { return recordId; }
    public String getEmployeeName() { return employeeName; }
    public String getShiftName() { return shiftName; }
    public LocalDate getRecordDate() { return recordDate; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }

    public void setId(Long id) { this.id = id; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUsername(String username) { this.username = username; }
    public void setAction(String action) { this.action = action; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}
