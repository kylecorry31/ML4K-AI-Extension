package com.kylecorry.ml4k;

class Classification {
    private String classification;
    private double confidence;

    private Classification(String classification, double confidence) {
        this.classification = classification;
        this.confidence = confidence;
    }

    /**
     * Loads a classification from JSON
     * @param json the JSON
     * @return the classification
     * @throws ML4KException if the JSON is invalid
     */
    public static Classification fromJson(String json) throws ML4KException {

        if (json == null){
          throw new ML4KException("JSON is not valid: " + json);
        }

        String className = JSONUtils.readStringProperty(json, "class_name");
        double confidence = JSONUtils.readRealProperty(json, "confidence");

        return new Classification(className, confidence);
    }

    /**
     * @return the classification
     */
    public String getClassification() {
        return classification;
    }

    /**
     * @return the confidence
     */
    public double getConfidence() {
        return confidence;
    }


    @Override
    public String toString() {
        return "Classification{" +
                "classification='" + classification + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}