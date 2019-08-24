package com.kylecorry.ml4k;

class APIErrorResponse {
    private String error;
    public APIErrorResponse(String error) {
        this.error = error;
    }
    public String getError() {
        return this.error;
    }

    public static APIErrorResponse fromJson(String json) {
        String error = JSONUtils.readStringProperty(json, "error");

        if (error == null){
            return null;
        }

        if (error.equals("Missing data")) {
            error = "Empty or invalid data sent to Machine Learning for Kids";
        }
        else if (error.equals("Scratch key not found")) {
            error = "Machine Learning for Kids API Key not recognised";
        }

        return new APIErrorResponse(error);
    }
}