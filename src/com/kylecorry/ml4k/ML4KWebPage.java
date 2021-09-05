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
    private boolean modelReady = false;

    private String modelStatus = "Not trained";
    private int modelProgress = 0;
    private Date modelUpdated = new Date();

    private WebView browser;

    ML4KWebPage(WebView browserView) {
        Log.d(LOGPREFIX, "Creating JS/Java interface");
        browser = browserView;
        pageReady = false;
    }

    @JavascriptInterface
    public void setReady(boolean ready) {
        pageReady = ready;
    }
    public boolean isReady() {
        return pageReady;
    }


    @JavascriptInterface
    public void setModelReady(boolean ready) {
        modelReady = ready;
    }
    public boolean isModelReady() {
        return modelReady;
    }


    @JavascriptInterface
    public void setModelStatus(String status, int progress) {
        modelStatus = status;
        modelProgress = progress;
        modelUpdated = new Date();
    }

    public int getModelProgress() {
        return modelProgress;
    }
    public String getModelStatus() {
        return modelStatus;
    }


    public void trainNewModel() {
        Log.d(LOGPREFIX, "Training new TensorflowJS model");
        String scratchkey = "not-ready-yet";
        runWebpageFunction("ml4kTrainNewModel('" + scratchkey + "')");
    }

    private void runWebpageFunction(String function) {
        browser.evaluateJavascript("(function() { " + function + "; })();", null);
    }
}
