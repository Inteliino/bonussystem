package lv.janis.bonussystem;

import lv.janis.bonussystem.model.GradeRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRuleRepository extends JpaRepository<GradeRule, Long> {
    GradeRule findByGradeName(String gradeName);
}