package lv.janis.bonussystem.service;

import lv.janis.bonussystem.BonusRuleRepository;
import lv.janis.bonussystem.GradeRuleRepository;
import lv.janis.bonussystem.model.BonusRule;
import lv.janis.bonussystem.model.GradeRule;
import lv.janis.bonussystem.model.WorkRecord;

import org.springframework.stereotype.Service;

@Service
public class BonusCalculatorService {

    private final BonusRuleRepository bonusRuleRepository;
    private final GradeRuleRepository gradeRuleRepository;

    public BonusCalculatorService(BonusRuleRepository bonusRuleRepository,
                                  GradeRuleRepository gradeRuleRepository) {
        this.bonusRuleRepository = bonusRuleRepository;
        this.gradeRuleRepository = gradeRuleRepository;
    }

    public void calculate(WorkRecord record) {
        double bonus = 0.0;

        bonus += calculateRobots(record.getRoboti());
        bonus += calculateCirsana(record);
        bonus += calculatePilnieAndNepilnie(record);

        double realRati = record.getRoboti()
                + record.getCirsana()
                + record.getPilnie()
                + effectiveNepilnie(
                    record.getPirmaPakape(),
                    record.getOtraPakape(),
                    record.getTreshaPakape(),
                    record.getCeturtaPakape()
                );

        record.setRealRati(round(realRati));
        record.setBonus(round(bonus));
    }

    private double calculateRobots(double amount) {
        BonusRule rule = bonusRuleRepository.findByNameIgnoreCase("Robots");

        if (rule == null || amount <= 0) {
            return 0.0;
        }

        double norm = rule.getNormValue();
        double step = rule.getStepValue();
        double price = rule.getPriceValue();
        double overStep = rule.getOverNormValue();
        double overPrice = rule.getOverNormPrice();

        if (step <= 0) {
            return 0.0;
        }

        if (amount <= norm) {
            return (amount / step) * price;
        }

        double bonus = (norm / step) * price;

        if (overStep > 0) {
            bonus += ((amount - norm) / overStep) * overPrice;
        }

        return bonus;
    }

    private double calculateCirsana(WorkRecord record) {
        double bonus = 0.0;

        BonusRule cirsanaRule = bonusRuleRepository.findByNameIgnoreCase("Cērtamie");

        if (cirsanaRule != null && record.getCirsana() > 0) {
            double step = cirsanaRule.getStepValue();

            if (step <= 0) {
                step = 1;
            }

            bonus += (record.getCirsana() / step) * cirsanaRule.getPriceValue();
        }

        BonusRule cirsanaNepilnieRule = bonusRuleRepository.findByNameIgnoreCase("Cērtamie nepilnie");

        if (cirsanaNepilnieRule != null && record.getCirsana() > 0) {
            double nepilnieCount = record.getPirmaPakape()
                    + record.getOtraPakape()
                    + record.getTreshaPakape()
                    + record.getCeturtaPakape();

            double step = cirsanaNepilnieRule.getStepValue();

            if (step <= 0) {
                step = 1;
            }

            bonus += (nepilnieCount / step) * cirsanaNepilnieRule.getPriceValue();
        }

        return bonus;
    }

