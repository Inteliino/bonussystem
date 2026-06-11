package lv.janis.bonussystem.service;

import lv.janis.bonussystem.PendingWorkRecordRepository;
import lv.janis.bonussystem.model.PendingWorkRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ValmieraGlassImportService {

    private final PendingWorkRecordRepository pendingRepository;

    public ValmieraGlassImportService(PendingWorkRecordRepository pendingRepository) {
        this.pendingRepository = pendingRepository;
    }

    public int importFromValmieraGlass() {

        String url = "https://webapp.valmiera-glass.com/krasns/endfindingRacksWorkers.php";
        int count = 0;

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://webapp.valmiera-glass.com/")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .timeout(30000)
                    .get();

            System.out.println("IMPORT STATUS: lapas virsraksts = " + document.title());
            System.out.println("IMPORT ROW COUNT: " + document.select("tr").size());

            Elements rows = document.select("tr");

            for (Element row : rows) {

                Elements cols = row.select("td");

                if (cols.size() < 2) {
                    continue;
                }

                String employeeName = cleanText(cols.get(0).text());
                String tabelesNr = cleanText(cols.get(1).text());

                if (employeeName.isBlank() || tabelesNr.isBlank()) {
                    continue;
                }

                if (!looksLikeEmployee(employeeName, tabelesNr)) {
                    continue;
                }

                PendingWorkRecord record = new PendingWorkRecord();

                record.setDate(LocalDate.now());
                record.setWorkPeriod("Importēts");
                record.setShiftName("Importēts");

                record.setEmployeeName(employeeName);
                record.setTabelesNr(tabelesNr);

                record.setRoboti(readNumber(cols, 2));
                record.setCirsana(readNumber(cols, 3));
                record.setCirsanaPilnie(0);
                record.setCirsanaNepilnie(0);
                record.setPilnie(readNumber(cols, 4));
                record.setNepilnie(readNumber(cols, 5));
                record.setPirmaPakape(readNumber(cols, 6));
                record.setOtraPakape(readNumber(cols, 7));
                record.setTreshaPakape(readNumber(cols, 8));
                record.setCeturtaPakape(readNumber(cols, 9));

                record.setStatus("PENDING");
                record.setImportedAt(LocalDateTime.now());
                record.setImportSource("VALMIERA_GLASS");

                pendingRepository.save(record);
                count++;
            }

            System.out.println("IMPORT DONE: saglabāti ieraksti = " + count);
            return count;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Neizdevās importēt datus no Valmiera Glass: " + e.getMessage(), e);
        }
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\u00A0", " ")
                .replace(",", ".")
                .trim();
    }

    private double readNumber(Elements cols, int index) {
        if (cols.size() <= index) {
            return 0;
        }

        String text = cleanText(cols.get(index).text());

        if (text.isBlank() || text.equals("-")) {
            return 0;
        }

        text = text.replaceAll("[^0-9.]", "");

        if (text.isBlank()) {
            return 0;
        }

        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean looksLikeEmployee(String employeeName, String tabelesNr) {
        if (employeeName.length() < 3) {
            return false;
        }

        if (employeeName.toLowerCase().contains("darbinieks")) {
            return false;
        }

        return tabelesNr.matches(".*\\d.*");
    }
}