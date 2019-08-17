package com.kylecorry.ml4k;

public class ModelStatus {
    private int statusCode;
    private String message;

    public ModelStatus(int statusCode, String message){
        this.statusCode = statusCode;
        this.message = message;
    }

    public static ModelStatus fromJson(String json) throws ML4KException {
        int code = JSONUtils.readIntProperty(json, "status");
        String message = JSONUtils.readStringProperty(json, "msg");
        if (message == null){
            throw new ML4KException("JSON is not valid: " + json);
        }
        return new ModelStatus(code, message);
    }

    public int getStatusCode(){
        return statusCode;
    }

    public String getMessage(){
        return message;
    }
}
