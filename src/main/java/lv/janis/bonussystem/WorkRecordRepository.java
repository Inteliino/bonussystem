package lv.janis.bonussystem;

import lv.janis.bonussystem.model.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    boolean existsByEmployee_Id(Long employeeId);

    boolean existsByEmployee_IdAndDateAndShiftName(
            Long employeeId,
            LocalDate date,
            String shiftName
    );
}