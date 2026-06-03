package lv.janis.bonussystem;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.janis.bonussystem.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}