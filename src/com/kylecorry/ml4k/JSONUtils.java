package com.kylecorry.ml4k;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtils {

    private JSONUtils(){
    }

    public static String readStringProperty(String json, String propertyName){
        String pattern = "\"" + propertyName + "\"\\s*:\\s*\"([\\w\\W]+)\"";
        Pattern compiled = Pattern.compile(pattern);

        Matcher matches = compiled.matcher(json);
        if (matches.find()){
            return matches.group(0);
        }

        return null;
    }

    public static int readIntProperty(String json, String propertyName){
        String pattern = "\"" + propertyName + "\"\\s*:\\s*(\\d+)";
        Pattern compiled = Pattern.compile(pattern);

        Matcher matches = compiled.matcher(json);
        if (matches.find()){
            return Integer.parseInt(matches.group(0));
        }

        return -1;
    }

    public static double readRealProperty(String json, String propertyName){
        String pattern = "\"" + propertyName + "\"\\s*:\\s*([\\d.]+)";
        Pattern compiled = Pattern.compile(pattern);

        Matcher matches = compiled.matcher(json);
        if (matches.find()){
            return Double.parseDouble(matches.group(0));
        }

        return -1;
    }
}
