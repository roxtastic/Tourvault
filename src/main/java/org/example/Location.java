package org.example;
public class Location {
    private String county;
    private Integer sirutaCode;
    private String locality;
    private String adminUnit;
    private String address;
    private Integer latitude;
    private Integer longitude;


    public Location(String county, Integer sirutaCode) {
        this.county = county;
        this.sirutaCode = sirutaCode;
    }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public Integer getSirutaCode() { return sirutaCode; }
    public void setSirutaCode(Integer sirutaCode) { this.sirutaCode = sirutaCode; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getAdminUnit() { return adminUnit; }
    public void setAdminUnit(String adminUnit) { this.adminUnit = adminUnit; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getLatitude() { return latitude; }
    public void setLatitude(Integer latitude) { this.latitude = latitude; }

    public Integer getLongitude() { return longitude; }
    public void setLongitude(Integer longitude) { this.longitude = longitude; }
}