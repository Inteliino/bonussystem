package lv.janis.bonussystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BonusRuleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String changedBy;
    private LocalDateTime changedAt;

    private String objectType;
    private String objectName;
    private String fieldName;

    private String oldValue;
    private String newValue;

    public BonusRuleHistory() {
    }

    public BonusRuleHistory(String changedBy, String objectType, String objectName,
                            String fieldName, String oldValue, String newValue) {
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
        this.objectType = objectType;
        this.objectName = objectName;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() {
        return id;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}