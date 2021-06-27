package net.rutger.home.service;

import net.rutger.home.domain.WeatherDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class KnmiService {
    private final static Logger LOG = LoggerFactory.getLogger(KnmiService.class);

    private static final String ZIP_DATA_LINE_START = "  240,";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Optional<Map<WeatherDataType, Double>> getDataFromPreviousDay() {
        try {
            final List<String> rawZipData = readDataFromZip();
            final Map<WeatherDataType, Double> parsedWeatherData = parseWeatherData(rawZipData);
            return Optional.ofNullable(parsedWeatherData);
        } catch (IllegalStateException e) {
            LOG.info("IllegalStateException during KNMI data retrieval:" + e.getMessage());
        } catch (IOException | RuntimeException e) {
            LOG.info("Exception during KNMI data retrieval", e);
        }
        return Optional.empty();
    }

    /*
     * Parse the given raw weather data from KNMI
     */
    private Map<WeatherDataType, Double> parseWeatherData(final List<String> rawZipData) {
        final Map<WeatherDataType, Double> result = new HashMap<>();

        if (rawZipData.size() != 2) {
            return null; // No data found, return null
        }

        // remove whitespaces and split using comma
        final List<String> columnNames = Arrays.asList(getLineAsArray(rawZipData.get(0)));
        final List<String> columnValues = Arrays.asList(getLineAsArray(rawZipData.get(1)));
        if (columnNames.size() != columnValues.size()) {
            throw new IllegalStateException("wrong data retrieved. Number of columns not equal to number of values");
        }

        for (WeatherDataType weatherDataType : WeatherDataType.values()) {
            // Get the index for each type of WeatherData that we need
            final int index = columnNames.indexOf(weatherDataType.name());
            if (index > -1) {
                Integer intValue = Integer.valueOf(columnValues.get(index));
                if ((WeatherDataType.RH.equals(weatherDataType) || WeatherDataType.SQ.equals(weatherDataType))
                        && intValue < 0) {
                    // For RH and SQ they value is -1 of they measured 0.5, in that case we'll force it to 0
                    intValue = 0;
                }
                result.put(weatherDataType, weatherDataType.getFactor() * intValue);
            }
        }

        // validate if we have the Makkink index
        if (result.get(WeatherDataType.EV24) == null) {
            throw new IllegalStateException("No valid MAKKINK index found");
        }
        LOG.debug("KNMI data retrieved:\n{}", result);
        return result;
    }

    private String[] getLineAsArray(final String input) {
        return input.replaceAll("\\s+","").split(",");

    }
    private List<String> readDataFromZip() throws IOException {
        final String url = "https://cdn.knmi.nl/knmi/map/page/klimatologie/gegevens/daggegevens/etmgeg_240.zip";
        LOG.debug("Reading zip data from URL: {}", url);

        final BufferedInputStream bufferedInput = new BufferedInputStream(new URL(url).openStream());
        final List<String> resultList;
        try (final ZipInputStream zipInput = new ZipInputStream(bufferedInput)) {
            final ZipEntry zipEntry = zipInput.getNextEntry();
            LOG.debug("Reading zipEntry {}",zipEntry.getName());
            resultList = readDataFromStream(zipInput);
        }
        return resultList;
    }

    private List<String> readDataFromStream(final InputStream inputStream) throws IOException {
        final List<String> resultList = new ArrayList<>();
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        try (final BufferedReader in = new BufferedReader(inputStreamReader)) {
            String inputLine;
            final String dataLineStart = ZIP_DATA_LINE_START + LocalDate.now().minusDays(1).format(dateFormatter);
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("#") || inputLine.startsWith(dataLineStart)) {
                    resultList.add(inputLine);
                    LOG.trace("Adding dataline: {}", inputLine);
                }
            }
        }
        return resultList;
    }

}
