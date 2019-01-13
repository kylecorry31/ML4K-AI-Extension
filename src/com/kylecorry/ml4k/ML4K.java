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

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
        description = "This provides an interface for the Machine Learning for Kids website.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/ml4k.png")
@UsesAssets(fileNames = "api.txt")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class ML4K extends AndroidNonvisibleComponent {

    private static final String ENDPOINT_URL = "https://machinelearningforkids.co.uk/api/scratch/%s/classify";
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

    @SimpleFunction(description = "Get the classification for the image.")
    public void ClassifyImage(final String path) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    if (key == null || key.isEmpty()) {
                      GotError(path, "API key not set");
                      return;
                    }
                    // Get the data
                    final String imageData = getImageData(path);
                    // URLEncoder.encode(imageData, "UTF-8")
                    String dataStr = "{\"data\": " + "\"" + imageData + "\"}";

                    // Setup the request
                    URL url = new URL(getURL());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setFixedLengthStreamingMode(dataStr.length());
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");
                    // Send image data
                    conn.setDoOutput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(dataStr);
                    os.flush();
                    os.close();

                    // Parse
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        final String json = read(conn.getInputStream());
                        conn.disconnect();

                        // Parse JSON
                        Classification classification = Classification.fromJson(path, json);
                        if (classification == null){
                            GotError(path, "Bad data from server: " + json);
                        } else {
                            GotClassification(classification.data, classification.classification, classification.confidence);
                        }
                    } else {
                        GotError(path, "Bad response from server: " + conn.getResponseCode());
                        conn.disconnect();
                    }

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
                    if (key == null || key.isEmpty()) {
                      GotError(data, "API key not set");
                      return;
                    }
                    // Get the data
                    String urlStr = getURL() + "?data=" + URLEncoder.encode(data, "UTF-8");

                    // Setup the request
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");

                    // Parse
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        final String json = read(conn.getInputStream());
                        conn.disconnect();

                        // Parse JSON
                        Classification classification = Classification.fromJson(data, json);
                        if (classification == null){
                            GotError(data, "Bad data from server: " + json);
                        } else {
                            GotClassification(classification.data, classification.classification, classification.confidence);
                        }
                    } else {
                        GotError(data, "Bad response from server: " + conn.getResponseCode());
                        conn.disconnect();
                    }

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
                    if (key == null || key.isEmpty()) {
                      GotError(data, "API key not set");
                      return;
                    }
                    // Get the data
                    String urlStr = getURL() + "?" + urlEncodeList("data", numbers);

                    // Setup the request
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");

                    // Parse
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        final String json = read(conn.getInputStream());
                        conn.disconnect();

                        // Parse JSON
                        Classification classification = Classification.fromJson(data, json);
                        if (classification == null){
                            GotError(data, "Bad data from server: " + json);
                        } else {
                            GotClassification(classification.data, classification.classification, classification.confidence);
                        }
                    } else {
                        GotError(data, "Bad response from server: " + conn.getResponseCode());
                        conn.disconnect();
                    }

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
    private String read(InputStream is) {
        Scanner scanner = new Scanner(is);

        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }

        return sb.toString();
    }

    /**
     * Get the ENDPOINT_URL for ML4K.
     *
     * @return The ENDPOINT_URL with the key for ML4K.
     */
    private String getURL() {
        return String.format(ENDPOINT_URL, key);
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
