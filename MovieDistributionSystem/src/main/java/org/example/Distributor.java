package org.example;

import java.util.*;

public class Distributor {

    private final String name;
    private List<Distributor> subDistributors;
    private Distributor parent;
    public Map<String, Region> includes = new HashMap<>();
    public Map<String, Region> excludes = new HashMap<>();

    public Distributor(String name) {
        this.name = name;
        this.parent = null;
        this.subDistributors = new ArrayList<>();
    }

    public void setParent(Distributor parent) {
        this.parent = parent;
        if (parent != null) {
            parent.subDistributors.add(this);
        }
    }

    public Distributor getParent() {
        return parent;
    }

    public String getName() {
        return this.name;
    }

    public boolean isProvinceAllowed(Region region) {
        return region.isProvinceInCountry(region.getProvince(), region.getCountry());
    }

    public boolean isCityAllowed(Region region) {
        return region.isCityInProvince(region.getCity(), region.getProvince());
    }

    public boolean isAllowedToInclude(Region region) {

        if(excludes.containsKey(region.getCountry())) {
            System.out.println(region.getCountry() + " is excluded in " + this.name);
            return false;
        }
        else if(excludes.containsKey(region.getProvince() + "-" + region.getCountry())) {
            System.out.println(region.getProvince() + " is excluded in " + this.name);
            return false;
        }
        else if(excludes.containsKey(region.getCity() + "-" + region.getProvince() + "-" + region.getCountry())) {
            System.out.println(region.getCity() + " is excluded in " + this.name);
            return false;
        }

        String provinceCountry = region.getProvince() + "-" + region.getCountry();
        String cityProvinceCountry = region.getCity() + "-" + region.getProvince() + "-" + region.getCountry();
        // includes contains "INDIA"
        if(includes.containsKey(region.getCountry())) {
            // input -> "INDIA"
            if(region.getProvince() == null) return true;
            // input -> "KARNATAKA-INDIA"
            if(region.getCity() == null) return isProvinceAllowed(region);
            // input -> "HUBLI-KARNATAKA-INDIA"
            else return isCityAllowed(region);
        }
        // includes contains "KARNATAKA-INDIA"
        else if(includes.containsKey(provinceCountry)) {
            // input -> "KARNATAKA-INDIA"
            Region includedRegion = includes.get(provinceCountry);
            if(Objects.equals(includedRegion.getProvince(), region.getProvince()) &&
                    Objects.equals(includedRegion.getCountry(), region.getProvince()))
                return true;
            // input -> "HUBLI-KARNATAKA-INDIA"
            return isCityAllowed(region);
        }
        // includes contains "MUMBAI-MAHARASHTRA-INDIA"
        else if (includes.containsKey(cityProvinceCountry)) {
            Region includedRegion = includes.get(cityProvinceCountry);
            return (Objects.equals(includedRegion.getCity(), region.getCity()) &&
                    Objects.equals(includedRegion.getProvince(), region.getProvince()) &&
                    Objects.equals(includedRegion.getCountry(), region.getCountry()));
        }
        return false;
    }

    public void addInclude(String regionString) {

        if(regionString.isEmpty())
            return;

        Region region = new Region(regionString);

        if(this.parent == null) {
            includes.put(regionString, region);
        }
        if(this.parent != null && this.parent.isAllowedToInclude(region)) {
            includes.put(regionString, region);
        }
    }

    public boolean isAllowedToExclude(Region region) {
        // Can only exclude subset of include
        if(includes.containsKey(region.getCountry())) {
            if(isProvinceAllowed(region) && region.getCity() == null) {
                return true;
            }
            else if(isProvinceAllowed(region) && isCityAllowed(region)) {
                return true;
            }
            return isCityAllowed(region);
        }
        else if(includes.containsKey(region.getProvince() + "-" + region.getCountry())) {
            return isCityAllowed(region);
        }
        return false;
    }

