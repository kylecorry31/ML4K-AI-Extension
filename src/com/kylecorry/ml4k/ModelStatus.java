package com.kylecorry.ml4k;

class ModelStatus {
    private int statusCode;
    private String message;

    /**
     * Default constructor
     * @param statusCode the status code
     * @param message the message
     */
    private ModelStatus(int statusCode, String message){
        this.statusCode = statusCode;
        this.message = message;
    }

    /**
     * Loads a model status from JSON
     * @param json the JSON
     * @return the model status
     * @throws ML4KException if the JSON does not represent a model status
     */
    public static ModelStatus fromJson(String json) throws ML4KException {
        int code = JSONUtils.readIntProperty(json, "status");
        String message = JSONUtils.readStringProperty(json, "msg");
        if (message == null){
            throw new ML4KException("JSON is not valid: " + json);
        }
        return new ModelStatus(code, message);
    }

    /**
     * @return the status code (0 = error, 1 = training, 2 = ready)
     */
    public int getStatusCode(){
        return statusCode;
    }

    /**
     * @return the message
     */
    public String getMessage(){
        return message;
    }
}
