package lv.janis.bonussystem;

import lv.janis.bonussystem.model.PendingWorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingWorkRecordRepository extends JpaRepository<PendingWorkRecord, Long> {

    List<PendingWorkRecord> findByStatusOrderByDateDescImportedAtDesc(String status);

    List<PendingWorkRecord> findAllByOrderByDateDescImportedAtDesc();
}