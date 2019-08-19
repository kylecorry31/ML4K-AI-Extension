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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
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
public final class ML4KComponent extends AndroidNonvisibleComponent {
    
    private final Activity activity;

    private String key = getKeyFromFile();

    public ML4KComponent(ComponentContainer container) {
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
            return MediaUtil.copyMediaToTempFile(form, path);
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
}
