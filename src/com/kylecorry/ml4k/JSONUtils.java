package com.kylecorry.ml4k;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtils {

    private JSONUtils(){
    }

    /**
     * Reads a string property of a one dimensional JSON object
     * @param json the JSON
     * @param propertyName the name of the property to read
     * @return the property value, or null if it does not exist
     */
    public static String readStringProperty(String json, String propertyName){
        String pattern = "\"" + propertyName + "\"\\s*:\\s*\"([^\\\"]*)\"";
        Pattern compiled = Pattern.compile(pattern);

        Matcher matches = compiled.matcher(json);
        if (matches.find()){
            return matches.group(1);
        }

        return null;
    }

    /**
     * Reads an int property of a one dimensional JSON object
     * @param json the JSON
     * @param propertyName the name of the property to read
     * @return the property value, or -1 if it does not exist (not good indicator)
     */
    public static int readIntProperty(String json, String propertyName){
        String pattern = "\"" + propertyName + "\"\\s*:\\s*(\\d+)";
        Pattern compiled = Pattern.compile(pattern);

        Matcher matches = compiled.matcher(json);
        if (matches.find()){
            return Integer.parseInt(matches.group(1));
        }

        return -1;
    }

    /**
     * Reads a real/double property of a one dimensional JSON object
     * @param json the JSON
     * @param propertyName the name of the property to read
     * @return the property value, or NaN if it does not exist
     */
    public static double readRealProperty(String json, String propertyName){
        String pattern = "\"" + propertyName + "\"\\s*:\\s*([\\d.]+)";
        Pattern compiled = Pattern.compile(pattern);

        Matcher matches = compiled.matcher(json);
        if (matches.find()){
            return Double.parseDouble(matches.group(1));
        }

        return Double.NaN;
    }
}
