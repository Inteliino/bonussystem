package lv.janis.bonussystem;

import lv.janis.bonussystem.model.GradeCoefficient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeCoefficientRepository extends JpaRepository<GradeCoefficient, Long> {

    Optional<GradeCoefficient> findByGradeName(String gradeName);
}