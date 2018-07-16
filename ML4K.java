package com.kylecorry.ml4k;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
  description = "This provides an interface for the Machine Learning for Kids website.",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/externalComponent.png")
@SimpleObject(external=true)
public final class ML4K extends AndroidNonvisibleComponent {
  private String key = "";

  public ML4K(ComponentContainer container) {
    super(container.$form());
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
  public String ClassifyText(String data) {
    return data;
  }

}
