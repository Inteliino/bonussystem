package lv.janis.bonussystem;

import lv.janis.bonussystem.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByOrderByNameAsc();

    List<Employee> findByActiveTrueOrderByNameAsc();

    boolean existsByTabelesNr(String tabelesNr);

    boolean existsByTabelesNrAndIdNot(String tabelesNr, Long id);

    Optional<Employee> findByTabelesNr(String tabelesNr);
}