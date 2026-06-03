package lv.janis.bonussystem;

import lv.janis.bonussystem.model.Assortment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssortmentRepository extends JpaRepository<Assortment, Long> {
    List<Assortment> findByActiveTrue();
}