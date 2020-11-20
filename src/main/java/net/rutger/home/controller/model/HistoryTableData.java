package net.rutger.home.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.rutger.home.domain.StaticWateringData;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.domain.WateringJobEnforceData;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class HistoryTableData {

    final List<HistoryTableColumns> columns = new ArrayList<>();

    final List<List<String>> rows = new ArrayList<>();

    public HistoryTableData(final Collection<WateringJobData> input) {
        for (final WateringJobData jobData : input) {
            final List<String> line = new ArrayList<>();
            line.add(jobData.getLocalDate() == null ? "" : jobData.getLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            line.add(jobData.getMakkinkIndexString());
            line.add(jobData.getPrecipitationString());
            line.add(jobData.getPrecipitationDurationString());
            line.add(jobData.getMeanTemperatureString());
            line.add(jobData.getMaxTemperatureString());
            line.add(String.valueOf(jobData.getNumberOfMinutes()));
            line.add(jobData.getType() == null ? "" : jobData.getType().name());

            final StaticWateringData staticData = jobData.getStaticWateringData();
            final WateringJobEnforceData enforceData = jobData.getEnforceData();
            if (enforceData != null) {
                line.add(enforceData.getMultiplyFactorString());
            } else if (staticData != null) {
                line.add(staticData.getFactorString());
            } else {
                line.add("");
            }

            if (staticData == null) {
                for(int i = 0; i<8; i++) {
                    line.add("");
                }
            } else {
                line.add(String.valueOf(staticData.getInitialMm()));
                line.add(String.valueOf(staticData.getDailyLimitMinutes()));
                line.add(String.valueOf(staticData.getMinutesPerMm()));
                line.add(staticData.getMaxDurationMinutes() + " / " + staticData.getIntervalMinutes() + " min.");
                line.add(staticData.getLastModified().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            rows.add(line);
        }

        columns.add(new HistoryTableColumns("Datum"));
        columns.add(new HistoryTableColumns("Verdamping"));
        columns.add(new HistoryTableColumns("Neerslag mm"));
        columns.add(new HistoryTableColumns("Neerslag hr"));
        columns.add(new HistoryTableColumns("Gem. temp."));
        columns.add(new HistoryTableColumns("Max. temp."));
        columns.add(new HistoryTableColumns("# minuten"));
        columns.add(new HistoryTableColumns("Type"));
        columns.add(new HistoryTableColumns("Factor"));
        columns.add(new HistoryTableColumns("Initieel mm."));
        columns.add(new HistoryTableColumns("Max #min."));
        columns.add(new HistoryTableColumns("min./mm."));
        columns.add(new HistoryTableColumns("Interval"));
        columns.add(new HistoryTableColumns("Datum settings"));
    }

    @Data
    @AllArgsConstructor
    class HistoryTableColumns{
        public String title;
    }
}
