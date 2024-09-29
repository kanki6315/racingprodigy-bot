package com.racingprodigy.standings_bot.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class CsvHelper {

    public static Set<Integer> readUniqueIracingCustomerIDsFromClassPath(String csvFilePath) throws Exception {
        return readUniqueIracingCustomerIDs(new InputStreamReader(
                CsvHelper.class.getClassLoader().getResourceAsStream(csvFilePath)));
    }

    public static Set<Integer> readUniqueIracingCustomerIDs(InputStreamReader streamReader) throws Exception {
        Set<Integer> uniqueCustomerIDs = new HashSet<>();
        try (BufferedReader br = new BufferedReader(streamReader)) {
            String line;
            String[] headers = br.readLine().split(",");
            int customerIDIndex = -1;

            // Find the index of the `iRacing Customer ID` in the header
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals("iRacing Customer ID")) {
                    customerIDIndex = i;
                    break;
                }
            }

            if (customerIDIndex == -1) {
                throw new IllegalArgumentException("CSV does not contain 'iRacing Customer ID' header");
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > customerIDIndex) {
                    try {
                        uniqueCustomerIDs.add(Integer.parseInt(values[customerIDIndex].trim()));
                    } catch (NumberFormatException e) {
                        // unable to parse number id from input
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return uniqueCustomerIDs;
    }
}
