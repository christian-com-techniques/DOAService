package com.inqubu.doa.doaservice;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by VORHECHR on 24.04.2015.
 */
public class JNDeserializer implements JsonDeserializer {
        public DataModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject jo = json.getAsJsonObject();
            String key = jo.get("key").toString().replaceAll("^\"|\"$", "");
            String value = jo.get("value").toString().replaceAll("^\"|\"$", "");
            String timestamp = jo.get("timestamp").toString().replaceAll("^\"|\"$", "");
            String scheme = jo.get("scheme").toString().replaceAll("^\"|\"$", "");
            String ttl = jo.get("ttl").toString().replaceAll("^\"|\"$", "");

            if(value.startsWith("[") && value.endsWith("]")) {
                JsonArray ja = jo.get("value").getAsJsonArray();
                ArrayList<DataChunk> al = new ArrayList<DataChunk>();
                for(int i=0;i<ja.size();i++) {
                    JsonObject je = ja.get(i).getAsJsonObject();
                    String key_v = je.get("key").toString().replaceAll("^\"|\"$", "");
                    String value_v = je.get("value").toString().replaceAll("^\"|\"$", "");
                    String scheme_v = je.get("scheme").toString().replaceAll("^\"|\"$", "");
                    String timestamp_v = je.get("timestamp").toString().replaceAll("^\"|\"$", "");
                    String ttl_v = je.get("ttl").toString().replaceAll("^\"|\"$", "");
                    DataChunk dc = new DataChunk(key_v, value_v, scheme_v, timestamp_v, ttl_v);
                    al.add(dc);
                }
                return new DataGroup(key, al);
            } else {
                return new DataChunk(key, value, scheme, timestamp, ttl);
            }
        }
}
