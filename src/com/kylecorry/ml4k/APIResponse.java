package com.kylecorry.ml4k;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Scanner;

public class APIResponse {
    private String responseMessage;
    private int responseCode;
    private String body;

    APIResponse(int responseCode, String responseMessage, String body) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.body = body;
    }

    public static APIResponse fromConnection(HttpURLConnection conn) throws IOException {
        final int responseCode = conn.getResponseCode();
        final String responseMessage = conn.getResponseMessage();

        String body;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            body = read(conn.getInputStream());
        } else {
            body = read(conn.getErrorStream());
        }

        return new APIResponse(responseCode, responseMessage, body);
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getBody() {
        return body;
    }

    public boolean isOK() {
        return responseCode == HttpURLConnection.HTTP_OK;
    }

    public boolean isError() {
        return !isOK();
    }

    /**
     * Read an input stream to a String.
     *
     * @param is The input stream.
     * @return The data from the input stream as a String.
     */
    private static String read(InputStream is) {
        if (is == null){
            return "";
        }
        Scanner scanner = new Scanner(is);

        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }

        return sb.toString();
    }

}
