package com.kylecorry.ml4k;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

class HttpUtils {

    private static final String ML4K_USER_AGENT = "MIT App Inventor (ML4K extension)";

    private HttpUtils(){}

    public static Response getJSON(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", ML4K_USER_AGENT);
  
        // Get response
        Response res = Response.fromConnection(conn);
        conn.disconnect();
        return res;
      }
  
      public static Response postJSON(final URL url, final String data) throws IOException {
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
          Response res = Response.fromConnection(conn);
          conn.disconnect();
          return res;
      }

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


    public static class Response {
        private String responseMessage;
        private int responseCode;
        private String body;
  
        private Response(int responseCode, String responseMessage, String body) {
          this.responseCode = responseCode;
          this.responseMessage = responseMessage;
          this.body = body;
        }
  
        private static Response fromConnection(HttpURLConnection conn) throws IOException {
          final int responseCode = conn.getResponseCode();
          final String responseMessage = conn.getResponseMessage();
  
          String body;
  
          if (responseCode == HttpURLConnection.HTTP_OK){
              body = read(conn.getInputStream());
          } else {
              body = read(conn.getErrorStream());
          }
  
          return new Response(responseCode, responseMessage, body);
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
}