package com.kylecorry.ml4k;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class ML4KWebPage {

    private static final String LOGPREFIX = "ML4KWebPage";
    private boolean pageReady = false;
    private boolean modelReady = false;

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


    public void trainNewModel() {
        Log.d(LOGPREFIX, "Training new TensorflowJS model");
        runWebpageFunction("ml4kTrainNewModel()");
    }




    private void runWebpageFunction(String function) {
        browser.evaluateJavascript("(function() { " + function + "; })();", null);
    }

    private void getStringFromWebpage(String function, ValueCallback<String> callback) {
        browser.evaluateJavascript("(function() { return " + function + "; })();", callback);
    }
}
