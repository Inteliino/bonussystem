package lv.janis.bonussystem;

import lv.janis.bonussystem.model.BonusRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusRuleRepository extends JpaRepository<BonusRule, Long> {
    BonusRule findByNameIgnoreCase(String name);
}