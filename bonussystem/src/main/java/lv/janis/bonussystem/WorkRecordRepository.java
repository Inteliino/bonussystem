package lv.janis.bonussystem;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.janis.bonussystem.model.WorkRecord;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {
}