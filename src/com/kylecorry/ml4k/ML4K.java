package com.kylecorry.ml4k;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The ML4K API
 */
public class ML4K {

    private static final String BASE_URL = "https://machinelearningforkids.co.uk/api/scratch/%s";
    private static final String CLASSIFY_ENDPOINT = "/classify";
    private static final String MODELS_ENDPOINT = "/models";
    private static final String TRAIN_ENDPOINT = "/train";
    private static final String STATUS_ENDPOINT = "/status";

    private HttpStrategy http;

    private final Pattern apiKeyPattern = Pattern.compile(
        "[0-9a-f]{8}-[0-9a-f]{4}-[1][0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}" +
        "[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}"
    );

    private String apiKey;

    /**
     * Default constructor
     * @param apiKey the API key
     * @throws ML4KException if the API key is invalid
     */
    public ML4K(String apiKey) throws ML4KException {
        setAPIKey(apiKey);
        http = new HttpImpl();
    }

    /**
     * @param apiKey the ML4K API key
     * @throws ML4KException if the API key is invalid
     */
    public void setAPIKey(String apiKey) throws ML4KException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ML4KException("API key not set");
        }
        if (!apiKeyPattern.matcher(apiKey).matches()) {
            throw new ML4KException("API key isn't a Machine Learning for Kids key");
        }

        this.apiKey = apiKey;
    }


    /**
     * Classify an image
     * @param image the image
     * @return the classification of the image
     * @throws ML4KException when an error occurs
     */
    public Classification classify(File image) throws ML4KException {
        try {
            String dataStr = "{\"data\": " + "\"" + ImageEncoder.encode(image) + "\"}";
            URL url = new URL(getBaseURL() + CLASSIFY_ENDPOINT);
            APIResponse res = http.postJSON(url, dataStr);

            if (res.isOK()) {
                return Classification.fromJson(res.getBody());
            } else {
                APIErrorResponse response = APIErrorResponse.fromJson(res.getBody());
                throw new ML4KException(response == null ? "Bad response from server: " + res.getResponseCode() : response.getError());
            }
        } catch (MalformedURLException e) {
            throw new ML4KException("Could not generate URL");
        } catch (FileNotFoundException e) {
            throw new ML4KException("Could not load image file");
        } catch (IOException e) {
            throw new ML4KException("No Internet connection."); // TODO: standardize error messages
        }
    }

    /**
     * Classify some text
     * @param text the text
     * @return the classification of the text
     * @throws ML4KException when an error occurs
     */
    public Classification classify(String text) throws ML4KException {
        try {
            String urlStr = getBaseURL() + CLASSIFY_ENDPOINT + "?data=" + URLEncoder.encode(text, "UTF-8");
            URL url = new URL(urlStr);
            APIResponse res = http.getJSON(url);
            
            if (res.isOK()) {
                return Classification.fromJson(res.getBody());
            } else {
                APIErrorResponse response = APIErrorResponse.fromJson(res.getBody());
                throw new ML4KException(response == null ? "Bad response from server: " + res.getResponseCode() : response.getError());
            }
        } catch (UnsupportedEncodingException e) {
            throw new ML4KException("Could not encode text");
        } catch (MalformedURLException e) {
            throw new ML4KException("Could not generate URL");
        } catch (IOException e) {
            throw new ML4KException("No Internet connection.");
        }
    }

    /**
     * Classify some numbers
     * @param numbers the numbers
     * @return the classification of the numbers
     * @throws ML4KException when an error occurs
     */
    public Classification classify(List<Double> numbers) throws ML4KException {
        try {
            String urlStr = getBaseURL() + CLASSIFY_ENDPOINT + "?" + urlEncodeList("data", numbers);
            URL url = new URL(urlStr);
            APIResponse res = http.getJSON(url);
            
            if (res.isOK()) {
                return Classification.fromJson(res.getBody());
            } else {
                APIErrorResponse response = APIErrorResponse.fromJson(res.getBody());
                throw new ML4KException(response == null ? "Bad response from server: " + res.getResponseCode() : response.getError());
            }

        } catch (UnsupportedEncodingException e) {
            throw new ML4KException("Could not encode numbers");
        } catch (MalformedURLException e) {
            throw new ML4KException("Could not generate URL");
        } catch (IOException e) {
            throw new ML4KException("No Internet connection.");
        }
    }

    /**
     * Adds text training data
     * @param label the label of the data
     * @param text the data
     * @throws ML4KException if there is an error
     */
    public void addTrainingData(String label, String text) throws ML4KException {
        try {
            URL url = new URL(getBaseURL() + TRAIN_ENDPOINT);
            APIResponse res = http.postJSON(url, "{ \"data\": \"" + text + "\", \"label\": \"" + label + "\" }");
            
            if (res.isOK()) {
                // Do nothing
            } else {
                APIErrorResponse response = APIErrorResponse.fromJson(res.getBody());
                throw new ML4KException(response == null ? "Bad response from server: " + res.getResponseCode() : response.getError());
            }
        } catch (UnsupportedEncodingException e) {
            throw new ML4KException("Could not encode numbers");
        } catch (MalformedURLException e) {
            throw new ML4KException("Could not generate URL");
        } catch (IOException e) {
            throw new ML4KException("No Internet connection.");
        }
    }

    /**
     * Adds image training data
     * @param label the label of the data
     * @param image the data
     * @throws ML4KException if there is an error
     */
    public void addTrainingData(String label, File image) throws ML4KException {
        try {
            addTrainingData(label, ImageEncoder.encode(image));
        } catch (IOException e) {
            throw new ML4KException("Could not encode image");
        }
    }

    /**
     * Adds numbers training data
     * @param label the label of the data
     * @param numbers the data
     * @throws ML4KException if there is an error
     */
    public void addTrainingData(String label, List<Double> numbers) throws ML4KException {
        addTrainingData(label, numbers.toString()); // TODO: this might not be right
    }


    /**
     * @return the model's status
     * @throws ML4KException when any error occurs or if the status is undefined
     */
    public ModelStatus getModelStatus() throws ML4KException {
        try{
            URL url = new URL(getBaseURL() + STATUS_ENDPOINT);
            APIResponse res = http.getJSON(url);
         
            if (res.isOK()) {
                return ModelStatus.fromJson(res.getBody());
            } else {
                APIErrorResponse response = APIErrorResponse.fromJson(res.getBody());
                throw new ML4KException(response == null ? "Bad response from server: " + res.getResponseCode() : response.getError());
            }
        } catch (MalformedURLException e) {
            throw new ML4KException("Could not generate URL");
        } catch (IOException e) {
            throw new ML4KException("Unable to connect to ML4K servers");
        }
    }


    /**
     * Train the model
     * @throws ML4KException when an error occurs
     */
    public void train() throws ML4KException {
        try {
            URL url = new URL(getBaseURL() + MODELS_ENDPOINT);
            APIResponse res = http.postJSON(url, "{}");
            
            if (res.isOK()) {
                // Do nothing
            } else {
                APIErrorResponse response = APIErrorResponse.fromJson(res.getBody());
                throw new ML4KException(response == null ? "Bad response from server: " + res.getResponseCode() : response.getError());
            }

        } catch (MalformedURLException e) {
            throw new ML4KException("Could not generate URL");
        } catch (IOException e) {
            throw new ML4KException("No Internet connection.");
        }
    }


    /**
     * @return the base URL of the ML4K API
     */
    private String getBaseURL(){
        return String.format(BASE_URL, apiKey);
    }

    /**
     * Encode a list for a URL get request.
     * @param paramName The name of the parameter.
     * @param list The list to encode.
     * @return The encoded list.
     */
    private String urlEncodeList(String paramName, List<?> list) {
        StringBuilder sb = new StringBuilder();
        if (list == null || list.size() == 0){
            return "";
        }

        for (int i = 0; i < list.size(); i++) {
            sb.append(paramName);
            sb.append('=');
            sb.append(list.get(i));
            if (i != list.size() - 1){
                sb.append('&');
            }
        }

        return sb.toString();
    }


    // For testing


    void setHttpStrategy(HttpStrategy http){
        this.http = http;
    }

}