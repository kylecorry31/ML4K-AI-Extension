package com.kylecorry.ml4k;

class Classification {
    private String classification;
    private double confidence;

    public Classification(String classification, double confidence) {
        this.classification = classification;
        this.confidence = confidence;
    }

    public static Classification fromJson(String json) throws ML4KException {

        if (json == null){
          throw new ML4KException("JSON is not valid: " + json);
        }

        String className = JSONUtils.readStringProperty(json, "class_name");
        double confidence = JSONUtils.readRealProperty(json, "confidence");

        return new Classification(className, confidence);
    }

    public String getClassification() {
        return classification;
    }

    public double getConfidence() {
        return confidence;
    }
}