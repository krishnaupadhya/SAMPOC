package com.prism.poc.locationhistory;

/**
 * Created by Krishna Upadhya on 20/03/20.
 */
public class PostAddress {

    private String zipCode;

    private String country;

    private String city;

    private String addressLine1;

    private GeoCode geoCode;

    private String addressLine2;

    private String state;

    public String getZipCode ()
    {
        return zipCode;
    }

    public void setZipCode (String zipCode)
    {
        this.zipCode = zipCode;
    }

    public String getCountry ()
    {
        return country;
    }

    public void setCountry (String country)
    {
        this.country = country;
    }

    public String getCity ()
    {
        return city;
    }

    public void setCity (String city)
    {
        this.city = city;
    }

    public String getAddressLine1 ()
    {
        return addressLine1;
    }

    public void setAddressLine1 (String addressLine1)
    {
        this.addressLine1 = addressLine1;
    }

    public GeoCode getGeoCode ()
    {
        return geoCode;
    }

    public void setGeoCode (GeoCode geoCode)
    {
        this.geoCode = geoCode;
    }

    public String getAddressLine2 ()
    {
        return addressLine2;
    }

    public void setAddressLine2 (String addressLine2)
    {
        this.addressLine2 = addressLine2;
    }

    public String getState ()
    {
        return state;
    }

    public void setState (String state)
    {
        this.state = state;
    }
}
