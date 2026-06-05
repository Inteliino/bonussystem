package lv.janis.bonussystem;

import lv.janis.bonussystem.model.BonusRuleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonusRuleHistoryRepository extends JpaRepository<BonusRuleHistory, Long> {

    List<BonusRuleHistory> findAllByOrderByChangedAtDesc();
}