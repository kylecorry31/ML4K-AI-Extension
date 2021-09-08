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
import com.google.appinventor.components.runtime.WebViewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
        description = "This provides an interface for the Machine Learning for Kids website.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/ml4k.png")
@UsesAssets(fileNames = "api.txt, ml4k.html, ml4k.js, promisepool.js, tf.min.js, model.json, group1-shard1of1, group10-shard1of1, group11-shard1of1, group12-shard1of1, group13-shard1of1, group14-shard1of1, group15-shard1of1, group16-shard1of1, group17-shard1of1, group18-shard1of1, group19-shard1of1, group2-shard1of1, group20-shard1of1, group21-shard1of1, group22-shard1of1, group23-shard1of1, group24-shard1of1, group25-shard1of1, group26-shard1of1, group27-shard1of1, group28-shard1of1, group29-shard1of1, group3-shard1of1, group30-shard1of1, group31-shard1of1, group32-shard1of1, group33-shard1of1, group34-shard1of1, group35-shard1of1, group36-shard1of1, group37-shard1of1, group38-shard1of1, group39-shard1of1, group4-shard1of1, group40-shard1of1, group41-shard1of1, group42-shard1of1, group43-shard1of1, group44-shard1of1, group45-shard1of1, group46-shard1of1, group47-shard1of1, group48-shard1of1, group49-shard1of1, group5-shard1of1, group50-shard1of1, group51-shard1of1, group52-shard1of1, group53-shard1of1, group54-shard1of1, group55-shard1of1, group6-shard1of1, group7-shard1of1, group8-shard1of1, group9-shard1of1")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class ML4KComponent extends AndroidNonvisibleComponent {

    private static final String LOGPREFIX = "ML4KComponent";

    private final Activity activity;

    private String key = getKeyFromFile();

    private WebView browser = null;
    private ML4KWebPage webPageObj = null;

    private final String ML4KURL = "https://machinelearningforkids.co.uk/appinventor-assets/";

    public ML4KComponent(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();

        browser = prepareBrowser();
        loadWebPage();
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

    @SimpleFunction(description = "Get the classification for the image.")
    public void ClassifyImage(final String path) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    final java.io.File image = loadImageFile(path);
                    ML4K ml4k = new ML4K(key);
                    Classification classification = ml4k.classify(image);
                    GotClassification(path, classification.getClassification(), classification.getConfidence());
                    image.delete();
                } catch (Exception e) {
                    GotError(path, e.getMessage());
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
                    ML4K ml4k = new ML4K(key);
                    Classification classification = ml4k.classify(data);
                    GotClassification(data, classification.getClassification(), classification.getConfidence());
                } catch (Exception e) {
                    GotError(data, e.getMessage());
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
                    ML4K ml4k = new ML4K(key);
                    Classification classification = ml4k.classify(convertYailListToDouble(numbers));
                    GotClassification(data, classification.getClassification(), classification.getConfidence());
                } catch (Exception e) {
                    GotError(data, e.getMessage());
                }
            }
        });
    }

    @SimpleFunction(description = "Train new machine learning model")
    public void TrainNewModel() {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ML4K ml4k = new ML4K(key);
                    ml4k.train();
                } catch (Exception e) {
                    GotError("train", e.getMessage());
                }
            }
        });
    }

    @SimpleFunction(description = "Adds an image training data to the model")
    public void AddImageTrainingData(final String label, final String path) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    java.io.File image = loadImageFile(path);
                    if (image == null){
                        throw new ML4KException("Could not load image");
                    }
                    ML4K ml4k = new ML4K(key);
                    ml4k.addTrainingData(label, image);
                } catch (Exception e) {
                    GotError("train", e.getMessage());
                }
            }
        });
    }

    @SimpleFunction(description = "Adds a text training data to the model")
    public void AddTextTrainingData(final String label, final String text) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ML4K ml4k = new ML4K(key);
                    ml4k.addTrainingData(label, text);
                } catch (Exception e) {
                    GotError("train", e.getMessage());
                }
            }
        });
    }

    @SimpleFunction(description = "Adds numbers training data to the model")
    public void AddNumbersTrainingData(final String label, final YailList numbers){
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ML4K ml4k = new ML4K(key);
                    ml4k.addTrainingData(label, convertYailListToDouble(numbers));
                } catch (Exception e) {
                    GotError("train", e.getMessage());
                }
            }
        });
    }

    @SimpleFunction(description = "Gets the status of the model")
    public void GetModelStatus() {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ML4K ml4k = new ML4K(key);
                    ModelStatus status = ml4k.getModelStatus();
                    GotStatus(status.getStatusCode(), status.getMessage());
                } catch (Exception e) {
                    GotError("status", e.getMessage());
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
        broadcastEvent("GotStatus", statusCode, message);
    }

    /**
     * Event indicating that a classification got an error.
     *
     * @param data  The data
     * @param error The error
     */
    @SimpleEvent
    public void GotError(final String data, final String error) {
        broadcastEvent("GotError", data, error);
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
        broadcastEvent("GotClassification", data, classification, confidence);
    }

    // Helpers

    /**
     * Broadcasts an event on the UI thread
     * @param eventName the name of the event
     * @param data the data of the event
     */
    private void broadcastEvent(final String eventName, final Object...data){
        final Component component = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(component, eventName, data);
            }
        });
    }

    /**
     * Converts a Yail List to a double list
     * @param list the list
     * @return the double list
     */
    private List<Double> convertYailListToDouble(YailList list){
        List<Double> numbersList = new ArrayList<Double>(list.size());
        for (int i = 0; i < list.size(); i++){
            String s = list.getString(i);
            numbersList.add(Double.parseDouble(s));
        }
        return numbersList;
    }

    /**
     * Turn the data of an image into base 64.
     *
     * @param path The path to the image.
     * @return The data of the image as a base 64 string.
     */
    private java.io.File loadImageFile(String path) {
        try {
            java.io.File image = MediaUtil.copyMediaToTempFile(form, path);
            return ImageResizer.resize(image, 224, 224);
        } catch (Exception e) {
            GotError(path, e.getMessage());
        }
        return null;
    }

    /**
     * Loads the key from api.txt if exists
     * @return the API key
     */
    private String getKeyFromFile(){
        try {
            InputStream inputStream = form.openAssetForExtension(ML4KComponent.this, "api.txt");
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

    /**
     * Runs a function in the background
     * @param runnable the runnable object
     */
    private void runInBackground(Runnable runnable) {
        AsynchUtil.runAsynchronously(runnable);
    }



    private void displayErrorMessage(String errormessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
            .setTitle("Machine Learning for Kids")
            .setMessage(errormessage)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            })
            .create()
            .show();
    }



    @SimpleFunction(description = "Train new machine learning model")
    public void TrainNewTfjsModel() {
        if (webPageObj.isReady()) {
            if (key == null || key.trim().isEmpty()) {
                displayErrorMessage("API key required");
            }
            else {
                webPageObj.trainNewModel(key);
            }
        }
        else {
            displayErrorMessage("Not ready yet. Please try again in a moment");
        }
    }

    @SimpleProperty
    public String GetTfjsModelState() {
        return webPageObj.getModelStatus() + " " + webPageObj.getModelProgress();
    }

    @SimpleFunction(description = "Get the classification for the image.")
    public void ClassifyImageTfjs(final String path) {
        if (!webPageObj.getModelStatus().equals("Available")) {
            displayErrorMessage("Please train a model first");
            return;
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
                Log.d(LOGPREFIX, "classifying image....");
                try {
                    Log.d(LOGPREFIX, "getting and resizing image");
                    final java.io.File image = loadImageFile(path);

                    Log.d(LOGPREFIX, "encoding image");
                    final String imagedata = ImageEncoder.encode(image);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webPageObj.submitClassificationRequest(imagedata);
                        }
                    });

                    image.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                    GotError(path, e.getMessage());
                }
            }
        });
    }


    private WebResourceResponse prepareAssetForBrowser(String filename) throws IOException {
        Log.d(LOGPREFIX, "loading from assets " + filename);
        InputStream fileStream = form.openAssetForExtension(ML4KComponent.this, filename);
        String mime = "text/plain";
        if (filename.endsWith(".json")) {
            mime = "application/json";
        }
        else if (filename.endsWith(".js")) {
            mime = "text/javascript";
        }
        else if (filename.endsWith(".html")) {
            mime = "text/html";
        }
        else if (filename.endsWith("-shard1of1")) {
            mime = "application/octet-stream";
        }
        else {
            Log.d(LOGPREFIX, "not available in assets " + filename);
            throw new IOException("File not included in assets");
        }

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Access-Control-Allow-Origin", "*");
        return new WebResourceResponse(mime, "UTF-8", 200, "OK", responseHeaders, fileStream);
    }


    private WebView prepareBrowser() {
        Log.d(LOGPREFIX, "Creating browser to use for TensorFlow.js");
        WebView webView = new WebView(activity);

        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, String url) {
                Log.d(LOGPREFIX, "shouldInterceptRequest " + url);

                if (url.startsWith(ML4KURL)) {
                    try {
                        String localUrl = url.substring(ML4KURL.length());
                        return prepareAssetForBrowser(localUrl);
                    }
                    catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }

                return super.shouldInterceptRequest(v, url);
            }
        });

        return webView;
    }


    private void loadWebPage() {
        try {
            Log.d(LOGPREFIX, "binding to Java object");
            webPageObj = new ML4KWebPage(browser, key.trim(), this);
            browser.addJavascriptInterface(webPageObj, "ML4KJavaInterface");

            Log.d(LOGPREFIX, "loading tfjs web page");
            browser.loadUrl(ML4KURL + "ml4k.html");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
