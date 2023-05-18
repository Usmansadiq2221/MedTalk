package com.devtwist.medtalk.Models;

public class CountryCodeData {

    private String countryName, countryCode, cAbbreviation;

    public CountryCodeData() {
    }

    public CountryCodeData(String countryName, String countryCode, String cAbbreviation) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.cAbbreviation = cAbbreviation;
    }



    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getcAbbreviation() {
        return cAbbreviation;
    }

    public void setcAbbreviation(String cAbbreviation) {
        this.cAbbreviation = cAbbreviation;
    }
}