    private double calculatePilnieAndNepilnie(WorkRecord record) {
        BonusRule pilnieRule = bonusRuleRepository.findByNameIgnoreCase("Pilnie");
        BonusRule nepilnieRule = bonusRuleRepository.findByNameIgnoreCase("Nepilnie");

        if (pilnieRule == null || nepilnieRule == null) {
            return 0.0;
        }

        double pilnie = record.getPilnie();

        double p1 = record.getPirmaPakape();
        double p2 = record.getOtraPakape();
        double p3 = record.getTreshaPakape();
        double p4 = record.getCeturtaPakape();

        double effectiveNepilnie = effectiveNepilnie(p1, p2, p3, p4);

        double pilnieNorm = pilnieRule.getNormValue();
        double comboNepilnieNorm = pilnieRule.getSecondaryNormValue();

        double nepilnieOnlyNorm = nepilnieRule.getNormValue();

        double baseComboBonus = pilnieRule.getPriceValue();
        double baseNepilnieBonus = nepilnieRule.getPriceValue();

        double pilnieOverPrice = pilnieRule.getOverNormPrice();
        double nepilnieOverPrice = nepilnieRule.getOverNormPrice();

        double bonus = 0.0;

        boolean hasPilnie = pilnie > 0;
        boolean hasNepilnie = effectiveNepilnie > 0;

        if (hasPilnie && hasNepilnie) {
            boolean comboReached = pilnie >= pilnieNorm && effectiveNepilnie >= comboNepilnieNorm;

            if (!comboReached) {
                return 0.0;
            }

            bonus += baseComboBonus;

            if (pilnie > pilnieNorm) {
                bonus += (pilnie - pilnieNorm) * pilnieOverPrice;
            }

            double nepilnieOverRaw = calculateNepilnieRawOverNorm(
                    p1, p2, p3, p4,
                    comboNepilnieNorm
            );

            bonus += nepilnieOverRaw * nepilnieOverPrice;

            return bonus;
        }

        if (!hasPilnie && hasNepilnie) {
            boolean nepilnieReached = effectiveNepilnie >= nepilnieOnlyNorm;

            if (!nepilnieReached) {
                return 0.0;
            }

            bonus += baseNepilnieBonus;

            double nepilnieOverRaw = calculateNepilnieRawOverNorm(
                    p1, p2, p3, p4,
                    nepilnieOnlyNorm
            );

            bonus += nepilnieOverRaw * nepilnieOverPrice;

            return bonus;
        }

        if (hasPilnie && !hasNepilnie) {
            boolean pilnieReached = pilnie >= pilnieNorm;

            if (!pilnieReached) {
                return 0.0;
            }

            bonus += baseComboBonus;

            if (pilnie > pilnieNorm) {
                bonus += (pilnie - pilnieNorm) * pilnieOverPrice;
            }

            return bonus;
        }

        return 0.0;
    }

    private double calculateNepilnieRawOverNorm(double p1,
                                                double p2,
                                                double p3,
                                                double p4,
                                                double norm) {

        double k1 = getCoefficient("I");
        double k2 = getCoefficient("II");
        double k3 = getCoefficient("III");
        double k4 = getCoefficient("IV");

        double currentNorm = 0.0;

        double remainingP1 = p1;
        double remainingP2 = p2;
        double remainingP3 = p3;
        double remainingP4 = p4;

        if (currentNorm < norm && remainingP1 > 0) {
            double need = norm - currentNorm;
            double use = Math.min(remainingP1, Math.ceil(need / k1));
            currentNorm += use * k1;
            remainingP1 -= use;
        }

        if (currentNorm < norm && remainingP2 > 0) {
            double need = norm - currentNorm;
            double use = Math.min(remainingP2, Math.ceil(need / k2));
            currentNorm += use * k2;
            remainingP2 -= use;
        }

        if (currentNorm < norm && remainingP3 > 0) {
            double need = norm - currentNorm;
            double use = Math.min(remainingP3, Math.ceil(need / k3));
            currentNorm += use * k3;
            remainingP3 -= use;
        }

        if (currentNorm < norm && remainingP4 > 0) {
            double need = norm - currentNorm;
            double use = Math.min(remainingP4, Math.ceil(need / k4));
            currentNorm += use * k4;
            remainingP4 -= use;
        }

        return remainingP1 + remainingP2 + remainingP3 + remainingP4;
    }

    private double effectiveNepilnie(double p1, double p2, double p3, double p4) {
        return p1 * getCoefficient("I")
                + p2 * getCoefficient("II")
                + p3 * getCoefficient("III")
                + p4 * getCoefficient("IV");
    }

    private double getCoefficient(String gradeName) {
        GradeRule gradeRule = gradeRuleRepository.findByGradeName(gradeName);

        if (gradeRule == null) {
            return 1.0;
        }

        return gradeRule.getCoefficient();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}