package lv.janis.bonussystem.service;

import lv.janis.bonussystem.BonusRuleRepository;
import lv.janis.bonussystem.GradeRuleRepository;
import lv.janis.bonussystem.WorkRecordRepository;
import lv.janis.bonussystem.model.BonusRule;
import lv.janis.bonussystem.model.GradeRule;
import lv.janis.bonussystem.model.WorkRecord;
import org.springframework.stereotype.Service;

@Service
public class BonusCalculatorService {

    private final GradeRuleRepository gradeRuleRepository;
    private final BonusRuleRepository bonusRuleRepository;
    private final WorkRecordRepository workRecordRepository;

    public BonusCalculatorService(GradeRuleRepository gradeRuleRepository,
                                  BonusRuleRepository bonusRuleRepository,
                                  WorkRecordRepository workRecordRepository) {
        this.gradeRuleRepository = gradeRuleRepository;
        this.bonusRuleRepository = bonusRuleRepository;
        this.workRecordRepository = workRecordRepository;
    }

    public double calculateBonus(WorkRecord record) {

        BonusRule robotsRule = getRule("Robots");
        BonusRule pilnieRule = getRule("Pilnie");
        BonusRule nepilnieRule = getRule("Nepilnie");
        BonusRule pilnieTikaiRule = getRule("Pilnie tikai");
        BonusRule certamieRule = getRule("Cērtamie");
        BonusRule certamieNepilnieRule = getRule("Cērtamie nepilnie");

        double pirmaCoef = getCoefficient("I", 1.2);
        double otraCoef = getCoefficient("II", 1.1);
        double treshaCoef = getCoefficient("III", 0.88);
        double ceturtaCoef = getCoefficient("IV", 0.8);

        double coefNepilnie =
                record.getNepilnie()
                        + record.getCirsanaNepilnie()
                        + record.getPirmaPakape() * pirmaCoef
                        + record.getOtraPakape() * otraCoef
                        + record.getTreshaPakape() * treshaCoef
                        + record.getCeturtaPakape() * ceturtaCoef;

        double bonus = 0.0;
        double realRati = 0.0;

        if (record.getRoboti() > 0) {
            bonus += calculateRobots(record.getRoboti(), robotsRule);
            realRati += record.getRoboti();
        }

        if (record.getCirsana() > 0 || record.getCirsanaPilnie() > 0) {
            bonus += record.getCirsana() * certamieRule.getPriceValue();
            bonus += record.getCirsanaPilnie() * certamieRule.getPriceValue();
            bonus += coefNepilnie * certamieNepilnieRule.getPriceValue();

            realRati += record.getCirsana();
            realRati += record.getCirsanaPilnie();
            realRati += coefNepilnie;
        }

        if (record.getPilnie() > 0) {
            bonus += calculatePilnie(
                    record.getPilnie(),
                    coefNepilnie,
                    pilnieRule,
                    pilnieTikaiRule
            );

            realRati += record.getPilnie();
            realRati += coefNepilnie;
        }

        if (record.getPilnie() <= 0
                && record.getRoboti() <= 0
                && record.getCirsana() <= 0
                && record.getCirsanaPilnie() <= 0) {

            bonus += calculateNepilnie(coefNepilnie, nepilnieRule);
            realRati += coefNepilnie;
        }

        bonus = round2(bonus);
        realRati = round2(realRati);

        record.setBonus(bonus);
        record.setRealRati(realRati);

        return bonus;
    }

    public void recalculateMonth(int year, int month) {
        for (WorkRecord record : workRecordRepository.findAll()) {
            if (record.getDate() == null) {
                continue;
            }

            if (record.getDate().getYear() == year
                    && record.getDate().getMonthValue() == month) {
                calculateBonus(record);
                workRecordRepository.save(record);
            }
        }
    }

    private double calculateRobots(double roboti, BonusRule rule) {
        double norm = rule.getNormValue();
        double step = rule.getStepValue();
        double price = rule.getPriceValue();
        double overStep = rule.getOverNormValue();
        double overPrice = rule.getOverNormPrice();

        if (roboti <= norm) {
            return (roboti / step) * price;
        }

        double bonus = (norm / step) * price;
        bonus += ((roboti - norm) / overStep) * overPrice;

        return bonus;
    }

    private double calculatePilnie(double pilnie,
                                   double nepilnieCoef,
                                   BonusRule pilnieRule,
                                   BonusRule pilnieTikaiRule) {

        double bestBonus = 0.0;

        double pilnieNorm = pilnieRule.getNormValue();
        double nepilnieNorm = pilnieRule.getSecondaryNormValue();
        double normaBonus = pilnieRule.getPriceValue();
        double pilnieOverPrice = pilnieRule.getOverNormPrice();

        double totalEquivalent = pilnie + (nepilnieCoef * 2.0);
        double normEquivalent = pilnieNorm + (nepilnieNorm * 2.0);

        if (totalEquivalent >= normEquivalent) {
            double bonus = normaBonus;
            bonus += (totalEquivalent - normEquivalent) * pilnieOverPrice;
            bestBonus = Math.max(bestBonus, bonus);
        }

        double pilnieTikaiNorm = pilnieTikaiRule.getNormValue();

        if (pilnie >= pilnieTikaiNorm) {
            double bonus = pilnieTikaiRule.getPriceValue();
            bonus += (pilnie - pilnieTikaiNorm) * pilnieTikaiRule.getOverNormPrice();
            bonus += nepilnieCoef * pilnieRule.getOverNormPrice() * 2.0;

            bestBonus = Math.max(bestBonus, bonus);
        }

        return bestBonus;
    }

    private double calculateNepilnie(double nepilnieCoef, BonusRule rule) {
        double norm = rule.getNormValue();

        if (nepilnieCoef < norm) {
            return 0.0;
        }

        double bonus = rule.getPriceValue();
        bonus += (nepilnieCoef - norm) * rule.getOverNormPrice();

        return bonus;
    }

    private BonusRule getRule(String name) {
        BonusRule rule = bonusRuleRepository.findByNameIgnoreCase(name);

        if (rule == null) {
            throw new RuntimeException("Nav atrasts bonusa nolikums: " + name);
        }

        return rule;
    }

    private double getCoefficient(String gradeName, double defaultValue) {
        return gradeRuleRepository.findAll()
                .stream()
                .filter(rule -> rule.getGradeName() != null)
                .filter(rule -> rule.getGradeName().equalsIgnoreCase(gradeName))
                .findFirst()
                .map(GradeRule::getCoefficient)
                .orElse(defaultValue);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}