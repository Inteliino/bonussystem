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
                    .timeout(15000)
                    .get();

            Elements rows = document.select("tr");

            for (Element row : rows) {

                Elements cols = row.select("td");

                if (cols.size() < 2) {
                    continue;
                }

                String employeeName = cols.get(0).text().trim();
                String tabelesNr = cols.get(1).text().trim();

                if (employeeName.isBlank() || tabelesNr.isBlank()) {
                    continue;
                }

                PendingWorkRecord record = new PendingWorkRecord();

                record.setDate(LocalDate.now());
                record.setWorkPeriod("Importēts");
                record.setShiftName("Importēts");
                record.setEmployeeName(employeeName);
                record.setTabelesNr(tabelesNr);

                record.setRoboti(0);
                record.setCirsana(0);
                record.setCirsanaPilnie(0);
                record.setCirsanaNepilnie(0);
                record.setPilnie(0);
                record.setNepilnie(0);
                record.setPirmaPakape(0);
                record.setOtraPakape(0);
                record.setTreshaPakape(0);
                record.setCeturtaPakape(0);

                record.setStatus("PENDING");
                record.setImportedAt(LocalDateTime.now());

                pendingRepository.save(record);
                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Neizdevās importēt datus: " + e.getMessage());
        }

        return count;
    }
}