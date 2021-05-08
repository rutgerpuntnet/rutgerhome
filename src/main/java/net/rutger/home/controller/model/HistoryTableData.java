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
//TODO RJ add upper valve data
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
            line.add(String.valueOf(jobData.getNumberOfMinutesUpper()));
            line.add(String.valueOf(jobData.getNumberOfMinutesLower()));
            line.add(jobData.getType() == null ? "" : jobData.getType().name());

            final StaticWateringData lowerStaticData = jobData.getLowerStaticWateringData();
            final WateringJobEnforceData enforceData = jobData.getEnforceData();
            if (enforceData != null) {
                line.add(enforceData.getMultiplyFactorString());
            } else if (lowerStaticData != null) {
                line.add(lowerStaticData.getFactorString());
            } else {
                line.add("");
            }

            if (lowerStaticData == null) {
                for(int i = 0; i<8; i++) {
                    line.add("");
                }
            } else {
                line.add(String.valueOf(lowerStaticData.getInitialMm()));
                line.add(String.valueOf(lowerStaticData.getDailyLimitMinutes()));
                line.add(String.valueOf(lowerStaticData.getMinutesPerMm()));
                line.add(lowerStaticData.getMaxDurationMinutes() + " / " + lowerStaticData.getIntervalMinutes() + " min.");
                line.add(lowerStaticData.getLastModified().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            rows.add(line);
        }

        columns.add(new HistoryTableColumns("Datum"));
        columns.add(new HistoryTableColumns("Verdamping"));
        columns.add(new HistoryTableColumns("Neerslag mm"));
        columns.add(new HistoryTableColumns("Neerslag hr"));
        columns.add(new HistoryTableColumns("Gem. temp."));
        columns.add(new HistoryTableColumns("Max. temp."));
        columns.add(new HistoryTableColumns("# minuten boven"));
        columns.add(new HistoryTableColumns("# minuten onder"));
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
