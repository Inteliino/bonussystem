package lv.janis.bonussystem.controller;

import lv.janis.bonussystem.EmployeeRepository;
import lv.janis.bonussystem.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;

@Controller
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Double.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(0.0);
                } else {
                    setValue(Double.parseDouble(text.replace(",", ".")));
                }
            }
        });

        binder.registerCustomEditor(double.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(0.0);
                } else {
                    setValue(Double.parseDouble(text.replace(",", ".")));
                }
            }
        });
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        return "employee";
    }

    @PostMapping("/employees/add")
    public String addEmployee(@RequestParam String name,
                              @RequestParam String tabelesNr,
                              @RequestParam String grade,
                              @RequestParam double coefficient) {

        Employee employee = new Employee();

        employee.setName(name);
        employee.setTabelesNr(tabelesNr);
        employee.setGrade(grade);
        employee.setCoefficient(coefficient);

        employeeRepository.save(employee);

        return "redirect:/employees";
    }

    @PostMapping("/employees/edit/{id}")
    public String editEmployee(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String tabelesNr,
                               @RequestParam String grade,
                               @RequestParam double coefficient) {

        Employee employee = employeeRepository.findById(id).orElseThrow();

        employee.setName(name);
        employee.setTabelesNr(tabelesNr);
        employee.setGrade(grade);
        employee.setCoefficient(coefficient);

        employeeRepository.save(employee);

        return "redirect:/employees";
    }

    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return "redirect:/employees";
    }
}