package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.EmployeeRepository;
import lv.janis.bonussystem.PendingWorkRecordRepository;
import lv.janis.bonussystem.WorkRecordRepository;
import lv.janis.bonussystem.model.AppUser;
import lv.janis.bonussystem.model.Employee;
import lv.janis.bonussystem.model.PendingWorkRecord;
import lv.janis.bonussystem.model.WorkRecord;
import lv.janis.bonussystem.service.BonusCalculatorService;
import lv.janis.bonussystem.service.ValmieraGlassImportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PendingRecordsController {

    private final PendingWorkRecordRepository pendingRepository;
    private final WorkRecordRepository workRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final BonusCalculatorService bonusCalculatorService;
    private final ValmieraGlassImportService valmieraGlassImportService;
    

    public PendingRecordsController(PendingWorkRecordRepository pendingRepository,
                                    WorkRecordRepository workRecordRepository,
                                    EmployeeRepository employeeRepository,
                                    BonusCalculatorService bonusCalculatorService,
                                    ValmieraGlassImportService valmieraGlassImportService) {
        this.pendingRepository = pendingRepository;
        this.workRecordRepository = workRecordRepository;
        this.employeeRepository = employeeRepository;
        this.bonusCalculatorService = bonusCalculatorService;
        this.valmieraGlassImportService = valmieraGlassImportService;
    }

    @GetMapping("/pending-records")
    public String pendingRecords(@RequestParam(required = false, defaultValue = "ALL") String shiftName,
                                 @RequestParam(required = false, defaultValue = "ALL") String defaultShiftName,
                                 @RequestParam(required = false, defaultValue = "PENDING") String status,
                                 Model model) {

        List<PendingWorkRecord> records = pendingRepository.findAllByOrderByDateDescImportedAtDesc();
        List<Employee> employees = employeeRepository.findAllByOrderByNameAsc();

        Map<String, Employee> employeeByTabelesNr = employees.stream()
                .filter(e -> e.getTabelesNr() != null)
                .collect(Collectors.toMap(
                        Employee::getTabelesNr,
                        e -> e,
                        (a, b) -> a
                ));

        records = records.stream()
                .filter(r -> "ALL".equals(status) || status.equalsIgnoreCase(r.getStatus()))
                .filter(r -> "ALL".equals(shiftName) || shiftName.equals(r.getShiftName()))
                .filter(r -> {
                    if ("ALL".equals(defaultShiftName)) {
                        return true;
                    }

                    Employee employee = employeeByTabelesNr.get(r.getTabelesNr());

                    if (employee == null) {
                        return false;
                    }

                    return defaultShiftName.equals(employee.getDefaultShiftName());
                })
                .toList();

        long pendingCount = pendingRepository.findAllByOrderByDateDescImportedAtDesc()
                .stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .count();

        model.addAttribute("records", records);
        model.addAttribute("employees", employees);
        model.addAttribute("editRecord", null);
        model.addAttribute("selectedShiftName", shiftName);
        model.addAttribute("selectedDefaultShiftName", defaultShiftName);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingCount", pendingCount);

        return "pending-records";
    }

    @PostMapping("/pending-records/import-valmiera")
    public String importFromValmieraGlass() {
        try {
            valmieraGlassImportService.importFromValmieraGlass();
        } catch (Exception e) {
            System.out.println("IMPORT ERROR:");
            e.printStackTrace();
        }

        return "redirect:/pending-records";
    }

    @GetMapping("/pending-records/edit/{id}")
    public String editRecord(@PathVariable Long id,
                             @RequestParam(required = false, defaultValue = "ALL") String shiftName,
                             @RequestParam(required = false, defaultValue = "ALL") String defaultShiftName,
                             @RequestParam(required = false, defaultValue = "PENDING") String status,
                             Model model) {

        pendingRecords(shiftName, defaultShiftName, status, model);
        model.addAttribute("editRecord", pendingRepository.findById(id).orElseThrow());

        return "pending-records";
    }

    @PostMapping("/pending-records/save")
    public String saveRecord(@RequestParam(required = false) Long id,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam String workPeriod,
                             @RequestParam String shiftName,
                             @RequestParam String tabelesNr,
                             @RequestParam String employeeName,
                             @RequestParam(defaultValue = "0") double roboti,
                             @RequestParam(defaultValue = "0") double cirsana,
                             @RequestParam(defaultValue = "0") double cirsanaPilnie,
                             @RequestParam(defaultValue = "0") double cirsanaNepilnie,
                             @RequestParam(defaultValue = "0") double pilnie,
                             @RequestParam(defaultValue = "0") double nepilnie,
                             @RequestParam(defaultValue = "0") double pirmaPakape,
                             @RequestParam(defaultValue = "0") double otraPakape,
                             @RequestParam(defaultValue = "0") double treshaPakape,
                             @RequestParam(defaultValue = "0") double ceturtaPakape) {

        PendingWorkRecord record = id != null
                ? pendingRepository.findById(id).orElseThrow()
                : new PendingWorkRecord();

        record.setDate(date);
        record.setWorkPeriod(workPeriod);
        record.setShiftName(shiftName);
        record.setTabelesNr(tabelesNr);
        record.setEmployeeName(employeeName);

        record.setRoboti(roboti);
        record.setCirsana(cirsana);
        record.setCirsanaPilnie(cirsanaPilnie);
        record.setCirsanaNepilnie(cirsanaNepilnie);
        record.setPilnie(pilnie);
        record.setNepilnie(nepilnie);
        record.setPirmaPakape(pirmaPakape);
        record.setOtraPakape(otraPakape);
        record.setTreshaPakape(treshaPakape);
        record.setCeturtaPakape(ceturtaPakape);

        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("PENDING");
        }

        pendingRepository.save(record);

        return "redirect:/pending-records";
    }

    @GetMapping("/pending-records/approve/{id}")
    public String approveRecord(@PathVariable Long id,
                                Authentication authentication,
                                HttpServletRequest request) {

        PendingWorkRecord pending = pendingRepository.findById(id).orElseThrow();

        if ("APPROVED".equals(pending.getStatus())) {
            return "redirect:/pending-records";
        }

        Employee employee = employeeRepository.findByTabelesNr(pending.getTabelesNr()).orElse(null);

        if (employee == null) {
            employee = new Employee();
            employee.setName(pending.getEmployeeName());
            employee.setTabelesNr(pending.getTabelesNr());
            employee.setDefaultShiftName(pending.getShiftName());
            employee.setActive(true);
            employeeRepository.save(employee);
        }

        WorkRecord record = new WorkRecord();

        record.setDate(pending.getDate());
        record.setShiftName(pending.getShiftName());
        record.setTabelesNr(pending.getTabelesNr());
        record.setEmployee(employee);

        record.setRoboti(pending.getRoboti());
        record.setCirsana(pending.getCirsana());
        record.setCirsanaPilnie(pending.getCirsanaPilnie());
        record.setCirsanaNepilnie(pending.getCirsanaNepilnie());
        record.setPilnie(pending.getPilnie());
        record.setNepilnie(pending.getNepilnie());
        record.setPirmaPakape(pending.getPirmaPakape());
        record.setOtraPakape(pending.getOtraPakape());
        record.setTreshaPakape(pending.getTreshaPakape());
        record.setCeturtaPakape(pending.getCeturtaPakape());

        bonusCalculatorService.calculateBonus(record);

        String username = getUsername(authentication, request);
        LocalDateTime approvedTime = LocalDateTime.now();

        record.setApproved(true);
        record.setApprovedBy(username);
        record.setApprovedAt(approvedTime);

        workRecordRepository.save(record);

        pending.setStatus("APPROVED");
        pending.setApprovedBy(username);
        pending.setApprovedAt(approvedTime);

        pendingRepository.save(pending);

        return "redirect:/pending-records";
    }

    @GetMapping("/pending-records/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        pendingRepository.deleteById(id);
        return "redirect:/pending-records";
    }

    private String getUsername(Authentication authentication, HttpServletRequest request) {
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object userObj = session.getAttribute("user");

            if (userObj instanceof AppUser user) {
                return user.getUsername();
            }

            Object loggedUserObj = session.getAttribute("loggedUser");

            if (loggedUserObj instanceof AppUser user) {
                return user.getUsername();
            }
        }

        return "SYSTEM";
    }
}