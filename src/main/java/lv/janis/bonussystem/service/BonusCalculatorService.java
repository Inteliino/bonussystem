package lv.janis.bonussystem.service;

import lv.janis.bonussystem.model.WorkRecord;
import org.springframework.stereotype.Service;

@Service
public class BonusCalculatorService {

    private static final double NORMA = 18.0;
    private static final double NORMA_BONUSS = 10.0;
    private static final double VIRS_NORMAS_CENA = 2.0;

    public double calculateBonus(WorkRecord record) {

        double totalRati = 0.0;

        // Pilnie
        totalRati += record.getPilnie();

        // Nepilnie - default koeficients 1
        totalRati += record.getCirsanaNepilnie();

        // Pakāpes pēc koeficientiem
        totalRati += record.getPirmaPakape() * 1.0;
        totalRati += record.getOtraPakape() * 0.95;
        totalRati += record.getTreshaPakape() * 0.9;
        totalRati += record.getCeturtaPakape() * 0.8;

        // Roboti un ciršana, ja tos arī skaiti pie ratiem
        totalRati += record.getRoboti();
        totalRati += record.getCirsana();
        totalRati += record.getCirsanaPilnie();

        record.setRealRati(totalRati);

        if (totalRati < NORMA) {
            record.setBonus(0.0);
            return 0.0;
        }

        double bonus = NORMA_BONUSS;

        if (totalRati > NORMA) {
            double virsNormas = totalRati - NORMA;
            bonus += virsNormas * VIRS_NORMAS_CENA;
        }

        record.setBonus(bonus);
        return bonus;
    }
}