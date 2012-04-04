package org.memmaze.maze;

import org.json.JSONObject;

public class MalformedLevelDataException extends Exception {
  private static final long serialVersionUID = 1L;

  public MalformedLevelDataException(String description, JSONObject levelData) {
    super(description + "\n\n" + levelData.toString());
  }

}
