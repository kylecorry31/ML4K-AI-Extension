package com.kylecorry.ml4k;

import java.io.IOException;
import java.net.URL;

public interface HttpStrategy {

    /**
     * Get JSON from the URL
     * @param url the URL
     * @return the JSON response
     */
    APIResponse getJSON(URL url) throws IOException;

    /**
     * Post JSON to the URL
     * @param url the URL
     * @param data the POST data
     * @return the JSON response
     */
    APIResponse postJSON(URL url, String data) throws IOException;

}
