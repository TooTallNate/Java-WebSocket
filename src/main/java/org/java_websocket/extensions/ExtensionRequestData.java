package org.java_websocket.extensions;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExtensionRequestData {

  public static String EMPTY_VALUE = "";

  private Map<String, String> extensionParameters;
  private String extensionName;

  private ExtensionRequestData() {
    extensionParameters = new LinkedHashMap<String, String>();
  }

  public static ExtensionRequestData parseExtensionRequest(String extensionRequest) {
    ExtensionRequestData extensionData = new ExtensionRequestData();
    String[] parts = extensionRequest.split(";");
    extensionData.extensionName = parts[0].trim();

    for (int i = 1; i < parts.length; i++) {
      String[] keyValue = parts[i].split("=");
      String value = EMPTY_VALUE;

      // Some parameters don't take a value. For those that do, parse the value.
      if (keyValue.length > 1) {
        String tempValue = keyValue[1].trim();

        // If the value is wrapped in quotes, just get the data between them.
        if ((tempValue.startsWith("\"") && tempValue.endsWith("\""))
            || (tempValue.startsWith("'") && tempValue.endsWith("'"))
            && tempValue.length() > 2) {
          tempValue = tempValue.substring(1, tempValue.length() - 1);
        }

        value = tempValue;
      }

      extensionData.extensionParameters.put(keyValue[0].trim(), value);
    }

    return extensionData;
  }

  public String getExtensionName() {
    return extensionName;
  }

  public Map<String, String> getExtensionParameters() {
    return extensionParameters;
  }
}
