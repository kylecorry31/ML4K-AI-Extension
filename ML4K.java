package com.kylecorry.ml4k;

import com.google.appinventor.components.runtime.*;
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

import android.app.Activity;

import java.net.URLEncoder;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
  description = "This provides an interface for the Machine Learning for Kids website.",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/extension.png")
@SimpleObject(external=true)
@UsesPermissions(permissionNames = "android.permission.INTERNET") // might need library for json
@UsesLibraries(libraries = "gson.jar")
public final class ML4K extends AndroidNonvisibleComponent {

  private final Activity activity;

  private String key = "";
  private final String baseURL = "https://machinelearningforkids.co.uk/api/scratch/";
  private final String endpointURL = "/classify?data=";

  public ML4K(ComponentContainer container) {
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

  @SimpleFunction(description = "Get the classification for the text.")
  public void ClassifyText(final String data) {
    final String urlStr = generateURL(data);
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {

        try {

          // Pull from URL
          URL url = new URL(urlStr);
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setReadTimeout(10000);
          conn.setConnectTimeout(10000);
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Content-Type", "application/json");
          conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");

          Scanner scanner = new Scanner(conn.getInputStream());
          String json = scanner.next();
          conn.disconnect();

          // Parsing
          JsonElement jsonElement = new JsonParser().parse(json);
          JsonArray jsonArray = jsonElement.getAsJsonArray();
          JsonObject value = jsonArray.get(0).getAsJsonObject();

          final String className = value.get("class_name").getAsString();
          final double confidence = value.get("confidence").getAsDouble();



          // Dispatch the event.
          activity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
              GotClassification(data, className, confidence);
            }
          });
        } catch(Exception e){
          e.printStackTrace();
          // TODO: handle error properly
          activity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
              GotClassification(data, "ERROR", 0);
            }
          });
        }

      }
    });
  }

  /**
   * Event indicating that a classification has finished.
   *
   * @param data The data
   * @param classification The classification
   * @param confidence The confidence of the classification
   */
  @SimpleEvent
  public void GotClassification(String data, String classification, double confidence) {
    // invoke the application's "GotClassification" event handler.
    EventDispatcher.dispatchEvent(this, "GotClassification", data, classification, confidence);
  }

  private String generateURL(String data){
    try {
      return baseURL + key + endpointURL + URLEncoder.encode(data, "UTF-8");
    } catch (Exception e){
      e.printStackTrace();
      return "";
    }
  }

}
