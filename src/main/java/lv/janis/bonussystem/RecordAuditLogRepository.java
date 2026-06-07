package lv.janis.bonussystem;

import lv.janis.bonussystem.model.RecordAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordAuditLogRepository extends JpaRepository<RecordAuditLog, Long> {

    List<RecordAuditLog> findTop200ByOrderByCreatedAtDesc();
}
