package net.rutger.home.service;

import net.rutger.home.domain.WeatherDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KnmiService {
    private final static Logger LOG = LoggerFactory.getLogger(KnmiService.class);

    @Value("${knmi.service.url}")
    private String url;

    public Optional<Map<WeatherDataType, Double>> getDataFromPreviousDay() {
        try {
            // Get data
            final List<String> rawData = readData();
            // Parse data
            return Optional.of(parseWeatherData(rawData));
        } catch (IOException | RuntimeException e) {
            LOG.info("Exception during KNMI data retrieval:" + e.getMessage());
            return Optional.empty();
        }
    }

    /*
     * Parse the given raw weather data from KNMI
     */
    private Map<WeatherDataType, Double> parseWeatherData(final List<String> rawData) {
        final Map<WeatherDataType, Double> result = new HashMap<>();

        // remove whitespaces and split using comma
        final String[] columnNames = rawData.get(2).replaceAll("\\s+","").split(",");
        final String[] columnValues = rawData.get(0).replaceAll("\\s+","").split(",");

        if (columnNames.length != columnValues.length) {
            LOG.error("wrong data retrieved. Number of columns not equal to number of values");
        } else {
            for (int i = 0; i < columnNames.length; i++) {
                final String column = columnNames[i];
                final String value = columnValues[i];
                try {
                    final WeatherDataType type = WeatherDataType.valueOf(column);
                    result.put(type, type.getFactor() * Integer.valueOf(value));
                } catch (IllegalArgumentException e) {
                    LOG.debug("Skipping unknown weather data type {}", column);
                }
            }
        }

        if (result.get(WeatherDataType.EV24) == null) {
            throw new RuntimeException("No valid MAKKINK index found: " + rawData.get(0));
        }
        LOG.debug("KNMI data retrieved:\n{}", result.toString());
        return result;
    }

    private List<String> readData() throws IOException {
        final String formattedDayYesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        final String interpolatedUrl = this.url.replaceAll("DATESTRING",formattedDayYesterday);
        LOG.debug("Reading data from URL: {}", interpolatedUrl);

        final InputStreamReader inputStreamReader = new InputStreamReader(new URL(interpolatedUrl).openStream());
        final List<String> resultList = new ArrayList<>();
        try (final BufferedReader in = new BufferedReader(inputStreamReader)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resultList.add(0, inputLine);
                LOG.trace("Reading dataline: {}", inputLine);
            }
        }
        LOG.debug("Done reading KNMI data. Resulting in {} lines", resultList.size());

        // The last line (so also last inserted line at position 0 in the List), should contain the actual data
        if (resultList.size() < 3 || resultList.get(0).startsWith("#") || !resultList.get(0).contains("240")) {
            LOG.debug("No valid weatherdata found:\n--{}", resultList.get(0));
            throw new RuntimeException("No valid weatherdata found"); // todo create specific exception
        } else {
            return resultList.subList(0, 3);
        }
    }

}
