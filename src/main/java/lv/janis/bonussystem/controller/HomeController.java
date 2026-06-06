package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.AssortmentRepository;
import lv.janis.bonussystem.EmployeeRepository;
import lv.janis.bonussystem.WorkRecordRepository;
import lv.janis.bonussystem.model.Employee;
import lv.janis.bonussystem.model.WorkRecord;
import lv.janis.bonussystem.service.BonusCalculatorService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
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
                       HttpSession session,
                       @RequestParam(required = false, defaultValue = "ALL") String shiftName,
                       @RequestParam(required = false) String month,
                       @RequestParam(required = false) String shiftCompareMonth,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Long summaryEmployeeId,
                       @RequestParam(required = false) String summaryMonth) {

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        int selectedYear = year != null ? year : today.getYear();

        String selectedMonthValue = (month == null || month.isBlank())
                ? currentMonth.toString()
                : month;

        String selectedShiftCompareMonthValue = (shiftCompareMonth == null || shiftCompareMonth.isBlank())
                ? currentMonth.toString()
                : shiftCompareMonth;

        String selectedSummaryMonthValue = (summaryMonth == null || summaryMonth.isBlank())
                ? currentMonth.toString()
                : summaryMonth;

        boolean isAdmin = isAdmin(session);

        List<Integer> years = new ArrayList<>();
        for (int y = today.getYear() - 3; y <= today.getYear() + 1; y++) {
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
                .filter(r -> YearMonth.from(r.getDate()).equals(YearMonth.parse(selectedMonthValue)))
                .toList();

        if (!shiftName.equals("ALL")) {
            String selectedShift = shiftName;
            records = records.stream()
                    .filter(r -> selectedShift.equals(r.getShiftName()))
                    .toList();
        }

        records = records.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
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

        YearMonth selectedShiftCompareMonth = YearMonth.parse(selectedShiftCompareMonthValue);

        for (String shift : shifts) {
            double shiftTotal = allRecords.stream()
                    .filter(r -> r.getDate() != null)
                    .filter(r -> YearMonth.from(r.getDate()).equals(selectedShiftCompareMonth))
                    .filter(r -> r.getShiftName() != null)
                    .filter(r -> r.getShiftName().equals(shift))
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
                .filter(r -> YearMonth.from(r.getDate()).equals(selectedShiftCompareMonth))
                .filter(r -> r.getShiftName() != null)
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

        Map<String, PositionBonusStats> positionBonusStats = createPositionBonusStats(shiftCompareRecords, shifts);

        List<PositionBonusStats> positionBonusRanking = positionBonusStats.values()
                .stream()
                .sorted(Comparator.comparingDouble(PositionBonusStats::getAverageBonus).reversed())
                .toList();

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

            YearMonth selectedSummaryYearMonth = YearMonth.parse(selectedSummaryMonthValue);

            employeeRecords = allRecords.stream()
                    .filter(r -> r.getEmployee() != null)
                    .filter(r -> r.getEmployee().getId().equals(summaryEmployeeId))
                    .filter(r -> r.getDate() != null)
                    .filter(r -> YearMonth.from(r.getDate()).equals(selectedSummaryYearMonth))
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .toList();

            employeeTotalBonus = employeeRecords.stream()
                    .mapToDouble(WorkRecord::getBonus)
                    .sum();

            employeeShiftCount = countUniqueShifts(employeeRecords);

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

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("todayDate", today.toString());
        model.addAttribute("currentMonth", currentMonth.toString());

        model.addAttribute("totalBonus", totalBonus);

        model.addAttribute("monthlySummary", monthlySummary);
        model.addAttribute("yearlyTotal", yearlyTotal);

        model.addAttribute("shiftSummary", shiftSummary);
        model.addAttribute("shiftStats", shiftStats);

        model.addAttribute("positionBonusStats", positionBonusStats);
        model.addAttribute("positionBonusRanking", positionBonusRanking);

        model.addAttribute("selectedShift", shiftName);
        model.addAttribute("selectedMonth", selectedMonthValue);
        model.addAttribute("selectedShiftCompareMonth", selectedShiftCompareMonthValue);
        model.addAttribute("selectedSummaryMonth", selectedSummaryMonthValue);
        model.addAttribute("years", years);

        model.addAttribute("summaryEmployeeId", summaryEmployeeId);
        model.addAttribute("selectedSummaryEmployee", selectedSummaryEmployee);
        model.addAttribute("selectedYear", selectedYear);
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

    private Map<String, PositionBonusStats> createPositionBonusStats(List<WorkRecord> records, List<String> shifts) {
        Map<String, PositionBonusStats> map = new LinkedHashMap<>();

        map.put("Robots", new PositionBonusStats("Robots", "🤖", "ROBOTS", shifts));
        map.put("Ciršana", new PositionBonusStats("Ciršana", "✂️", "CIRSANA", shifts));
        map.put("Pilnie", new PositionBonusStats("Pilnie", "📦", "PILNIE", shifts));
        map.put("Nepilnie", new PositionBonusStats("Nepilnie", "💎", "NEPILNIE", shifts));

        for (WorkRecord record : records) {
            if (record.getShiftName() == null) {
                continue;
            }

            if (record.getRoboti() > 0) {
                map.get("Robots").add(record.getShiftName(), record.getBonus());
            }

            if (record.getCirsana() > 0 || record.getCirsanaPilnie() > 0 || record.getCirsanaNepilnie() > 0) {
                map.get("Ciršana").add(record.getShiftName(), record.getBonus());
            }

            if (record.getPilnie() > 0) {
                map.get("Pilnie").add(record.getShiftName(), record.getBonus());
            }

            if (record.getNepilnie() > 0
                    || record.getPirmaPakape() > 0
                    || record.getOtraPakape() > 0
                    || record.getTreshaPakape() > 0
                    || record.getCeturtaPakape() > 0) {
                map.get("Nepilnie").add(record.getShiftName(), record.getBonus());
            }
        }

        for (PositionBonusStats stats : map.values()) {
            stats.calculate();
        }

        return map;
    }

    @PostMapping("/add-record")
    public String addRecord(@RequestParam Long employeeId,
                            @RequestParam String shiftName,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) List<String> workType,
                            @RequestParam(required = false) List<String> amount) {

        Employee employee = employeeRepository.findById(employeeId).orElseThrow();

        LocalDate recordDate = parseDate(date);

        if (workRecordRepository.existsByEmployee_IdAndDateAndShiftName(employeeId, recordDate, shiftName)) {
            return "redirect:/?duplicate=true";
        }

        WorkRecord record = new WorkRecord();

        record.setEmployee(employee);
        record.setTabelesNr(employee.getTabelesNr());
        record.setShiftName(shiftName);
        record.setDate(recordDate);

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
    public String recalculateBonuses(HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

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

    private int countUniqueShifts(List<WorkRecord> records) {
        Set<String> uniqueShifts = new HashSet<>();

        for (WorkRecord record : records) {
            if (record.getDate() != null && record.getShiftName() != null) {
                uniqueShifts.add(record.getDate() + "|" + record.getShiftName());
            }
        }

        return uniqueShifts.size();
    }

    private boolean isAdmin(HttpSession session) {
        Object loggedUser = session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return false;
        }

        String username = readStringValue(loggedUser, "getUsername");
        String role = readStringValue(loggedUser, "getRole");

        return "ADMIN".equalsIgnoreCase(role)
                || ("JanisR".equalsIgnoreCase(username) && "ADMIN".equalsIgnoreCase(role));
    }

    private String readStringValue(Object object, String methodName) {
        try {
            Method method = object.getClass().getMethod(methodName);
            Object value = method.invoke(object);

            if (value == null) {
                return "";
            }

            return value.toString();
        } catch (Exception e) {
            return "";
        }
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

        private Set<String> uniqueShifts = new HashSet<>();

        private List<ShiftLeader> topLeaders = new ArrayList<>();

        public ShiftStats(String shiftName) {
            this.shiftName = shiftName;
        }

        public void addRecord(WorkRecord record) {
            if (record.getDate() != null && record.getShiftName() != null) {
                uniqueShifts.add(record.getDate() + "|" + record.getShiftName());
            }

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
            shiftCount = uniqueShifts.size();

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

    public static class PositionBonusStats {

        private String name;
        private String icon;
        private String code;

        private double totalBonus;
        private int count;
        private double averageBonus;

        private Map<String, Double> shiftTotals = new LinkedHashMap<>();
        private Map<String, Integer> shiftCounts = new LinkedHashMap<>();
        private Map<String, Double> shiftAverages = new LinkedHashMap<>();

        public PositionBonusStats(String name, String icon, String code, List<String> shifts) {
            this.name = name;
            this.icon = icon;
            this.code = code;

            for (String shift : shifts) {
                shiftTotals.put(shift, 0.0);
                shiftCounts.put(shift, 0);
                shiftAverages.put(shift, 0.0);
            }
        }

        public void add(String shiftName, double bonus) {
            totalBonus += bonus;
            count++;

            if (shiftTotals.containsKey(shiftName)) {
                shiftTotals.put(shiftName, shiftTotals.get(shiftName) + bonus);
                shiftCounts.put(shiftName, shiftCounts.get(shiftName) + 1);
            }
        }

        public void calculate() {
            averageBonus = count == 0 ? 0 : totalBonus / count;

            for (String shift : shiftTotals.keySet()) {
                int shiftCount = shiftCounts.get(shift);
                double shiftTotal = shiftTotals.get(shift);

                shiftAverages.put(shift, shiftCount == 0 ? 0 : shiftTotal / shiftCount);
            }
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public String getCode() {
            return code;
        }

        public double getTotalBonus() {
            return totalBonus;
        }

        public int getCount() {
            return count;
        }

        public double getAverageBonus() {
            return averageBonus;
        }

        public Map<String, Double> getShiftAverages() {
            return shiftAverages;
        }
    }
}