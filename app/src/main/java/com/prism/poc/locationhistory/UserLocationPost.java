package com.prism.poc.locationhistory;

/**
 * Created by Krishna Upadhya on 20/03/20.
 */
public class UserLocationPost {

    private String updatedTime;

    private String emailAddress;

    private PostAddress address;

    public String getUpdatedTime ()
    {
        return updatedTime;
    }

    public void setUpdatedTime (String updatedTime)
    {
        this.updatedTime = updatedTime;
    }

    public String getEmailAddress ()
    {
        return emailAddress;
    }

    public void setEmailAddress (String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public PostAddress getAddress ()
    {
        return address;
    }

    public void setAddress (PostAddress address)
    {
        this.address = address;
    }

}
