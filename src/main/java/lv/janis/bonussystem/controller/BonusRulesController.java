package lv.janis.bonussystem.controller;

import lv.janis.bonussystem.AssortmentRepository;
import lv.janis.bonussystem.BonusRuleRepository;
import lv.janis.bonussystem.GradeRuleRepository;
import lv.janis.bonussystem.model.Assortment;
import lv.janis.bonussystem.model.BonusRule;
import lv.janis.bonussystem.model.GradeRule;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BonusRulesController {

    private final BonusRuleRepository bonusRuleRepository;
    private final GradeRuleRepository gradeRuleRepository;
    private final AssortmentRepository assortmentRepository;

    public BonusRulesController(BonusRuleRepository bonusRuleRepository,
                                GradeRuleRepository gradeRuleRepository,
                                AssortmentRepository assortmentRepository) {
        this.bonusRuleRepository = bonusRuleRepository;
        this.gradeRuleRepository = gradeRuleRepository;
        this.assortmentRepository = assortmentRepository;
    }

    @GetMapping("/bonus-rules")
    public String bonusRulesPage(Model model) {
        model.addAttribute("bonusRules", bonusRuleRepository.findAll());
        model.addAttribute("gradeRules", gradeRuleRepository.findAll());
        model.addAttribute("assortments", assortmentRepository.findAll());

        model.addAttribute("newBonusRule", new BonusRule());
        model.addAttribute("newGradeRule", new GradeRule());
        model.addAttribute("newAssortment", new Assortment());

        return "bonus-rules";
    }

    @PostMapping("/bonus-rules/save")
    public String saveBonusRule(@ModelAttribute BonusRule bonusRule) {
        bonusRuleRepository.save(bonusRule);
        return "redirect:/bonus-rules";
    }

    @PostMapping("/grade-rules/save")
    public String saveGradeRule(@ModelAttribute GradeRule gradeRule) {
        gradeRuleRepository.save(gradeRule);
        return "redirect:/bonus-rules";
    }

    @PostMapping("/assortments/save")
    public String saveAssortment(@ModelAttribute Assortment assortment) {
        assortment.setActive(true);

        if (!"NEPILNIE".equals(assortment.getType())) {
            assortment.setGradeName("");
        }

        assortmentRepository.save(assortment);
        return "redirect:/bonus-rules";
    }

    @GetMapping("/bonus-rules/delete/{id}")
    public String deleteBonusRule(@PathVariable Long id) {
        bonusRuleRepository.deleteById(id);
        return "redirect:/bonus-rules";
    }

    @GetMapping("/grade-rules/delete/{id}")
    public String deleteGradeRule(@PathVariable Long id) {
        gradeRuleRepository.deleteById(id);
        return "redirect:/bonus-rules";
    }
    @GetMapping("/bonus-rules/edit/{id}")
    public String editBonusRulePage(@PathVariable Long id, Model model) {
        BonusRule rule = bonusRuleRepository.findById(id).orElseThrow();

        model.addAttribute("editRule", rule);
        model.addAttribute("bonusRules", bonusRuleRepository.findAll());
        model.addAttribute("gradeRules", gradeRuleRepository.findAll());
        model.addAttribute("assortments", assortmentRepository.findAll());

        model.addAttribute("newBonusRule", new BonusRule());
        model.addAttribute("newGradeRule", new GradeRule());
        model.addAttribute("newAssortment", new Assortment());

        return "bonus-rules";
    }

    @GetMapping("/assortments/delete/{id}")
    public String deleteAssortment(@PathVariable Long id) {
        assortmentRepository.deleteById(id);
        return "redirect:/bonus-rules";
    }
}