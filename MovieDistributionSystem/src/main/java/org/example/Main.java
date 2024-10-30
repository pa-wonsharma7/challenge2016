package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static String parseInputJson(String file) {
        StringBuilder inputJson = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File(file));
            while(scanner.hasNextLine()) {
                inputJson.append(scanner.nextLine()).append('\n');
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input JSON file not found");
        }
        return inputJson.toString();
    }

    public static String[] parseRegion(String region) {
        if(!region.isEmpty()) {
            return region.split("-");
        }
        return new String[]{};
    }

    public static Map<String, Distributor> parsePermissions(String jsonData) {

        Map<String, Distributor> distributors = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode data = objectMapper.readTree(jsonData);
            Iterator<String> distributorNames = data.fieldNames();
            while (distributorNames.hasNext()) {
                String distributorName = distributorNames.next();
                Distributor distributor = distributors.computeIfAbsent(distributorName, Distributor::new);

                String parentString = data.get(distributorName).get("PARENT").asText();
                if(!parentString.isEmpty()) {
                    Distributor parent = distributors.computeIfAbsent(parentString, Distributor::new);
                    distributor.setParent(parent);
                }

                JsonNode includeArray = data.get(distributorName).get("INCLUDE");
                if (includeArray != null && includeArray.isArray()) {
                    for (JsonNode includeNode : includeArray) {
                        distributor.addInclude(includeNode.asText());
                    }
                }

                JsonNode excludeArray = data.get(distributorName).get("EXCLUDE");
                if (excludeArray != null && excludeArray.isArray()) {
                    for (JsonNode excludeNode : excludeArray) {
                        distributor.addExclude(excludeNode.asText());
                    }
                }

                distributors.put(distributorName, distributor);
            }
        } catch (JsonProcessingException e) {
            System.out.println("Unable to process json data");
        }
        return distributors;
    }

    public static void main(String[] args) {

        String csvFile = "MovieDistributionSystem/src/main/resources/cities.csv";
        Region.readCSV(csvFile);

        String permissionsFile = "MovieDistributionSystem/src/main/resources/permissions.json";
        String input = parseInputJson(permissionsFile);

        Map<String, Distributor> distributors = parsePermissions(input);

        String inputFile = "MovieDistributionSystem/src/main/resources/input.txt";

        try {
            Scanner scanner = new Scanner(new File(inputFile));

            Pattern distributorPattern = Pattern.compile("(DISTRIBUTOR\\d+)", Pattern.CASE_INSENSITIVE);
            Pattern regionPattern = Pattern.compile("in (([A-Z]+[\\- ]?)+)\\??", Pattern.CASE_INSENSITIVE);
            Distributor distributor = null;
            String region = null;
            int count = 0;
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                count += 1;
                Matcher distributorMatcher = distributorPattern.matcher(line);
                if(distributorMatcher.find()) {
                    String distributorName = distributorMatcher.group(1);
                    distributor = distributors.get(distributorName);
                }
                Matcher regionMatcher = regionPattern.matcher(line);
                if(regionMatcher.find()) {
                    region = regionMatcher.group(1);
                }
                if(distributor != null) {
                    if (distributor.canDistributeIn(region)) {
                        System.out.println("Yes, " + distributor.getName() + " can distribute in " + region);
                    } else {
                        System.out.println("No, " + distributor.getName() + " cannot distribute in " + region);
                    }
                }

            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}