package com.kylecorry.ml4k;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.WebViewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    private final Map<String, String> scratchKeyTypes;

    // --------------------------------------------------------------------
    // MESSAGES DISPLAYED TO USERS IN DIALOG POP-UPS IN THE EVENT OF ERRORS
    // --------------------------------------------------------------------
    private final static String EXPLAIN_API_KEY_NEEDED = "A Machine Learning for Kids 'API key' is needed to use the machine learning blocks. This is a secret code you can copy from the App Inventor page on the Machine Learning for Kids website.";
    private final static String EXPLAIN_API_KEY_EXPIRED = "The Machine Learning for Kids website did not recognise your project API key. The most likely reason is that your ML project has been deleted.";
    private final static String EXPLAIN_API_KEY_INVALID = "Your API key does not look like a machine learning key. It is a secret code you have to copy from the App Inventor page on the Machine Learning for Kids website.";
    private final static String EXPLAIN_API_KEY_TYPE_TEXT = "You created a machine learning project to recognise text, so you can only use the text ML blocks in this project.";
    private final static String EXPLAIN_API_KEY_TYPE_NUMBERS = "You created a machine learning project to recognise numbers, so you can only use the numbers ML blocks in this project.";
    private final static String EXPLAIN_API_KEY_TYPE_IMAGES = "You created a machine learning project to recognise images, so you can only use the images ML blocks in this project.";
    private final static String EXPLAIN_API_KEY_TYPE_SOUNDS = "You created a machine learning project to recognise sounds, so you cannot use that block";
    private final static String EXPLAIN_ML_MODEL_NEEDED = "Please train a machine learning model before you try to do this.";



    public ML4KComponent(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();

        scratchKeyTypes = new HashMap<String, String>();

        if (!isApiKeyMissing()) {
            checkProjectType(new NextAction(NextStep.Init));
        }
    }


    // --------------------------------------------------------------------
    // DISPLAY USER FEEDBACK
    // --------------------------------------------------------------------

    public void displayErrorMessage(final String errormessage) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }



    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(description = "The API key for the ML4K app.")
    public void Key(String key) {
        Log.d(LOGPREFIX, "Updating API key to " + key);
        this.key = key.trim();

        if (!isApiKeyMissing()) {
            checkProjectType(new NextAction(NextStep.Init));
        }
    }

    @SimpleProperty
    public String Key() {
        return key;
    }

    // --------------------------------------------------------------------
    // ACTIONS REPRESENTED AS BLOCKS IN APP INVENTOR
    // --------------------------------------------------------------------

    @SimpleFunction(description = "Get the classification for the image.")
    public void ClassifyImage(final String path) {
        checkProjectType(new ClassifyImageAction(path));
    }

    @SimpleFunction(description = "Get the classification for the text.")
    public void ClassifyText(final String data) {
        checkProjectType(new ClassifyTextAction(data));
    }

    @SimpleFunction(description = "Get the classification for the numbers.")
    public void ClassifyNumbers(final YailList numbers) {
        checkProjectType(new ClassifyNumbersAction(numbers));
    }


    @SimpleFunction(description = "Train new machine learning model")
    public void TrainNewModel() {
        checkProjectType(new NextAction(NextStep.TrainModel));
    }


    @SimpleFunction(description = "Adds an image training data to the model")
    public void AddImageTrainingData(final String label, final String path) {
        checkProjectType(new ImageTrainingAction(label, path));
    }

    @SimpleFunction(description = "Adds a text training data to the model")
    public void AddTextTrainingData(final String label, final String text) {
        checkProjectType(new TextTrainingAction(label, text));
    }

    @SimpleFunction(description = "Adds numbers training data to the model")
    public void AddNumbersTrainingData(final String label, final YailList numbers){
        checkProjectType(new NumbersTrainingAction(label, numbers));
    }


    @SimpleFunction(description = "Gets the status of the model")
    public void GetModelStatus() {
        checkProjectType(new NextAction(NextStep.CheckModelStatus));
    }



    // --------------------------------------------------------------------
    // SUBMIT DATA TO ML4K SERVER FOR CLASSIFICATION
    //
    //  network access needs to happen on background threads
    // --------------------------------------------------------------------

    private void classifyTestDataOnServer(final NextAction classifyAction) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                String data = null;
                try {
                    ML4K ml4k = new ML4K(key);

                    Classification classification = null;
                    switch (classifyAction.step) {
                        case ClassifyText:
                            data = ((ClassifyTextAction)classifyAction).text;
                            classification = ml4k.classify(data);
                            break;
                        case ClassifyImage:
                            data = ((ClassifyImageAction)classifyAction).imagePath;
                            classification = ml4k.classify(loadImageFile(data));
                            break;
                        case ClassifyNumbers:
                            YailList numbers = ((ClassifyNumbersAction)classifyAction).numbers;
                            data = numbers.toString();
                            classification = ml4k.classify(convertYailListToDouble(numbers));
                            break;
                        default:
                            Log.d(LOGPREFIX, "Unexpected classify action");
                            return;
                    }

                    GotClassification(data, classification.getClassification(), classification.getConfidence());

                } catch (Exception e) {
                    GotError(data, e.getMessage());
                }
            }
        });
    }



    // --------------------------------------------------------------------
    // ADD TRAINING DATA TO ML4K SERVER
    //
    //  network access needs to happen on background threads
    // --------------------------------------------------------------------

    private void addTrainingDataToProject(final NextAction addTrainingAction) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ML4K ml4k = new ML4K(key);

                    switch (addTrainingAction.step) {
                        case AddTextTraining:
                            ml4k.addTrainingData(
                                ((TextTrainingAction)addTrainingAction).label,
                                ((TextTrainingAction)addTrainingAction).text);
                            break;
                        case AddImageTraining:
                            java.io.File image = loadImageFile(((ImageTrainingAction)addTrainingAction).path);
                            if (image == null) {
                                throw new ML4KException("Could not load image");
                            }
                            ml4k.addTrainingData(
                                ((ImageTrainingAction)addTrainingAction).label,
                                image);
                            break;
                        case AddNumbersTraining:
                            ml4k.addTrainingData(
                                ((NumbersTrainingAction)addTrainingAction).label,
                                convertYailListToDouble(((NumbersTrainingAction)addTrainingAction).numbers));
                            break;
                        default:
                            Log.d(LOGPREFIX, "Unexpected training action");
                            return;
                    }
                } catch (Exception e) {
                    GotError("train", e.getMessage());
                }
            }
        });
    }



    // --------------------------------------------------------------------
    // ML MODEL ACTIONS FOR MODELS HOSTED ON ML4K SERVER
    // --------------------------------------------------------------------

    private void trainModelWithWatson() {
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


    private void fetchModelStatusFromServer() {
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



    // --------------------------------------------------------------------
    // EVENTS FIRED BY THE EXTENSION
    // --------------------------------------------------------------------

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


    // --------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------

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

    private boolean isApiKeyMissing() {
        return key == null || key.trim().isEmpty();
    }





    // ---------------------------------------------------------------------
    // SUPPORT FOR TENSORFLOW.JS MODELS ON-DEVICE IN HIDDEN EMBEDDED BROWSER
    // ---------------------------------------------------------------------

    private void initialiseTensorFlow() {
        Log.d(LOGPREFIX, "initializing tensorflowjs");
        if (webPageObj == null) {
            activity.runOnUiThread(new Runnable()  {
                @Override
                public void run() {
                    browser = prepareBrowser();
                    loadWebPage();
                }
            });
        }
    }


    private void trainModelWithTensorflow() {
        initialiseTensorFlow();

        if (webPageObj != null && webPageObj.isReady()) {
            webPageObj.trainNewModel(key);
        }
        else {
            // very unlikely to happen, but there is maybe a tiny race condition where it is possible?
            displayErrorMessage("Not ready yet. Please try again in a moment");
        }
    }

    private void checkTensorFlowJsStatus() {
        if (webPageObj == null) {
            GotStatus(0, "Not trained");
        }
        else {
            switch (webPageObj.getModelStatus()) {
                case "Not trained":
                case "Failed":
                    GotStatus(0, webPageObj.getModelStatus());
                    break;
                case "Available":
                    GotStatus(2, webPageObj.getModelStatus());
                    break;
                case "Training":
                    GotStatus(1, webPageObj.getModelProgress() + "%");
                    break;
            }
        }
    }


    private void classifyImageWithTensorflow(final String path) {
        initialiseTensorFlow();

        if (!webPageObj.getModelStatus().equals("Available")) {
            displayErrorMessage(EXPLAIN_ML_MODEL_NEEDED);
            return;
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
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
        webViewSettings.setDomStorageEnabled(true);

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



    // ---------------------------------------------------------------------
    // COORDINATE TAKING ACTIONS THAT ARE IMPACTED BY THE PROJECT TYPE
    // ---------------------------------------------------------------------

    /**
     * Check the type of project before taking the requested action.
     *
     *  The project type is read from an in-memory cache if it's already
     *   known, otherwise an API call to ML4K servers is made first before
     *   taking the requested action.
     *
     * @param nextAction  Action that should be taken once project type has been verified
     */
    private void checkProjectType(final NextAction nextAction) {
        if (isApiKeyMissing()) {
            processNextAction("unknown", nextAction);
        }
        else if (scratchKeyTypes.containsKey(key)) {
            processNextAction(scratchKeyTypes.get(key), nextAction);
        }
        else {
            runInBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        ML4K ml4k = new ML4K(key);
                        ModelStatus status = ml4k.getModelStatus();
                        String type = status.getProjectType();
                        scratchKeyTypes.put(key, type);

                        processNextAction(type, nextAction);
                    }
                    catch (ML4KException exc) {
                        if (exc.getMessage().equals("Scratch key not found"))
                        {
                            scratchKeyTypes.put(key, "expired");
                            processNextAction("expired", nextAction);
                        }
                        else if (exc.getMessage().equals("API key isn't a Machine Learning for Kids key"))
                        {
                            scratchKeyTypes.put(key, "invalid");
                            processNextAction("invalid", nextAction);
                        }
                        else {
                            processNextAction("unknown", nextAction);
                        }
                    }
                }
            });
        }
    }

    // take the requested action
    private void processNextAction(final String projectType, final NextAction action) {
        Log.d(LOGPREFIX, "processNextAction " + action.step.name() + " for " + projectType);

        // startup actions
        if (action.step == NextStep.Init) {
            if (projectType.equals("imgtfjs")) {
                initialiseTensorFlow();
            }
            return;
        }

        // if there is a problem with the API key, it
        //  doesn't matter what the next step is, the
        //  response is the same: display a message
        if ("invalid".equals(projectType)) {
            displayErrorMessage(EXPLAIN_API_KEY_INVALID);
            return;
        }
        if ("expired".equals(projectType)) {
            displayErrorMessage(EXPLAIN_API_KEY_EXPIRED);
            return;
        }
        if ("unknown".equals(projectType)) {
            displayErrorMessage(EXPLAIN_API_KEY_NEEDED);
            return;
        }

        // otherwise...
        switch (action.step) {
            // training data actions
            case AddTextTraining:
                switch (projectType) {
                    case "text":
                        addTrainingDataToProject(action);
                        break;
                    case "images":
                    case "imgtfjs":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_IMAGES);
                        break;
                    case "numbers":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_NUMBERS);
                        break;
                    case "sounds":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            case AddImageTraining:
                switch (projectType) {
                    case "images":
                    case "imgtfjs":
                        addTrainingDataToProject(action);
                        break;
                    case "text":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_TEXT);
                        break;
                    case "numbers":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_NUMBERS);
                        break;
                    case "sounds":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            case AddNumbersTraining:
                switch (projectType) {
                    case "numbers":
                        addTrainingDataToProject(action);
                        break;
                    case "images":
                    case "imgtfjs":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_IMAGES);
                        break;
                    case "text":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_TEXT);
                        break;
                    case "sounds":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            // classification actions
            case ClassifyText:
                switch (projectType) {
                    case "text":
                        classifyTestDataOnServer(action);
                        break;
                    case "images":
                    case "imgtfjs":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_IMAGES);
                        break;
                    case "numbers":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_NUMBERS);
                        break;
                    case "sounds":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            case ClassifyImage:
                switch (projectType) {
                    case "images":
                        classifyTestDataOnServer(action);
                        break;
                    case "imgtfjs":
                        classifyImageWithTensorflow(((ClassifyImageAction)action).imagePath);
                        break;
                    case "text":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_TEXT);
                        break;
                    case "numbers":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_NUMBERS);
                        break;
                    case "sounds":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            case ClassifyNumbers:
                switch (projectType) {
                    case "numbers":
                        classifyTestDataOnServer(action);
                        break;
                    case "images":
                    case "imgtfjs":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_IMAGES);
                        break;
                    case "text":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_TEXT);
                        break;
                    case "sounds":
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            // ML model actions
            case TrainModel:
                switch (projectType) {
                    case "images":
                    case "text":
                    case "numbers":
                        trainModelWithWatson();
                        break;
                    case "imgtfjs":
                        trainModelWithTensorflow();
                        break;
                    default:
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
            case CheckModelStatus:
                switch (projectType) {
                    case "images":
                    case "text":
                    case "numbers":
                        fetchModelStatusFromServer();
                        break;
                    case "imgtfjs":
                        checkTensorFlowJsStatus();
                        break;
                    default:
                        displayErrorMessage(EXPLAIN_API_KEY_TYPE_SOUNDS);
                        break;
                }
                break;
        }
    }






    // --------------------------------------------------------------------
    // DEFINES ACTIONS THAT ARE CARRIED OUT DIFFERENTLY DEPENDING ON THE
    //  TYPE OF ML PROJECT (e.g. text, images, etc.)
    //
    //  This means these actions can only be carried out after fetching the
    //   type of project.
    // --------------------------------------------------------------------

    private enum NextStep {
        // init local ML model - only relevant for project types
        //  where models are hosted on-device
        Init,
        // add training data to the project
        AddTextTraining,
        AddImageTraining,
        AddNumbersTraining,
        // classify test data using a model
        ClassifyText,
        ClassifyImage,
        ClassifyNumbers,
        // train a new ML model
        TrainModel,
        // check the current status of the ML model
        CheckModelStatus;
    }
    private class NextAction {
        private NextStep step;
        private NextAction(NextStep step) {
            this.step = step;
        }
    }
    private class ClassifyImageAction extends NextAction {
        private String imagePath;
        private ClassifyImageAction(String path) {
            super(NextStep.ClassifyImage);
            this.imagePath = path;
        }
    }
    private class ClassifyNumbersAction extends NextAction {
        private YailList numbers;
        private ClassifyNumbersAction(YailList nums) {
            super(NextStep.ClassifyNumbers);
            this.numbers = nums;
        }
    }
    private class ClassifyTextAction extends NextAction {
        private String text;
        private ClassifyTextAction(String textData) {
            super(NextStep.ClassifyText);
            this.text = textData;
        }
    }
    private class TextTrainingAction extends NextAction {
        private String text;
        private String label;
        private TextTrainingAction(String label, String textData) {
            super(NextStep.AddTextTraining);
            this.text = textData;
            this.label = label;
        }
    }
    private class ImageTrainingAction extends NextAction {
        private String path;
        private String label;
        private ImageTrainingAction(String label, String imagePath) {
            super(NextStep.AddImageTraining);
            this.path = imagePath;
            this.label = label;
        }
    }
    private class NumbersTrainingAction extends NextAction {
        private YailList numbers;
        private String label;
        private NumbersTrainingAction(String label, YailList nums) {
            super(NextStep.AddNumbersTraining);
            this.label = label;
            this.numbers = nums;
        }
    }
}
