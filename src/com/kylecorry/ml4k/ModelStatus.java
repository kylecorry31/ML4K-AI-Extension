package com.kylecorry.ml4k;

class ModelStatus {
    private int statusCode;
    private String message;
    private String projectType;

    /**
     * Default constructor
     * @param statusCode the status code
     * @param message the message
     */
    private ModelStatus(int statusCode, String message, String projectType){
        this.statusCode = statusCode;
        this.message = message;
        this.projectType = projectType;
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
        String type = JSONUtils.readStringProperty(json, "type");
        if (message == null || type == null){
            throw new ML4KException("JSON is not valid: " + json);
        }
        return new ModelStatus(code, message, type);
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

    public String getProjectType(){
        return projectType;
    }

    @Override
    public String toString() {
        return "ModelStatus{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", type='" + projectType + '\'' +
                '}';
    }
}
