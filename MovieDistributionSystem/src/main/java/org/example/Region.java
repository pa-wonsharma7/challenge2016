package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Region {

    private String city;
    private String province;
    private String country;

    private static Map<String, String> cityToProvince = new HashMap<>();
    private static Map<String, String> provinceToCountry = new HashMap<>();

    public static void readCSV(String file) {
        try {
            Scanner scanner = new Scanner(new File(file));
            if(scanner.hasNextLine()) scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                String city = values[3].trim();
                String province = values[4].trim();
                String country = values[5].trim();
                cityToProvince.put(city.toUpperCase(), province.toUpperCase());
                provinceToCountry.put(province.toUpperCase(), country.toUpperCase());
            }
        } catch (FileNotFoundException e) {
            System.out.println("CSV file not found");
        }
    }

    public Region(String region) {
        String[] parts = region.split("-");
        if(parts.length == 1) {
            this.country = parts[0].trim();
        }
        if(parts.length == 2) {
            this.country = parts[1].trim();
            this.province = parts[0].trim();
        }
        if(parts.length == 3) {
            this.city = parts[0].trim();
            this.province = parts[1].trim();
            this.country = parts[2].trim();
        }
    }

    public static int getCSVSize() {
        return cityToProvince.size();
    }

    public boolean isCityInProvince(String city, String province) {
        if(cityToProvince.containsKey(city)) {
            return Objects.equals(cityToProvince.get(city), province);
        } else {
            throw new RuntimeException("City " + city + ", not in CSV file");
        }
    }

    public boolean isProvinceInCountry(String province, String country) {
        if(provinceToCountry.containsKey(province)) {
            return Objects.equals(provinceToCountry.get(province), country);
        } else {
            throw new RuntimeException("Province " + province + ", not in CSV file");
        }
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getCountry() {
        return country;
    }
}
