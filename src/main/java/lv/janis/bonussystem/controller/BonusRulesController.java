package lv.janis.bonussystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lv.janis.bonussystem.AssortmentRepository;
import lv.janis.bonussystem.BonusRuleHistoryRepository;
import lv.janis.bonussystem.BonusRuleRepository;
import lv.janis.bonussystem.GradeRuleRepository;
import lv.janis.bonussystem.model.AppUser;
import lv.janis.bonussystem.model.Assortment;
import lv.janis.bonussystem.model.BonusRule;
import lv.janis.bonussystem.model.BonusRuleHistory;
import lv.janis.bonussystem.model.GradeRule;
import lv.janis.bonussystem.service.BonusCalculatorService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BonusRulesController {

    private final BonusRuleRepository bonusRuleRepository;
    private final GradeRuleRepository gradeRuleRepository;
    private final AssortmentRepository assortmentRepository;
    private final BonusRuleHistoryRepository historyRepository;
    private final BonusCalculatorService bonusCalculatorService;

    public BonusRulesController(BonusRuleRepository bonusRuleRepository,
                                GradeRuleRepository gradeRuleRepository,
                                AssortmentRepository assortmentRepository,
                                BonusRuleHistoryRepository historyRepository,
                                BonusCalculatorService bonusCalculatorService) {
        this.bonusRuleRepository = bonusRuleRepository;
        this.gradeRuleRepository = gradeRuleRepository;
        this.assortmentRepository = assortmentRepository;
        this.historyRepository = historyRepository;
        this.bonusCalculatorService = bonusCalculatorService;
    }

    @GetMapping("/bonus-rules")
    public String bonusRules(Model model,
                             Authentication authentication,
                             HttpServletRequest request) {
        fillModel(model, null, null, authentication, request);
        return "bonus-rules";
    }

    @GetMapping("/bonus-rules/edit/{id}")
    public String editBonusRule(@PathVariable Long id,
                                Model model,
                                Authentication authentication,
                                HttpServletRequest request) {
        fillModel(model, bonusRuleRepository.findById(id).orElseThrow(), null, authentication, request);
        return "bonus-rules";
    }

    @PostMapping("/bonus-rules/save")
    public String saveBonusRule(@RequestParam(required = false) Long id,
                                @RequestParam String name,
                                @RequestParam(defaultValue = "0") double normValue,
                                @RequestParam(defaultValue = "0") double secondaryNormValue,
                                @RequestParam(defaultValue = "0") double stepValue,
                                @RequestParam(defaultValue = "0") double priceValue,
                                @RequestParam(defaultValue = "0") double overNormValue,
                                @RequestParam(defaultValue = "0") double overNormPrice,
                                Authentication authentication,
                                HttpServletRequest request) {

        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        String changedBy = getUsername(authentication, request);

        BonusRule rule = id != null
                ? bonusRuleRepository.findById(id).orElseThrow()
                : new BonusRule();

        if (id != null) {
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "Nosaukums", rule.getName(), name);
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "Norma", rule.getNormValue(), normValue);
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "Kombinētā norma", rule.getSecondaryNormValue(), secondaryNormValue);
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "Solis", rule.getStepValue(), stepValue);
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "EUR", rule.getPriceValue(), priceValue);
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "Virsnorma", rule.getOverNormValue(), overNormValue);
            saveHistoryIfChanged(changedBy, "Norma", rule.getName(), "EUR virs normas", rule.getOverNormPrice(), overNormPrice);
        } else {
            historyRepository.save(new BonusRuleHistory(changedBy, "Norma", name, "Izveidots", "-", "Jauna norma"));
        }

        rule.setName(name);
        rule.setNormValue(normValue);
        rule.setSecondaryNormValue(secondaryNormValue);
        rule.setStepValue(stepValue);
        rule.setPriceValue(priceValue);
        rule.setOverNormValue(overNormValue);
        rule.setOverNormPrice(overNormPrice);

        bonusRuleRepository.save(rule);

        return "redirect:/bonus-rules";
    }

    @GetMapping("/bonus-rules/delete/{id}")
    public String deleteBonusRule(@PathVariable Long id,
                                  Authentication authentication,
                                  HttpServletRequest request) {
        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        BonusRule rule = bonusRuleRepository.findById(id).orElseThrow();
        historyRepository.save(new BonusRuleHistory(
                getUsername(authentication, request),
                "Norma",
                rule.getName(),
                "Dzēsts",
                rule.getName(),
                "-"
        ));

        bonusRuleRepository.deleteById(id);
        return "redirect:/bonus-rules";
    }

    @GetMapping("/grade-rules/edit/{id}")
    public String editGradeRule(@PathVariable Long id,
                                Model model,
                                Authentication authentication,
                                HttpServletRequest request) {
        fillModel(model, null, gradeRuleRepository.findById(id).orElseThrow(), authentication, request);
        return "bonus-rules";
    }

    @PostMapping("/grade-rules/save")
    public String saveGradeRule(@RequestParam(required = false) Long id,
                                @RequestParam String gradeName,
                                @RequestParam(defaultValue = "1") double coefficient,
                                @RequestParam(defaultValue = "0") double pricePerUnit,
                                Authentication authentication,
                                HttpServletRequest request) {

        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        String changedBy = getUsername(authentication, request);

        GradeRule gradeRule = id != null
                ? gradeRuleRepository.findById(id).orElseThrow()
                : new GradeRule();

        if (id != null) {
            saveHistoryIfChanged(changedBy, "Pakāpe", gradeRule.getGradeName(), "Pakāpe", gradeRule.getGradeName(), gradeName);
            saveHistoryIfChanged(changedBy, "Pakāpe", gradeRule.getGradeName(), "Koeficients", gradeRule.getCoefficient(), coefficient);
            saveHistoryIfChanged(changedBy, "Pakāpe", gradeRule.getGradeName(), "EUR par pakāpi", gradeRule.getPricePerUnit(), pricePerUnit);
        } else {
            historyRepository.save(new BonusRuleHistory(changedBy, "Pakāpe", gradeName, "Izveidots", "-", "Jauna pakāpe"));
        }

        gradeRule.setGradeName(gradeName);
        gradeRule.setCoefficient(coefficient);
        gradeRule.setPricePerUnit(pricePerUnit);

        gradeRuleRepository.save(gradeRule);

        return "redirect:/bonus-rules";
    }

    @GetMapping("/grade-rules/delete/{id}")
    public String deleteGradeRule(@PathVariable Long id,
                                  Authentication authentication,
                                  HttpServletRequest request) {
        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        GradeRule gradeRule = gradeRuleRepository.findById(id).orElseThrow();
        historyRepository.save(new BonusRuleHistory(
                getUsername(authentication, request),
                "Pakāpe",
                gradeRule.getGradeName(),
                "Dzēsts",
                gradeRule.getGradeName(),
                "-"
        ));

        gradeRuleRepository.deleteById(id);
        return "redirect:/bonus-rules";
    }

    @PostMapping("/assortments/save")
    public String saveAssortment(@RequestParam String name,
                                 @RequestParam String type,
                                 @RequestParam(required = false) String gradeName,
                                 Authentication authentication,
                                 HttpServletRequest request) {

        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        Assortment assortment = new Assortment();
        assortment.setName(name);
        assortment.setType(type);
        assortment.setGradeName(gradeName);
        assortment.setActive(true);

        assortmentRepository.save(assortment);

        historyRepository.save(new BonusRuleHistory(
                getUsername(authentication, request),
                "Sortiments",
                name,
                "Izveidots",
                "-",
                type
        ));

        return "redirect:/bonus-rules";
    }

    @GetMapping("/assortments/delete/{id}")
    public String deleteAssortment(@PathVariable Long id,
                                   Authentication authentication,
                                   HttpServletRequest request) {
        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        Assortment assortment = assortmentRepository.findById(id).orElseThrow();

        historyRepository.save(new BonusRuleHistory(
                getUsername(authentication, request),
                "Sortiments",
                assortment.getName(),
                "Dzēsts",
                assortment.getName(),
                "-"
        ));

        assortmentRepository.deleteById(id);
        return "redirect:/bonus-rules";
    }

    @PostMapping("/bonus-rules/recalculate-month")
    public String recalculateMonth(@RequestParam int year,
                                   @RequestParam int month,
                                   Authentication authentication,
                                   HttpServletRequest request) {

        if (!isAdmin(authentication, request)) {
            return "redirect:/bonus-rules";
        }

        bonusCalculatorService.recalculateMonth(year, month);

        historyRepository.save(new BonusRuleHistory(
                getUsername(authentication, request),
                "Pārrēķins",
                year + "-" + month,
                "Mēneša pārrēķins",
                "-",
                "Pārrēķināts"
        ));

        return "redirect:/bonus-rules";
    }

    private void fillModel(Model model,
                           BonusRule editRule,
                           GradeRule editGrade,
                           Authentication authentication,
                           HttpServletRequest request) {
        model.addAttribute("bonusRules", bonusRuleRepository.findAll());
        model.addAttribute("gradeRules", gradeRuleRepository.findAll());
        model.addAttribute("assortments", assortmentRepository.findAll());
        model.addAttribute("history", historyRepository.findAllByOrderByChangedAtDesc());
        model.addAttribute("editRule", editRule);
        model.addAttribute("editGrade", editGrade);
        model.addAttribute("isAdmin", isAdmin(authentication, request));
    }

    private boolean isAdmin(Authentication authentication, HttpServletRequest request) {
        if (authentication != null) {
            boolean springAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ADMIN"));

            if (springAdmin || "admin".equalsIgnoreCase(authentication.getName())) {
                return true;
            }
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object userObj = session.getAttribute("user");

            if (userObj instanceof AppUser user) {
                return "ADMIN".equalsIgnoreCase(user.getRole())
                        || "admin".equalsIgnoreCase(user.getUsername());
            }

            Object loggedUserObj = session.getAttribute("loggedUser");

            if (loggedUserObj instanceof AppUser user) {
                return "ADMIN".equalsIgnoreCase(user.getRole())
                        || "admin".equalsIgnoreCase(user.getUsername());
            }
        }

        return false;
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

    private void saveHistoryIfChanged(String changedBy, String objectType, String objectName,
                                      String fieldName, Object oldValue, Object newValue) {
        String oldVal = String.valueOf(oldValue);
        String newVal = String.valueOf(newValue);

        if (!oldVal.equals(newVal)) {
            historyRepository.save(new BonusRuleHistory(
                    changedBy,
                    objectType,
                    objectName,
                    fieldName,
                    oldVal,
                    newVal
            ));
        }
    }
}