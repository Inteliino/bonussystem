package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.EmployeeRepository;
import lv.janis.bonussystem.WorkRecordRepository;
import lv.janis.bonussystem.model.AppUser;
import lv.janis.bonussystem.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final WorkRecordRepository workRecordRepository;

    public EmployeeController(EmployeeRepository employeeRepository,
                              WorkRecordRepository workRecordRepository) {
        this.employeeRepository = employeeRepository;
        this.workRecordRepository = workRecordRepository;
    }

    @GetMapping("/employees")
    public String employees(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String success,
                            Model model,
                            HttpSession session) {

        model.addAttribute("employees", employeeRepository.findAllByOrderByNameAsc());
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        model.addAttribute("isAdmin", isAdmin(session));

        return "employee";
    }

    @PostMapping("/employees/add")
    public String addEmployee(@RequestParam String name,
                              @RequestParam String tabelesNr,
                              @RequestParam(required = false) String defaultShiftName,
                              HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/employees?error=onlyAdmin";
        }

        if (employeeRepository.existsByTabelesNr(tabelesNr)) {
            return "redirect:/employees?error=tabelesExists";
        }

        Employee employee = new Employee();
        employee.setName(name);
        employee.setTabelesNr(tabelesNr);
        employee.setDefaultShiftName(defaultShiftName);
        employee.setActive(true);

        employeeRepository.save(employee);

        return "redirect:/employees?success=added";
    }

    @PostMapping("/employees/edit/{id}")
    public String editEmployee(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String tabelesNr,
                               @RequestParam(required = false) String defaultShiftName,
                               HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/employees?error=onlyAdmin";
        }

        if (employeeRepository.existsByTabelesNrAndIdNot(tabelesNr, id)) {
            return "redirect:/employees?error=tabelesExists";
        }

        Employee employee = employeeRepository.findById(id).orElseThrow();

        employee.setName(name);
        employee.setTabelesNr(tabelesNr);
        employee.setDefaultShiftName(defaultShiftName);

        employeeRepository.save(employee);

        return "redirect:/employees?success=saved";
    }

    @PostMapping("/employees/toggle-active/{id}")
    public String toggleActive(@PathVariable Long id,
                               HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/employees?error=onlyAdmin";
        }

        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setActive(!employee.isActive());

        employeeRepository.save(employee);

        return "redirect:/employees?success=statusChanged";
    }

    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id,
                                 HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/employees?error=onlyAdmin";
        }

        if (workRecordRepository.existsByEmployee_Id(id)) {
            return "redirect:/employees?error=hasRecords";
        }

        employeeRepository.deleteById(id);

        return "redirect:/employees?success=deleted";
    }

    private boolean isAdmin(HttpSession session) {
        Object userObject = session.getAttribute("loggedUser");

        if (!(userObject instanceof AppUser user)) {
            return false;
        }

        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
}