package com.inqubu.doa.doaservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by VORHECHR on 21.04.2015.
 */
public class DataChunk extends DataModel {

    private String key;
    private String scheme;
    private String value;
    private String timestamp;
    private String createdAt;
    private String authenticationservice;
    private String checksum;
    private int ttl;
    private boolean oldflag;

    public DataChunk(String key, String value) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.timestamp = sdf.format(new Date());
        this.createdAt = sdf.format(new Date());
        this.key = key;
        this.value = value;
        this.checksum = "84zrbn2bo2n88732eonc";
        this.authenticationservice = "http://www.inqubu.com/doa_auth";
        this.scheme = "http://www.inqubu.com/doa_example";
        this.ttl = 0;
        this.oldflag = false;
    }

    public DataChunk(String key, String value, String scheme, String timestamp, String ttl) {
        this.timestamp = timestamp;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.createdAt = sdf.format(new Date());
        this.key = key;
        this.value = value;
        this.checksum = "84zrbn2bo2n88732eonc";
        this.authenticationservice = "http://www.inqubu.com/doa_auth";
        this.scheme = scheme;
        this.ttl = Integer.parseInt(ttl);
        this.oldflag = false;
    }

    public void incrementCounter() {
        this.ttl = this.ttl+1;
    }

    public String getValue()  {
        return value;
    }

    public String getTimestamp()  {
        return timestamp;
    }

    public String getKey()  {
        return key;
    }

    public String getScheme()  {
        return scheme;
    }

    public String getCreatedAt() { return createdAt; }

    public int getTTL() { return ttl; }

    public void setOldflag(boolean value) {
        this.oldflag = value;
    }

    public boolean getOldFlag() { return oldflag; }


}
