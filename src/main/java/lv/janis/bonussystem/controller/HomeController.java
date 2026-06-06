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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Long summaryEmployeeId,
                       @RequestParam(required = false) String summaryMonth) {

        int selectedYear = year != null ? year : LocalDate.now().getYear();

        List<Integer> years = new ArrayList<>();
        for (int y = LocalDate.now().getYear() - 3; y <= LocalDate.now().getYear() + 1; y++) {
            years.add(y);
        }

        List<WorkRecord> allRecords = workRecordRepository.findAll();
        List<WorkRecord> records = new ArrayList<>(allRecords);

        if (shiftName == null || shiftName.isBlank()) {
            shiftName = "ALL";
        }

        records = records.stream()
                .filter(r -> r.getDate() != null)
                .filter(r -> r.getDate().getYear() == selectedYear)
                .toList();

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

        records = records.stream()
                .sorted((a, b) -> {
                    if (a.getDate() == null && b.getDate() == null) return 0;
                    if (a.getDate() == null) return 1;
                    if (b.getDate() == null) return -1;
                    return b.getDate().compareTo(a.getDate());
                })
                .toList();

        double totalBonus = records.stream()
                .mapToDouble(WorkRecord::getBonus)
                .sum();

        Map<String, Double> monthlySummary = new LinkedHashMap<>();

        for (int i = 1; i <= 12; i++) {
            YearMonth ym = YearMonth.of(selectedYear, i);

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
                    .filter(r -> r.getDate() != null)
                    .filter(r -> r.getDate().getYear() == selectedYear)
                    .filter(r -> r.getShiftName() != null)
                    .filter(r -> r.getShiftName().equals(shift))
                    .filter(r -> {
                        if (month == null || month.isBlank()) {
                            return true;
                        }

                        return YearMonth.from(r.getDate()).equals(YearMonth.parse(month));
                    })
                    .mapToDouble(WorkRecord::getBonus)
                    .sum();

            shiftSummary.put(shift, shiftTotal);
        }

        Map<String, ShiftStats> shiftStats = new LinkedHashMap<>();

        for (String shift : shifts) {
            shiftStats.put(shift, new ShiftStats(shift));
        }

        List<WorkRecord> shiftCompareRecords = allRecords.stream()
                .filter(r -> r.getDate() != null)
                .filter(r -> r.getDate().getYear() == selectedYear)
                .filter(r -> r.getShiftName() != null)
                .filter(r -> {
                    if (month == null || month.isBlank()) {
                        return true;
                    }

                    return YearMonth.from(r.getDate()).equals(YearMonth.parse(month));
                })
                .toList();

        for (WorkRecord record : shiftCompareRecords) {
            ShiftStats stat = shiftStats.get(record.getShiftName());

            if (stat != null) {
                stat.addRecord(record);
            }
        }

        for (ShiftStats stat : shiftStats.values()) {
            stat.calculate();

            stat.setTopLeaders(
                    shiftCompareRecords.stream()
                            .filter(r -> stat.getShiftName().equals(r.getShiftName()))
                            .filter(r -> r.getEmployee() != null)
                            .collect(Collectors.groupingBy(
                                    r -> r.getEmployee().getName(),
                                    Collectors.summingDouble(WorkRecord::getBonus)
                            ))
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                            .limit(2)
                            .map(e -> new ShiftLeader(e.getKey(), e.getValue()))
                            .toList()
            );
        }

        Employee selectedSummaryEmployee = null;
        List<WorkRecord> employeeRecords = new ArrayList<>();

        double employeeTotalBonus = 0.0;
        int employeeShiftCount = 0;

        double avgRoboti = 0.0;
        double avgCirsana = 0.0;
        double avgCirsanaPilnie = 0.0;
        double avgCirsanaNepilnie = 0.0;
        double avgPilnie = 0.0;
        double avgNepilnie = 0.0;

        double avgPirmaPakape = 0.0;
        double avgOtraPakape = 0.0;
        double avgTreshaPakape = 0.0;
        double avgCeturtaPakape = 0.0;

        double totalNepilnieKopa = 0.0;

        if (summaryEmployeeId != null) {
            selectedSummaryEmployee = employeeRepository.findById(summaryEmployeeId).orElse(null);

            employeeRecords = allRecords.stream()
                    .filter(r -> r.getEmployee() != null)
                    .filter(r -> r.getEmployee().getId().equals(summaryEmployeeId))
                    .filter(r -> r.getDate() != null)
                    .filter(r -> {
                        if (summaryMonth == null || summaryMonth.isBlank()) {
                            return r.getDate().getYear() == selectedYear;
                        }

                        return YearMonth.from(r.getDate()).equals(YearMonth.parse(summaryMonth));
                    })
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .toList();

            employeeTotalBonus = employeeRecords.stream()
                    .mapToDouble(WorkRecord::getBonus)
                    .sum();

            employeeShiftCount = employeeRecords.size();

            double totalRoboti = employeeRecords.stream().mapToDouble(WorkRecord::getRoboti).sum();
            double totalCirsana = employeeRecords.stream().mapToDouble(WorkRecord::getCirsana).sum();
            double totalCirsanaPilnie = employeeRecords.stream().mapToDouble(WorkRecord::getCirsanaPilnie).sum();
            double totalCirsanaNepilnie = employeeRecords.stream().mapToDouble(WorkRecord::getCirsanaNepilnie).sum();

            double totalPilnie = employeeRecords.stream().mapToDouble(WorkRecord::getPilnie).sum();
            double totalNepilnie = employeeRecords.stream().mapToDouble(WorkRecord::getNepilnie).sum();

            double totalPirmaPakape = employeeRecords.stream().mapToDouble(WorkRecord::getPirmaPakape).sum();
            double totalOtraPakape = employeeRecords.stream().mapToDouble(WorkRecord::getOtraPakape).sum();
            double totalTreshaPakape = employeeRecords.stream().mapToDouble(WorkRecord::getTreshaPakape).sum();
            double totalCeturtaPakape = employeeRecords.stream().mapToDouble(WorkRecord::getCeturtaPakape).sum();

            totalNepilnieKopa = totalNepilnie
                    + totalCirsanaNepilnie
                    + totalPirmaPakape
                    + totalOtraPakape
                    + totalTreshaPakape
                    + totalCeturtaPakape;

            long robotiCount = employeeRecords.stream().filter(r -> r.getRoboti() > 0).count();
            long cirsanaCount = employeeRecords.stream().filter(r -> r.getCirsana() > 0).count();
            long cirsanaPilnieCount = employeeRecords.stream().filter(r -> r.getCirsanaPilnie() > 0).count();
            long cirsanaNepilnieCount = employeeRecords.stream().filter(r -> r.getCirsanaNepilnie() > 0).count();

            long pilnieCount = employeeRecords.stream().filter(r -> r.getPilnie() > 0).count();
            long nepilnieCount = employeeRecords.stream().filter(r -> r.getNepilnie() > 0).count();

            long pirmaPakapeCount = employeeRecords.stream().filter(r -> r.getPirmaPakape() > 0).count();
            long otraPakapeCount = employeeRecords.stream().filter(r -> r.getOtraPakape() > 0).count();
            long treshaPakapeCount = employeeRecords.stream().filter(r -> r.getTreshaPakape() > 0).count();
            long ceturtaPakapeCount = employeeRecords.stream().filter(r -> r.getCeturtaPakape() > 0).count();

            avgRoboti = robotiCount == 0 ? 0 : totalRoboti / robotiCount;
            avgCirsana = cirsanaCount == 0 ? 0 : totalCirsana / cirsanaCount;
            avgCirsanaPilnie = cirsanaPilnieCount == 0 ? 0 : totalCirsanaPilnie / cirsanaPilnieCount;
            avgCirsanaNepilnie = cirsanaNepilnieCount == 0 ? 0 : totalCirsanaNepilnie / cirsanaNepilnieCount;

            avgPilnie = pilnieCount == 0 ? 0 : totalPilnie / pilnieCount;
            avgNepilnie = nepilnieCount == 0 ? 0 : totalNepilnie / nepilnieCount;

            avgPirmaPakape = pirmaPakapeCount == 0 ? 0 : totalPirmaPakape / pirmaPakapeCount;
            avgOtraPakape = otraPakapeCount == 0 ? 0 : totalOtraPakape / otraPakapeCount;
            avgTreshaPakape = treshaPakapeCount == 0 ? 0 : totalTreshaPakape / treshaPakapeCount;
            avgCeturtaPakape = ceturtaPakapeCount == 0 ? 0 : totalCeturtaPakape / ceturtaPakapeCount;
        }

        model.addAttribute("employees", employeeRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("assortments", assortmentRepository.findByActiveTrue());
        model.addAttribute("records", records);

        model.addAttribute("totalBonus", totalBonus);

        model.addAttribute("monthlySummary", monthlySummary);
        model.addAttribute("yearlyTotal", yearlyTotal);

        model.addAttribute("shiftSummary", shiftSummary);
        model.addAttribute("shiftStats", shiftStats);

        model.addAttribute("selectedShift", shiftName);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("years", years);

        model.addAttribute("summaryEmployeeId", summaryEmployeeId);
        model.addAttribute("selectedSummaryMonth", summaryMonth);
        model.addAttribute("selectedSummaryEmployee", selectedSummaryEmployee);
        model.addAttribute("employeeRecords", employeeRecords);
        model.addAttribute("employeeTotalBonus", employeeTotalBonus);

        model.addAttribute("employeeShiftCount", employeeShiftCount);
        model.addAttribute("avgRoboti", avgRoboti);
        model.addAttribute("avgCirsana", avgCirsana);
        model.addAttribute("avgCirsanaPilnie", avgCirsanaPilnie);
        model.addAttribute("avgCirsanaNepilnie", avgCirsanaNepilnie);
        model.addAttribute("avgPilnie", avgPilnie);
        model.addAttribute("avgNepilnie", avgNepilnie);

        model.addAttribute("avgPirmaPakape", avgPirmaPakape);
        model.addAttribute("avgOtraPakape", avgOtraPakape);
        model.addAttribute("avgTreshaPakape", avgTreshaPakape);
        model.addAttribute("avgCeturtaPakape", avgCeturtaPakape);
        model.addAttribute("totalNepilnieKopa", totalNepilnieKopa);

        return "index";
    }

    @PostMapping("/add-record")
    public String addRecord(@RequestParam Long employeeId,
                            @RequestParam String shiftName,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) List<String> workType,
                            @RequestParam(required = false) List<String> amount) {

        Employee employee = employeeRepository.findById(employeeId).orElseThrow();

        WorkRecord record = new WorkRecord();

        record.setEmployee(employee);
        record.setTabelesNr(employee.getTabelesNr());
        record.setShiftName(shiftName);
        record.setDate(parseDate(date));

        if (workType != null && amount != null) {
            for (int i = 0; i < workType.size(); i++) {

                String type = workType.get(i);
                double value = 0.0;

                if (i < amount.size()) {
                    value = parseDouble(amount.get(i));
                }

                if (type == null || type.isBlank() || value == 0.0) {
                    continue;
                }

                switch (type) {
                    case "ROBOTS" -> record.setRoboti(record.getRoboti() + value);
                    case "CIRSANA" -> record.setCirsana(record.getCirsana() + value);
                    case "CIRSANA_PILNIE" -> record.setCirsanaPilnie(record.getCirsanaPilnie() + value);
                    case "CIRSANA_NEPILNIE" -> record.setCirsanaNepilnie(record.getCirsanaNepilnie() + value);
                    case "PILNIE" -> record.setPilnie(record.getPilnie() + value);
                    case "NEPILNIE" -> record.setNepilnie(record.getNepilnie() + value);
                    case "PAKAPE_I" -> record.setPirmaPakape(record.getPirmaPakape() + value);
                    case "PAKAPE_II" -> record.setOtraPakape(record.getOtraPakape() + value);
                    case "PAKAPE_III" -> record.setTreshaPakape(record.getTreshaPakape() + value);
                    case "PAKAPE_IV" -> record.setCeturtaPakape(record.getCeturtaPakape() + value);
                }
            }
        }

        bonusCalculatorService.calculateBonus(record);
        workRecordRepository.save(record);

        return "redirect:/";
    }

    @GetMapping("/edit-record/{id}")
    public String editRecordPage(@PathVariable Long id, Model model) {
        WorkRecord record = workRecordRepository.findById(id).orElseThrow();

        model.addAttribute("record", record);
        model.addAttribute("employees", employeeRepository.findByActiveTrueOrderByNameAsc());
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
                             @RequestParam(required = false) String cirsanaPilnie,
                             @RequestParam(required = false) String cirsanaNepilnie,
                             @RequestParam(required = false) String pilnie,
                             @RequestParam(required = false) String nepilnie,
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
        record.setCirsanaPilnie(parseDouble(cirsanaPilnie));
        record.setCirsanaNepilnie(parseDouble(cirsanaNepilnie));

        record.setPilnie(parseDouble(pilnie));
        record.setNepilnie(parseDouble(nepilnie));

        record.setPirmaPakape(parseDouble(pirmaPakape));
        record.setOtraPakape(parseDouble(otraPakape));
        record.setTreshaPakape(parseDouble(treshaPakape));
        record.setCeturtaPakape(parseDouble(ceturtaPakape));

        bonusCalculatorService.calculateBonus(record);
        workRecordRepository.save(record);

        return "redirect:/";
    }

    @GetMapping("/delete-record/{id}")
    public String deleteRecord(@PathVariable Long id) {
        workRecordRepository.deleteById(id);
        return "redirect:/";
    }

    @PostMapping("/recalculate-bonuses")
    public String recalculateBonuses() {

        List<WorkRecord> records = workRecordRepository.findAll();

        for (WorkRecord record : records) {
            bonusCalculatorService.calculateBonus(record);
            workRecordRepository.save(record);
        }

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

    public static class ShiftLeader {
        private String name;
        private double bonus;

        public ShiftLeader(String name, double bonus) {
            this.name = name;
            this.bonus = bonus;
        }

        public String getName() {
            return name;
        }

        public double getBonus() {
            return bonus;
        }
    }

    public static class ShiftStats {

        private String shiftName;
        private int shiftCount;

        private double totalBonus;
        private double avgRoboti;
        private double avgPilnie;
        private double avgNepilnie;
        private double avgPirmaPakape;
        private double avgOtraPakape;
        private double avgTreshaPakape;
        private double avgCeturtaPakape;

        private double totalRoboti;
        private double totalPilnie;
        private double totalNepilnie;
        private double totalPirmaPakape;
        private double totalOtraPakape;
        private double totalTreshaPakape;
        private double totalCeturtaPakape;

        private int robotiCount;
        private int pilnieCount;
        private int nepilnieCount;
        private int pirmaCount;
        private int otraCount;
        private int treshaCount;
        private int ceturtaCount;

        private List<ShiftLeader> topLeaders = new ArrayList<>();

        public ShiftStats(String shiftName) {
            this.shiftName = shiftName;
        }

        public void addRecord(WorkRecord record) {
            shiftCount++;
            totalBonus += record.getBonus();

            if (record.getRoboti() > 0) {
                totalRoboti += record.getRoboti();
                robotiCount++;
            }

            if (record.getPilnie() > 0) {
                totalPilnie += record.getPilnie();
                pilnieCount++;
            }

            if (record.getNepilnie() > 0) {
                totalNepilnie += record.getNepilnie();
                nepilnieCount++;
            }

            if (record.getPirmaPakape() > 0) {
                totalPirmaPakape += record.getPirmaPakape();
                pirmaCount++;
            }

            if (record.getOtraPakape() > 0) {
                totalOtraPakape += record.getOtraPakape();
                otraCount++;
            }

            if (record.getTreshaPakape() > 0) {
                totalTreshaPakape += record.getTreshaPakape();
                treshaCount++;
            }

            if (record.getCeturtaPakape() > 0) {
                totalCeturtaPakape += record.getCeturtaPakape();
                ceturtaCount++;
            }
        }

        public void calculate() {
            avgRoboti = robotiCount == 0 ? 0 : totalRoboti / robotiCount;
            avgPilnie = pilnieCount == 0 ? 0 : totalPilnie / pilnieCount;
            avgNepilnie = nepilnieCount == 0 ? 0 : totalNepilnie / nepilnieCount;
            avgPirmaPakape = pirmaCount == 0 ? 0 : totalPirmaPakape / pirmaCount;
            avgOtraPakape = otraCount == 0 ? 0 : totalOtraPakape / otraCount;
            avgTreshaPakape = treshaCount == 0 ? 0 : totalTreshaPakape / treshaCount;
            avgCeturtaPakape = ceturtaCount == 0 ? 0 : totalCeturtaPakape / ceturtaCount;
        }

        public String getShiftName() {
            return shiftName;
        }

        public int getShiftCount() {
            return shiftCount;
        }

        public double getTotalBonus() {
            return totalBonus;
        }

        public double getAvgRoboti() {
            return avgRoboti;
        }

        public double getAvgPilnie() {
            return avgPilnie;
        }

        public double getAvgNepilnie() {
            return avgNepilnie;
        }

        public double getAvgPirmaPakape() {
            return avgPirmaPakape;
        }

        public double getAvgOtraPakape() {
            return avgOtraPakape;
        }

        public double getAvgTreshaPakape() {
            return avgTreshaPakape;
        }

        public double getAvgCeturtaPakape() {
            return avgCeturtaPakape;
        }

        public List<ShiftLeader> getTopLeaders() {
            return topLeaders;
        }

        public void setTopLeaders(List<ShiftLeader> topLeaders) {
            this.topLeaders = topLeaders;
        }
    }
}