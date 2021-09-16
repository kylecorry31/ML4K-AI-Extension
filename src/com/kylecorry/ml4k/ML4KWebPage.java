package com.kylecorry.ml4k;

import java.util.Date;
import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class ML4KWebPage {

    private static final String LOGPREFIX = "ML4KWebPage";
    private boolean pageReady = false;

    private String currentScratchKey;

    private String modelStatus = "Not trained";
    private int modelProgress = 0;

    private WebView browser;

    private ML4KComponent callback;

    ML4KWebPage(WebView browserView, String scratchkey, ML4KComponent parent) {
        Log.d(LOGPREFIX, "Creating JS/Java interface");
        browser = browserView;
        pageReady = false;
        currentScratchKey = scratchkey;
        callback = parent;
    }

    @JavascriptInterface
    public void setReady(boolean ready) {
        pageReady = ready;
    }
    public boolean isReady() {
        return pageReady;
    }


    @JavascriptInterface
    public String getInitialScratchKey() {
        return currentScratchKey;
    }


    @JavascriptInterface
    public void setModelStatus(String status, int progress) {
        modelStatus = status;
        modelProgress = progress;
    }

    public int getModelProgress() {
        return modelProgress;
    }
    public String getModelStatus() {
        return modelStatus;
    }

    @JavascriptInterface
    public void classifyResponse(String label, double confidence){
        Log.d(LOGPREFIX, "Received classify response");
        callback.GotClassification(label, label, confidence);
    }

    @JavascriptInterface
    public void returnErrorMessage(String errormessage) {
        Log.d(LOGPREFIX, "Returning error " + errormessage);
        callback.displayErrorMessage(errormessage);
    }

    public void trainNewModel(String scratchkey) {
        Log.d(LOGPREFIX, "Training new TensorflowJS model");
        runWebpageFunction("ml4kTrainNewModel('" + scratchkey + "')");
    }

    public void submitClassificationRequest(String base64imagedata) {
        Log.d(LOGPREFIX, "Classifying image");
        runWebpageFunction("ml4kClassifyImage('" + base64imagedata + "')");
    }

    private void runWebpageFunction(String function) {
        browser.evaluateJavascript("(function() { " + function + "; })();", null);
    }
}
