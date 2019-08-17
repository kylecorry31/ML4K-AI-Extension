package com.kylecorry.ml4k;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.runtime.util.MediaUtil;

import android.app.Activity;
import android.os.Build;
import android.net.Uri;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Pattern;

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
        description = "This provides an interface for the Machine Learning for Kids website.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/ml4k.png")
@UsesAssets(fileNames = "api.txt")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class ML4K extends AndroidNonvisibleComponent {

    private static final String ML4K_USER_AGENT = "MIT App Inventor (ML4K extension)";
    private static final String BASE_URL = "https://machinelearningforkids.co.uk/api/scratch/%s";
    private static final String CLASSIFY_ENDPOINT = "/classify";
    private static final String MODELS_ENDPOINT = "/model";
    private static final String TRAIN_ENDPOINT = "/train";
    private static final String STATUS_ENDPOINT = "/status";
    private static final String DATA_KEY = "data";

    private final Activity activity;

    private String key = getKeyFromFile();

    public ML4K(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(description = "The API key for the ML4K app.")
    public void Key(String key) {
        this.key = key;
    }

    @SimpleProperty
    public String Key() {
        return key;
    }

    public String getKeyFromFile(){
        try {
            InputStream inputStream = form.openAssetForExtension(ML4K.this, "api.txt");
            Scanner scanner = new Scanner(inputStream);
            if (scanner.hasNext()){
              return scanner.next();
            } else {
              return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private final Pattern apiKeyPattern = Pattern.compile(
        "[0-9a-f]{8}-[0-9a-f]{4}-[1][0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}" +
        "[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}"
    );

    private void checkApiKey() throws ML4KException {
        if (key == null || key.isEmpty()) {
            throw new ML4KException("API key not set");
        }
        if (apiKeyPattern.matcher(key).matches() == false) {
            throw new ML4KException("API key isn't a Machine Learning for Kids key");
        }
    }



    @SimpleFunction(description = "Get the classification for the image.")
    public void ClassifyImage(final String path) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    // check we have something that looks like a usable API key
                    checkApiKey();

                    // Get the data
                    final String imageData = getImageData(path);
                    // URLEncoder.encode(imageData, "UTF-8")
                    String dataStr = "{\"data\": " + "\"" + imageData + "\"}";

                    // Setup the request
                    URL url = new URL(getURL() + CLASSIFY_ENDPOINT);
                    HttpResponse res = postJSON(url, dataStr);

                    if (res.isOK()) {
                        // Parse JSON
                        Classification classification = Classification.fromJson(path, res.getBody());
                        if (classification == null){
                            GotError(path, "Bad data from server: " + res.getBody());
                        } else {
                            GotClassification(classification.data, classification.classification, classification.confidence);
                        }
                    } else {
                        APIErrorResponse error = getErrorMessage(res.getResponseMessage(), res.getBody());
                        GotError(path, error.getError());
                    }
                } catch (ML4KException e) {
                    GotError(path, e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    GotError(path, "Could not encode image");
                } catch (MalformedURLException e) {
                    GotError(path, "Could not generate URL");
                } catch (IOException e) {
                    GotError(path, "No Internet connection.");
                }

            }
        });
    }

    @SimpleFunction(description = "Get the classification for the text.")
    public void ClassifyText(final String data) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    // check we have something that looks like a usable API key
                    checkApiKey();

                    // Get the data
                    String urlStr = getURL() + CLASSIFY_ENDPOINT + "?data=" + URLEncoder.encode(data, "UTF-8");

                    // Setup the request
                    URL url = new URL(urlStr);
                    HttpResponse res = getJSON(url);
                    
                    if (res.isOK()) {
                        // Parse JSON
                        Classification classification = Classification.fromJson(data, res.getBody());
                        if (classification == null){
                            GotError(data, "Bad data from server: " + res.getBody());
                        } else {
                            GotClassification(classification.data, classification.classification, classification.confidence);
                        }
                    } else {
                        APIErrorResponse error = getErrorMessage(res.getResponseMessage(), res.getBody());
                        GotError(data, error.getError());
                    }
                } catch (ML4KException e) {
                    GotError(data, e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    GotError(data, "Could not encode text");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    GotError(data, "Could not generate URL");
                } catch (IOException e) {
                    GotError(data, "No Internet connection.");
                }


            }
        });
    }

    @SimpleFunction(description = "Get the classification for the numbers.")
    public void ClassifyNumbers(final YailList numbers) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                final String data = numbers.toString();
                try {
                    // check we have something that looks like a usable API key
                    checkApiKey();

                    // Get the data
                    String urlStr = getURL() + CLASSIFY_ENDPOINT + "?" + urlEncodeList("data", numbers);

                    // Setup the request
                    URL url = new URL(urlStr);
                    HttpResponse res = getJSON(url);
                    
                    if (res.isOK()) {
                        // Parse JSON
                        Classification classification = Classification.fromJson(data, res.getBody());
                        if (classification == null){
                            GotError(data, "Bad data from server: " + res.getBody());
                        } else {
                            GotClassification(classification.data, classification.classification, classification.confidence);
                        }
                    } else {
                        APIErrorResponse error = getErrorMessage(res.getResponseMessage(), res.getBody());
                        GotError(data, error.getError());
                    }

                } catch (ML4KException e) {
                    GotError(data, e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    GotError(data, "Could not encode text");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    GotError(data, "Could not generate URL");
                } catch (IOException e) {
                    GotError(data, "No Internet connection.");
                }


            }
        });
    }


    private static HttpResponse getJSON(final URL url) throws IOException { // TODO: Handle IO exceptions
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("User-Agent", ML4K_USER_AGENT);

      // Get response
      HttpResponse res = HttpResponse.fromConnection(conn);
      conn.disconnect();
      return res;
    }

    private static HttpResponse postJSON(final URL url, final String data) throws IOException { // TODO: Handle IO exceptions
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setFixedLengthStreamingMode(data.length());
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", ML4K_USER_AGENT);

        // Write the post data
        conn.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        os.writeBytes(data);
        os.flush();
        os.close();

        // Get response
        HttpResponse res = HttpResponse.fromConnection(conn);
        conn.disconnect();
        return res;
    }



    @SimpleFunction(description = "Train new machine learning model")
    public void TrainNewModel() {
       runInBackground(new Runnable() {
        @Override
        public void run() {
            final String TRAIN_KEY = "train";
            try {
                // check we have something that looks like a usable API key
                checkApiKey();

                // Get the data
                String urlStr = getURL() + MODELS_ENDPOINT;

                // Setup the request
                URL url = new URL(urlStr);
                HttpResponse res = postJSON(url, "");
                
                if (res.isOK()) {
                    // Do nothing
                } else {
                    APIErrorResponse error = getErrorMessage(res.getResponseMessage(), res.getBody());
                    GotError(TRAIN_KEY, error.getError());
                }

            } catch (ML4KException e) {
                GotError(TRAIN_KEY, e.getMessage());
            } catch (UnsupportedEncodingException e) {
                GotError(TRAIN_KEY, "Could not encode text");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                GotError(TRAIN_KEY, "Could not generate URL");
            } catch (IOException e) {
                GotError(TRAIN_KEY, "No Internet connection.");
            }
        }
    });

    }

    @SimpleFunction(description = "Adds training data to the model")
    public void AddTrainingData(String label, String data) {
      // TODO: Implement this, must detect type of data (list, string, image)
      // Post to /api/scratch/:scratchkey/train
      // Payload: {"data": "...", "label": "..."}
    }

    @SimpleFunction(description = "Gets the status of the model")
    public void GetModelStatus() {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                final String STATUS_KEY = "status";
                try {
                    // check we have something that looks like a usable API key
                    checkApiKey();

                    // Get the data
                    String urlStr = getURL() + STATUS_ENDPOINT;

                    // Setup the request
                    URL url = new URL(urlStr);
                    HttpResponse res = getJSON(url);
                    
                    if (res.isOK()) {
                        // Parse JSON
                        ModelStatus modelStatus = ModelStatus.fromJson(res.getBody());
                        GotStatus(modelStatus.getStatusCode(), modelStatus.getMessage());
                    } else {
                        APIErrorResponse error = getErrorMessage(res.getResponseMessage(), res.getBody());
                        GotError(STATUS_KEY, error.getError());
                    }

                } catch (ML4KException e) {
                    GotError(STATUS_KEY, e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    GotError(STATUS_KEY, "Could not encode text");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    GotError(STATUS_KEY, "Could not generate URL");
                } catch (IOException e) {
                    GotError(STATUS_KEY, "No Internet connection.");
                }
            }
        });
    }

    /**
     * Event fired when the status check completes.
     *
     * @param statusCode  The status code of the model (2 = ready, 1 = training, 0 = error)
     * @param message     The status message of the model
     */
    @SimpleEvent
    public void GotStatus(final int statusCode, final String message) {
        final Component component = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(component, "GotStatus", statusCode, message);
            }
        });
    }



    private APIErrorResponse getErrorMessage(String responseCode, String json) {
        APIErrorResponse errorMessage = APIErrorResponse.fromJson(json);
        if (errorMessage == null) {
            errorMessage = new APIErrorResponse("Bad response from server: " + responseCode);
        }
        return errorMessage;
    }


    /**
     * Event indicating that a classification got an error.
     *
     * @param data  The data
     * @param error The error
     */
    @SimpleEvent
    public void GotError(final String data, final String error) {
        final Component component = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(component, "GotError", data, error);
            }
        });
    }

    /**
     * Event indicating that a classification has finished.
     *
     * @param data           The data
     * @param classification The classification
     * @param confidence     The confidence of the classification
     */
    @SimpleEvent
    public void GotClassification(final String data, final String classification, final double confidence) {
        final Component component = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(component, "GotClassification", data, classification, confidence);
            }
        });
    }

    // Helpers

    /**
     * Read an input stream to a String.
     *
     * @param is The input stream.
     * @return The data from the input stream as a String.
     */
    private static String read(InputStream is) {
        Scanner scanner = new Scanner(is);

        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }

        return sb.toString();
    }

    /**
     * Get the base URL for ML4K.
     *
     * @return The base URL with the key for ML4K.
     */
    private String getURL() {
        return String.format(BASE_URL, key);
    }

    /**
     * Turn the data of an image into base 64.
     *
     * @param path The path to the image.
     * @return The data of the image as a base 64 string.
     */
    private String getImageData(String path) {
        try {

            // if (MediaUtil.isExternalFileUrl(path)){
            //   path = new java.io.File(new URL(path).toURI()).getAbsolutePath();
            // }
            //
            java.io.File file = MediaUtil.copyMediaToTempFile(form, path);


            byte[] byteArray = readAllBytes(new FileInputStream(file));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              // byte[] byteArray = Files.readAllBytes(new java.io.File(path).toPath());
              return Base64.getEncoder().encodeToString(byteArray);
            } else {
              // byte[] byteArray = readAllBytes(path);
              return Base64Encoder.encode(byteArray);
            }
        } catch (IOException e) {
          GotError(path, "File not found");
        }
        return "";
    }

    /**
     * Reads all bytes from a file.
     * @param is The input stream.
     * @return The file contents as bytes.
     * @throws IOException upon error reading the input file.
     */
    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int read;
        while ((read = is.read(buff)) != -1) {
            byteArrayOutputStream.write(buff, 0, read);
        }

        byte[] out = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        is.close();

        return out;
    }

    /**
     * Reads all bytes from a file.
     * @param filePath The path to the file.
     * @return The file contents as bytes.
     * @throws IOException upon error reading the input file.
     */
    private byte[] readAllBytes(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        byte[] buff = new byte[4096];
        int read;
        while ((read = fis.read(buff)) != -1) {
            byteArrayOutputStream.write(buff, 0, read);
        }

        byte[] out = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        fis.close();

        return out;
    }


    private void runInBackground(Runnable runnable) {
        AsynchUtil.runAsynchronously(runnable);
    }

    /**
     * Encode a list for a URL get request.
     * @param paramName The name of the parameter.
     * @param list The list to encode.
     * @return The encoded list.
     */
    private String urlEncodeList(String paramName, YailList list) {
        StringBuilder sb = new StringBuilder();
        if (list == null || list.size() == 0){
            return "";
        }

        for (int i = 0; i < list.size(); i++) {
            sb.append(paramName);
            sb.append('=');
            sb.append(list.getObject(i));
            if (i != list.size() - 1){
                sb.append('&');
            }
        }

        return sb.toString();
    }



    private static class APIErrorResponse {
        private String error;
        private APIErrorResponse(String error) {
            this.error = error;
        }
        private String getError() {
            return this.error;
        }

        private static APIErrorResponse fromJson(String json) {
            int indexErrorMessage = json.indexOf("error");
            if (indexErrorMessage == -1) {
                return null;
            }

            int errorMessageStart = json.indexOf("\"", indexErrorMessage + "error".length() + 2);
            int errorMessageEnd = json.indexOf("\"", errorMessageStart + 1);

            if (errorMessageStart >= json.length() || errorMessageEnd >= json.length() ||
                errorMessageStart == -1 || errorMessageEnd == -1) {
                return null;
            }

            String error = json.substring(errorMessageStart + 1, errorMessageEnd);
            if (error.equals("Missing data")) {
                error = "Empty or invalid data sent to Machine Learning for Kids";
            }
            else if (error.equals("Scratch key not found")) {
                error = "Machine Learning for Kids API Key not recognised";
            }

            return new APIErrorResponse(error);
        }
    }

    private static class HttpResponse {
      private String responseMessage;
      private int responseCode;
      private String body;

      private HttpResponse(int responseCode, String responseMessage, String body) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.body = body;
      }

      private static HttpResponse fromConnection(HttpURLConnection conn) throws IOException {
        final int responseCode = conn.getResponseCode();
        final String responseMessage = conn.getResponseMessage();

        String body;

        if (responseCode == HttpURLConnection.HTTP_OK){
            body = read(conn.getInputStream());
        } else {
            body = read(conn.getErrorStream());
        }

        return new HttpResponse(responseCode, responseMessage, body);
      }

      public String getResponseMessage(){
        return responseMessage;
      }

      public int getResponseCode(){
        return responseCode;
      }

      public String getBody(){
        return body;
      }

      public boolean isOK(){
          return responseCode == HttpURLConnection.HTTP_OK;
      }

      public boolean isError(){
          return !isOK();
      }

    }


    private static class Classification {
        private String data;
        private String classification;
        private double confidence;

        private Classification(String data, String classification, double confidence) {
            this.data = data;
            this.classification = classification;
            this.confidence = confidence;
        }

        private static Classification fromJson(String data, String json) {

            if (json == null){
              return null;
            }

            int indexClassName = json.indexOf("class_name");
            int indexConfidence = json.indexOf("confidence");

            if (indexClassName == -1 || indexConfidence == -1){
                return null;
            }

            int classNameStart = json.indexOf("\"", indexClassName + 12);
            int classNameEnd = json.indexOf("\"", classNameStart + 1);

            if (classNameStart >= json.length() || classNameEnd >= json.length()){
                return null;
            }

            String className = json.substring(classNameStart + 1, classNameEnd);

            int confidenceStart = json.indexOf(":", indexConfidence);
            int confidenceEnd = json.indexOf(",", confidenceStart + 1);

            if (confidenceStart >= json.length() || confidenceEnd >= json.length()){
                return null;
            }

            String confidenceStr = json.substring(confidenceStart + 1, confidenceEnd).trim();
            double confidence = Double.parseDouble(confidenceStr);

            return new Classification(data, className, confidence);
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }

    private static class Base64Encoder {

    /**
     * Encode a string into base64 format.
     * @param s The string to encode.
     * @return The input string, encoded into a base64 string.
     */
    public static String encode(String s){
        return encode(s.getBytes());
    }

    /**
     * Encode a byte array into base64 format.
     * @param bytes The byte array to encode.
     * @return The input byte array, encoded into a base64 string.
     */
    public static String encode(byte[] bytes){

        StringBuilder s = new StringBuilder();

        for (int i = 0; i < bytes.length; i+=3) {
            byte b1 = bytes[i];
            byte b2 = i+1 < bytes.length ? bytes[i+1] : 0;
            byte b3 = i+2 < bytes.length ? bytes[i+2] : 0;

            int current = packBytesIntoInt(byteToUnsignedInt(b1), byteToUnsignedInt(b2), byteToUnsignedInt(b3));
            s.append(lookup(byteToUnsignedInt(read6Bits(0, current))));
            s.append(lookup(byteToUnsignedInt(read6Bits(1, current))));
            if (i + 1 < bytes.length) {
                s.append(lookup(byteToUnsignedInt(read6Bits(2, current))));
            }
            if (i + 2 < bytes.length){
                s.append(lookup(byteToUnsignedInt(read6Bits(3, current))));
            }
        }

        int eqsNeeded = (bytes.length % 3);
        if (eqsNeeded == 2){
            s.append('=');
        } else if (eqsNeeded == 1){
            s.append("==");
        }

        return s.toString();
    }

    /**
     * Convert a byte to an unsigned int.
     * @param b The byte to convert.
     * @return The unsigned int version of the byte.
     */
    static int byteToUnsignedInt(byte b){
        return ((int) b) & 0xFF;
    }

    /**
     * Pack 3 bytes into an integer, from left to right.
     *  Byte 1  Byte 2  Byte 3  Zeros
     * 11111111222222223333333300000000
     * @param byte1 The left most byte
     * @param byte2 The middle byte
     * @param byte3 The right most byte
     * @return An integer containing all three bytes and right padded with zeros.
     */
    static int packBytesIntoInt(int byte1, int byte2, int byte3){
        return ((byte1 << 24) + (byte2 << 16) + (byte3 << 8));
    }

    /**
     * Read 6 bits from an integer.
     * @param index The index to read bits from [0, 1, 2, 3].
     * @param values The integer to get bits from.
     * @return The 6 bits from the values int, left padded with 2 zeros.
     */
    static byte read6Bits(int index, int values){
        if (index >= 4 || index < 0){
            return 0;
        }
        int i = index * 6;
        int mask = 0b111111 << (26 - i);
        return (byte) ((values & mask) >> (26 - i) & 0b00111111);
    }

    /**
     * Lookup a value in a base64 table.
     * @param val The value to lookup [0, 63]
     * @return The character associated with the value in base64.
     */
    private static char lookup(int val){
        if (val <= 25){
            return (char) ('A' + val);
        } else if (val <= 51){
            return (char) ('a' + val - 26);
        } else if (val <= 61){
            return (char) ('0' + val - 52);
        } else if (val == 62){
            return '+';
        } else {
            return '/';
        }
    }

}


}
