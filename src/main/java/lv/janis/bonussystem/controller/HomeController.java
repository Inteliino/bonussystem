package lv.janis.bonussystem.controller;

import lv.janis.bonussystem.AssortmentRepository;
import lv.janis.bonussystem.EmployeeRepository;
import lv.janis.bonussystem.WorkRecordRepository;
import lv.janis.bonussystem.model.Employee;
import lv.janis.bonussystem.model.WorkRecord;
import lv.janis.bonussystem.service.BonusCalculatorService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final EmployeeRepository employeeRepository;
    private final WorkRecordRepository workRecordRepository;
    private final AssortmentRepository assortmentRepository;
    private final BonusCalculatorService bonusCalculatorService;

    public HomeController(EmployeeRepository employeeRepository,
                          WorkRecordRepository workRecordRepository,
                          AssortmentRepository assortmentRepository,
                          BonusCalculatorService bonusCalculatorService) {
        this.employeeRepository = employeeRepository;
        this.workRecordRepository = workRecordRepository;
        this.assortmentRepository = assortmentRepository;
        this.bonusCalculatorService = bonusCalculatorService;
    }

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(required = false, defaultValue = "ALL") String shiftName,
                       @RequestParam(required = false) String month,
                       @RequestParam(required = false) Long summaryEmployeeId,
                       @RequestParam(required = false) String summaryMonth) {

        List<WorkRecord> allRecords = workRecordRepository.findAll();
        List<WorkRecord> records = new ArrayList<>(allRecords);

        if (shiftName == null || shiftName.isBlank()) {
            shiftName = "ALL";
        }

        if (!shiftName.equals("ALL")) {
            String selectedShift = shiftName;
            records = records.stream()
                    .filter(r -> selectedShift.equals(r.getShiftName()))
                    .toList();
        }

        if (month != null && !month.isBlank()) {
            YearMonth selectedMonth = YearMonth.parse(month);

            records = records.stream()
                    .filter(r -> r.getDate() != null)
                    .filter(r -> YearMonth.from(r.getDate()).equals(selectedMonth))
                    .toList();
        }

        double totalBonus = records.stream()
                .mapToDouble(WorkRecord::getBonus)
                .sum();

        Map<String, Double> monthlySummary = new LinkedHashMap<>();

        for (int i = 1; i <= 12; i++) {
            YearMonth ym = YearMonth.of(LocalDate.now().getYear(), i);

            double monthTotal = allRecords.stream()
                    .filter(r -> r.getDate() != null)
                    .filter(r -> YearMonth.from(r.getDate()).equals(ym))
                    .mapToDouble(WorkRecord::getBonus)
                    .sum();

            monthlySummary.put(ym.toString(), monthTotal);
        }

        double yearlyTotal = monthlySummary.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Double> shiftSummary = new LinkedHashMap<>();
        List<String> shifts = List.of("1. maiņa", "2. maiņa", "3. maiņa", "4. maiņa");

        for (String shift : shifts) {
            double shiftTotal = allRecords.stream()
                    .filter(r -> r.getShiftName() != null)
                    .filter(r -> r.getShiftName().equals(shift))
                    .filter(r -> {
                        if (month == null || month.isBlank()) {
                            return true;
                        }

                        return r.getDate() != null
                                && YearMonth.from(r.getDate()).equals(YearMonth.parse(month));
                    })
                    .mapToDouble(WorkRecord::getBonus)
                    .sum();

            shiftSummary.put(shift, shiftTotal);
        }

        Employee selectedSummaryEmployee = null;
        List<WorkRecord> employeeRecords = new ArrayList<>();
        double employeeTotalBonus = 0.0;

        int employeeShiftCount = 0;

        double avgRoboti = 0.0;
        double avgCirsana = 0.0;
        double avgPilnie = 0.0;
        double avgPirmaPakape = 0.0;
        double avgOtraPakape = 0.0;
        double avgTreshaPakape = 0.0;
        double avgCeturtaPakape = 0.0;
        double avgNepilnieKopa = 0.0;

        double totalNepilnieKopa = 0.0;

        if (summaryEmployeeId != null) {
            selectedSummaryEmployee = employeeRepository.findById(summaryEmployeeId).orElse(null);

            String selectedMonth = summaryMonth;

            employeeRecords = allRecords.stream()
                    .filter(r -> r.getEmployee() != null)
                    .filter(r -> r.getEmployee().getId().equals(summaryEmployeeId))
                    .filter(r -> {
                        if (selectedMonth == null || selectedMonth.isBlank()) {
                            return true;
                        }

                        return r.getDate() != null
                                && YearMonth.from(r.getDate()).equals(YearMonth.parse(selectedMonth));
                    })
                    .toList();

            employeeTotalBonus = employeeRecords.stream()
                    .mapToDouble(WorkRecord::getBonus)
                    .sum();

            employeeShiftCount = employeeRecords.size();

            double totalRoboti = employeeRecords.stream().mapToDouble(WorkRecord::getRoboti).sum();
            double totalCirsana = employeeRecords.stream().mapToDouble(WorkRecord::getCirsana).sum();
            double totalPilnie = employeeRecords.stream().mapToDouble(WorkRecord::getPilnie).sum();

            double totalPirmaPakape = employeeRecords.stream().mapToDouble(WorkRecord::getPirmaPakape).sum();
            double totalOtraPakape = employeeRecords.stream().mapToDouble(WorkRecord::getOtraPakape).sum();
            double totalTreshaPakape = employeeRecords.stream().mapToDouble(WorkRecord::getTreshaPakape).sum();
            double totalCeturtaPakape = employeeRecords.stream().mapToDouble(WorkRecord::getCeturtaPakape).sum();

            totalNepilnieKopa = totalPirmaPakape + totalOtraPakape + totalTreshaPakape + totalCeturtaPakape;

            if (employeeShiftCount > 0) {
                avgRoboti = totalRoboti / employeeShiftCount;
                avgCirsana = totalCirsana / employeeShiftCount;
                avgPilnie = totalPilnie / employeeShiftCount;

                avgPirmaPakape = totalPirmaPakape / employeeShiftCount;
                avgOtraPakape = totalOtraPakape / employeeShiftCount;
                avgTreshaPakape = totalTreshaPakape / employeeShiftCount;
                avgCeturtaPakape = totalCeturtaPakape / employeeShiftCount;

                avgNepilnieKopa = totalNepilnieKopa / employeeShiftCount;
            }
        }

        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("assortments", assortmentRepository.findByActiveTrue());
        model.addAttribute("records", records);

        model.addAttribute("totalBonus", totalBonus);

        model.addAttribute("monthlySummary", monthlySummary);
        model.addAttribute("yearlyTotal", yearlyTotal);

        model.addAttribute("shiftSummary", shiftSummary);

        model.addAttribute("selectedShift", shiftName);
        model.addAttribute("selectedMonth", month);

        model.addAttribute("summaryEmployeeId", summaryEmployeeId);
        model.addAttribute("selectedSummaryMonth", summaryMonth);
        model.addAttribute("selectedSummaryEmployee", selectedSummaryEmployee);
        model.addAttribute("employeeRecords", employeeRecords);
        model.addAttribute("employeeTotalBonus", employeeTotalBonus);

        model.addAttribute("employeeShiftCount", employeeShiftCount);
        model.addAttribute("avgRoboti", avgRoboti);
        model.addAttribute("avgCirsana", avgCirsana);
        model.addAttribute("avgPilnie", avgPilnie);
        model.addAttribute("avgPirmaPakape", avgPirmaPakape);
        model.addAttribute("avgOtraPakape", avgOtraPakape);
        model.addAttribute("avgTreshaPakape", avgTreshaPakape);
        model.addAttribute("avgCeturtaPakape", avgCeturtaPakape);
        model.addAttribute("avgNepilnieKopa", avgNepilnieKopa);
        model.addAttribute("totalNepilnieKopa", totalNepilnieKopa);

        return "index";
    }

    @PostMapping("/add-record")
    public String addRecord(@RequestParam Long employeeId,
                            @RequestParam String shiftName,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) String roboti,
                            @RequestParam(required = false) String cirsana,
                            @RequestParam(required = false) String pilnie,
                            @RequestParam(required = false) String pirmaPakape,
                            @RequestParam(required = false) String otraPakape,
                            @RequestParam(required = false) String treshaPakape,
                            @RequestParam(required = false) String ceturtaPakape) {

        Employee employee = employeeRepository.findById(employeeId).orElseThrow();

        WorkRecord record = new WorkRecord();

        record.setEmployee(employee);
        record.setTabelesNr(employee.getTabelesNr());
        record.setShiftName(shiftName);
        record.setDate(parseDate(date));

        record.setRoboti(parseDouble(roboti));
        record.setCirsana(parseDouble(cirsana));
        record.setPilnie(parseDouble(pilnie));
        record.setPirmaPakape(parseDouble(pirmaPakape));
        record.setOtraPakape(parseDouble(otraPakape));
        record.setTreshaPakape(parseDouble(treshaPakape));
        record.setCeturtaPakape(parseDouble(ceturtaPakape));

        bonusCalculatorService.calculate(record);

        workRecordRepository.save(record);

        return "redirect:/";
    }

    @GetMapping("/edit-record/{id}")
    public String editRecordPage(@PathVariable Long id, Model model) {
        WorkRecord record = workRecordRepository.findById(id).orElseThrow();

        model.addAttribute("record", record);
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("assortments", assortmentRepository.findByActiveTrue());

        return "edit-record";
    }

    @PostMapping("/edit-record/{id}")
    public String editRecord(@PathVariable Long id,
                             @RequestParam Long employeeId,
                             @RequestParam String shiftName,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) String roboti,
                             @RequestParam(required = false) String cirsana,
                             @RequestParam(required = false) String pilnie,
                             @RequestParam(required = false) String pirmaPakape,
                             @RequestParam(required = false) String otraPakape,
                             @RequestParam(required = false) String treshaPakape,
                             @RequestParam(required = false) String ceturtaPakape) {

        WorkRecord record = workRecordRepository.findById(id).orElseThrow();
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();

        record.setEmployee(employee);
        record.setTabelesNr(employee.getTabelesNr());
        record.setShiftName(shiftName);
        record.setDate(parseDate(date));

        record.setRoboti(parseDouble(roboti));
        record.setCirsana(parseDouble(cirsana));
        record.setPilnie(parseDouble(pilnie));
        record.setPirmaPakape(parseDouble(pirmaPakape));
        record.setOtraPakape(parseDouble(otraPakape));
        record.setTreshaPakape(parseDouble(treshaPakape));
        record.setCeturtaPakape(parseDouble(ceturtaPakape));

        bonusCalculatorService.calculate(record);

        workRecordRepository.save(record);

        return "redirect:/";
    }

    @GetMapping("/delete-record/{id}")
    public String deleteRecord(@PathVariable Long id) {
        workRecordRepository.deleteById(id);
        return "redirect:/";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }

        return LocalDate.parse(value);
    }

    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }

        return Double.parseDouble(value.replace(",", "."));
    }
}