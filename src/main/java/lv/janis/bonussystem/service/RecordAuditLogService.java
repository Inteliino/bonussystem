package lv.janis.bonussystem.service;

import lv.janis.bonussystem.RecordAuditLogRepository;
import lv.janis.bonussystem.model.RecordAuditLog;
import lv.janis.bonussystem.model.WorkRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RecordAuditLogService {

    private final RecordAuditLogRepository recordAuditLogRepository;

    public RecordAuditLogService(RecordAuditLogRepository recordAuditLogRepository) {
        this.recordAuditLogRepository = recordAuditLogRepository;
    }

    public void logCreated(String username, WorkRecord record) {
        save(username, "IZVEIDOTS", record, "-", createSnapshot(record));
    }

    public void logEdited(String username, WorkRecord record, String oldSnapshot) {
        String newSnapshot = createSnapshot(record);

        if (!oldSnapshot.equals(newSnapshot)) {
            save(username, "LABOTS", record, oldSnapshot, newSnapshot);
        }
    }

    public void logDeleted(String username, WorkRecord record) {
        save(username, "DZĒSTS", record, createSnapshot(record), "Ieraksts dzēsts");
    }

    public String createSnapshot(WorkRecord record) {
        String employeeName = record.getEmployee() != null ? record.getEmployee().getName() : "-";

        return "Darbinieks: " + employeeName
                + " | Datums: " + safe(record.getDate())
                + " | Maiņa: " + safe(record.getShiftName())
                + " | Tabeles Nr: " + safe(record.getTabelesNr())
                + " | Roboti: " + record.getRoboti()
                + " | Ciršana: " + record.getCirsana()
                + " | C. pilnie: " + record.getCirsanaPilnie()
                + " | C. nepilnie: " + record.getCirsanaNepilnie()
                + " | Pilnie: " + record.getPilnie()
                + " | Nepilnie: " + record.getNepilnie()
                + " | I pak.: " + record.getPirmaPakape()
                + " | II pak.: " + record.getOtraPakape()
                + " | III pak.: " + record.getTreshaPakape()
                + " | IV pak.: " + record.getCeturtaPakape()
                + " | Bonuss: " + record.getBonus();
    }

    private void save(String username, String action, WorkRecord record, String oldValue, String newValue) {
        RecordAuditLog log = new RecordAuditLog();

        log.setCreatedAt(LocalDateTime.now());
        log.setUsername(username == null || username.isBlank() ? "SYSTEM" : username);
        log.setAction(action);
        log.setRecordId(record.getId());
        log.setEmployeeName(record.getEmployee() != null ? record.getEmployee().getName() : "-");
        log.setShiftName(record.getShiftName());
        log.setRecordDate(record.getDate());
        log.setOldValue(oldValue);
        log.setNewValue(newValue);

        recordAuditLogRepository.save(log);
    }

    private String safe(Object value) {
        return value == null ? "-" : value.toString();
    }
}
