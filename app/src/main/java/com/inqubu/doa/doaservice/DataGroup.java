package com.inqubu.doa.doaservice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by VORHECHR on 22.04.2015.
 */
public class DataGroup extends DataModel {

    private String key;
    private String scheme;
    private String authenticationservice;
    private String checksum;
    private ArrayList<DataChunk> value;
    private String timestamp;
    private String createdAt;
    private int ttl;
    private boolean oldflag;

    public DataGroup(String key, ArrayList<DataChunk> datachunks) {
        this.key = key;
        this.value = datachunks;
        this.authenticationservice = "http://www.inqubu.com/doa_auth";
        this.scheme = "http://www.inqubu.com/doa_example";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.timestamp = sdf.format(new Date());
        this.createdAt = sdf.format(new Date());
        this.oldflag = false;
    }

    public DataGroup(String key) {
        this.key = key;
        this.value = new ArrayList<DataChunk>();
        this.authenticationservice = "http://www.inqubu.com/doa_auth";
        this.scheme = "http://www.inqubu.com/doa_example";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.timestamp = sdf.format(new Date());
        this.createdAt = sdf.format(new Date());
        this.oldflag = false;
    }

    public void addChunk(DataChunk dc) {
        value.add(dc);
    }

    public ArrayList<DataChunk> getValue()  {
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

    public int getTTL() { return ttl; }

}
