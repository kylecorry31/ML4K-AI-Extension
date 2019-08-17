package com.kylecorry.ml4k;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpImpl implements HttpStrategy {

    private static final String ML4K_USER_AGENT = "MIT App Inventor (ML4K extension)";

    @Override
    public APIResponse getJSON(URL url) throws IOException  {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", ML4K_USER_AGENT);

        // Get response
        APIResponse res = APIResponse.fromConnection(conn);
        conn.disconnect();
        return res;
    }

    @Override
    public APIResponse postJSON(URL url, String data) throws IOException {
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
        APIResponse res = APIResponse.fromConnection(conn);
        conn.disconnect();
        return res;
    }
}
