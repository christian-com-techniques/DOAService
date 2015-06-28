package com.inqubu.doa.doaservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Date;
import java.text.DateFormat;

/**
 * Created by VORHECHR on 21.04.2015.
 */
public class DOAAccessHandler {

    private static List<DataModel> private_datachunks = new ArrayList<DataModel>();
    private static List<DataModel> public_datachunks = new ArrayList<DataModel>();

    public DOAAccessHandler() {

    }

    public static void resetPublicChunks() {
        public_datachunks.clear();
    }

    //For debugging
    public static String printValues() {
        String msg = "";
        for(int i=0;i<public_datachunks.size();i++) {
            DataModel dm = public_datachunks.get(i);
            if(dm instanceof DataChunk) {
                DataChunk dc = (DataChunk)dm;
                msg += i+1 + ". "+dc.getKey()+", "+dc.getTimestamp()+"\n";
                //msg += "key: "+dc.getKey()+", value: "+dc.getValue()+", timestamp: "+dc.getTimestamp()+", old: "+dc.getOldFlag()+", ttl: "+dc.getTTL()+"\n";
            } else {
                DataGroup dc = (DataGroup)dm;
                msg += i+1 + ". "+dc.getKey()+", "+dc.getTimestamp()+"\n";
                //msg += "key: "+dc.getKey()+", value: "+dc.getValue()+", timestamp: "+dc.getTimestamp()+"\n";
            }
        }
        return msg;
    }

    public static DataGroup getPublicData() {
        DataGroup datagroup = new DataGroup("doa");
        for(int i=0;i<public_datachunks.size();i++) {
            DataModel dm = public_datachunks.get(i);
            if(dm instanceof DataChunk) {
                DataChunk dc = (DataChunk)dm;
                if(dc.getOldFlag() == false) {
                    datagroup.addChunk(dc);
                }
            }
        }
        return datagroup;
    }

    public static void addValue(DataModel d) {

        String com_key;
        String com_scheme;
        String com_timestamp;
        Date com_date = new Date();
        Date date = new Date();
        boolean addflag = false;
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        if(d instanceof DataChunk) {
            DataChunk dc = (DataChunk)d;
            com_key = dc.getKey();
            com_scheme = dc.getScheme();
            com_timestamp = dc.getTimestamp();
            try {
                com_date = sdf.parse(com_timestamp);
            } catch(ParseException e) {
                System.out.println("PARSE EXCEPTION");
                e.printStackTrace();
            }
        } else {
            DataGroup dc = (DataGroup)d;
            com_key = dc.getKey();
            com_scheme = dc.getScheme();
            com_timestamp = dc.getTimestamp();
            try {
                com_date = sdf.parse(com_timestamp);
            } catch(ParseException e) { e.printStackTrace(); }
        }

        for(int i=0;i<public_datachunks.size();i++) {
            DataModel dcm = public_datachunks.get(i);
            if(d instanceof DataChunk) {
                DataChunk dct = (DataChunk)dcm;
                try {
                    String date_s = dct.getTimestamp();
                    date = sdf.parse(date_s);
                } catch(ParseException e) { e.printStackTrace(); }

                //System.out.println(com_scheme+" == "+dct.getScheme()+" und "+com_key+" == "+dct.getKey());

                if(com_scheme.equals(dct.getScheme()) && com_key.equals(dct.getKey())) {
                    //System.out.println(com_date.toString()+" == "+date.toString());
                    if(com_date.after(date)) {
                        removeValue(dcm);
                        addflag = true;
                        break;
                    } else {
                        addflag = false;
                        break;
                    }
                } else {
                    addflag = true;
                }
            } else {
                DataGroup dct = (DataGroup)dcm;
                try {
                    date = sdf.parse(dct.getTimestamp());
                } catch(ParseException e) { e.printStackTrace(); }
                if(com_scheme.equals(dct.getScheme()) && com_key.equals(dct.getKey())) {
                    if(com_date.after(date)) {
                        removeValue(dcm);
                        addflag = true;
                        break;
                    } else {
                        break;
                    }
                } else {
                    addflag = true;
                    break;
                }
            }
        }
        if(public_datachunks.size() == 0) {
            addflag = true;
        }
        if(addflag) {
            public_datachunks.add(d);
        }
    }

    public static void removeValue(DataModel d) {
        public_datachunks.remove(d);
    }

    public static DataModel convertJsonToChunk(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DataModel.class, new JNDeserializer())
                .create();
        DataModel dc = gson.fromJson(json, DataModel.class);
        return dc;
    }

    public static String convertChunkToJson(DataModel dc) {
        Gson gson = new Gson();
        String json = gson.toJson(dc).toString();
        return json;
    }

    public static List getPublicDataChunks() {
        return public_datachunks;
    }

}
