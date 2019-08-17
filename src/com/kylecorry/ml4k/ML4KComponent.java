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

    public String getKeyFromFile(){
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



    @SimpleFunction(description = "Get the classification for the image.")
    public void ClassifyImage(final String path) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    final String imageData = getImageData(path);
                    ML4K ml4k = new ML4K(key);
                    Classification classification = ml4k.classifyImage(imageData);
                    GotClassification(path, classification.getClassification(), classification.getConfidence());
                } catch (ML4KException e) {
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
                    Classification classification = ml4k.classifyText(data);
                    GotClassification(data, classification.getClassification(), classification.getConfidence());
                } catch (ML4KException e) {
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
                List<Double> numbersList = new ArrayList<Double>(numbers.size());
                for (int i = 0; i < numbers.size(); i++){
                    String s = numbers.getString(i);
                    numbersList.add(Double.parseDouble(s));
                }
                try {
                    ML4K ml4k = new ML4K(key);
                    Classification classification = ml4k.classifyNumbers(numbersList);
                } catch (ML4KException e) {
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
            } catch (ML4KException e) {
                GotError("train", e.getMessage());
            }
        }
    });

    }

    @SimpleFunction(description = "Adds training data to the model")
    public void AddTrainingData(String label, String data) {
      // TODO: Implement this, must detect type of data (list, string, image) - might have to either overload or rename methods
      // Post to /api/scratch/:scratchkey/train
      // Payload: {"data": "...", "label": "..."}
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
                } catch (ML4KException e) {
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
        final Component component = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(component, "GotStatus", statusCode, message);
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
     * Turn the data of an image into base 64.
     *
     * @param path The path to the image.
     * @return The data of the image as a base 64 string.
     */
    private String getImageData(String path) {
        try {
            java.io.File file = MediaUtil.copyMediaToTempFile(form, path);


            byte[] byteArray = readAllBytes(new FileInputStream(file));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              return Base64.getEncoder().encodeToString(byteArray);
            } else {
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

    private void runInBackground(Runnable runnable) {
        AsynchUtil.runAsynchronously(runnable);
    }
}