    public void addExclude(String regionString) {

        if(regionString.isEmpty())
            return;

        Region region = new Region(regionString);
        if(this.parent == null && this.isAllowedToExclude(region)) {
            excludes.put(regionString, region);
        }
        if(this.parent != null && this.parent.isAllowedToExclude(region)) {
            this.excludes.put(regionString, region);
            Map<String, Region> pExcludes = this.parent.excludes;
            excludes.putAll(pExcludes);
        }
    }

    public boolean isInExcludes(Region region) {
        String provinceCountry = region.getProvince() + "-" + region.getCountry();
        String cityProvinceCountry = region.getCity() + "-" + region.getProvince() + "-" + region.getCountry();
        if(excludes.containsKey(provinceCountry)) {
            Region excludedRegion = excludes.get(provinceCountry);
            if(region.getCity() == null &&
                    Objects.equals(excludedRegion.getProvince(), region.getProvince()) &&
                    Objects.equals(excludedRegion.getCountry(), region.getCountry())) {
                return true;
            }
            return region.getCity() != null && isCityAllowed(region);
        }
        else if(excludes.containsKey(cityProvinceCountry)) {
            Region excludedRegion = excludes.get(cityProvinceCountry);
            return Objects.equals(excludedRegion.getCountry(), region.getCountry()) &&
                    Objects.equals(excludedRegion.getProvince(), region.getProvince()) &&
                    Objects.equals(excludedRegion.getCity(), region.getCity());
        }
        return false;
    }

    public boolean isInIncludes(Region region) {
        String country = region.getCountry();
        String provinceCountry = region.getProvince() + "-" + region.getCountry();
        String cityProvinceCountry = region.getCity() + "-" + region.getProvince() + "-" + region.getCountry();
        if(includes.containsKey(country)) {
            Region includedRegion = includes.get(country);
            if(region.getProvince() == null && Objects.equals(includedRegion.getCountry(), country)) {
                return true;
            }
            if(region.getCity() == null && region.getProvince() != null && isProvinceAllowed(region)) {
                return true;
            }
            if(isCityAllowed(region)) {
                return true;
            }
        }
        // includes contains "Karnataka-India"
        else if(includes.containsKey(provinceCountry)) {
            Region includedRegion = includes.get(provinceCountry);
            // input -> "Maharashtra-India"/"Karnataka-India"
            if(region.getCity() == null && region.getProvince() != null &&
                    (Objects.equals(includedRegion.getProvince(), region.getProvince()) &&
                            Objects.equals(includedRegion.getCountry(), region.getCountry()))) {
                return true;
            }
            // input -> "Hubli-Karnataka-India"/"Mumbai-Maharashtra-India"
            if(includedRegion.getCountry() == region.getCountry() &&
                    includedRegion.getProvince() == region.getProvince() &&
                    isCityAllowed(region)) {
                return true;
            }
        }
        // includes contains "Mumbai-Maharashtra-India"
        // input -> has to be of form "a-b-c"
        else if(includes.containsKey(cityProvinceCountry)) {
            Region includedRegion = includes.get(cityProvinceCountry);
            return Objects.equals(includedRegion.getCity(), region.getCity()) &&
                    Objects.equals(includedRegion.getProvince(), region.getProvince()) &&
                    Objects.equals(includedRegion.getCountry(), region.getCountry());
        }
        return false;
    }

    public boolean canDistributeIn(String regionString) {
        Region region = new Region(regionString);
        Distributor current = this;
        if(current.parent == null) {
            if(isInExcludes(region)) {
                return false;
            }
            if(isInIncludes(region)) {
                return true;
            }
        }
        if(current.parent != null) {
            if(isInExcludes(region)) {
                return false;
            }
            return parent.canDistributeIn(regionString);
        }
        return false;
    }

    public void print(String prefix) {
        System.out.println(prefix + name);
        for(Distributor subDistributor: subDistributors) {
            subDistributor.print(prefix + "  ");
        }
    }


}
