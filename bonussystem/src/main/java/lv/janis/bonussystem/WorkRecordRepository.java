package lv.janis.bonussystem;

import lv.janis.bonussystem.model.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    boolean existsByEmployee_Id(Long employeeId);
}