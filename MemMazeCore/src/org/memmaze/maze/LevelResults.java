package org.memmaze.maze;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.memmaze.rendering.util.FileUtil;

public class LevelResults {

  private static final String JSON_LEVEL_RESULTS = "level-results";
  private static final String JSON_LEVEL_ID = "level-id";
  private static final String JSON_LEVEL_RESULT = "level-result";

  private final Map<String, Double> levelResults;

  public LevelResults(InputStream inputStream) throws MemMazePersistenceException {
    try {
      JSONObject levelResultsData = new JSONObject(FileUtil.readStream(inputStream));

      JSONArray levelResultsArray = levelResultsData.getJSONArray(JSON_LEVEL_RESULTS);
      levelResults = new HashMap<String, Double>(levelResultsArray.length());

      for (int ix = 0; ix < levelResultsArray.length(); ix++) {
        JSONObject levelResult = levelResultsArray.getJSONObject(ix);
        setLevelResult(levelResult.getString(JSON_LEVEL_ID), levelResult.getDouble(JSON_LEVEL_RESULT));
      }
    } catch (JSONException e) {
      throw new MemMazePersistenceException("Could not read level results from string.", e);
    } catch (IOException e) {
      throw new MemMazePersistenceException("Could not read level results file.", e);
    }

  }

  public LevelResults() {
    levelResults = new HashMap<String, Double>();
  }

  public double getLevelResult(String levelId) {
    if (!levelResults.containsKey(levelId)) {
      return 0;
    }
    return levelResults.get(levelId).doubleValue();
  }

  public void setLevelResult(String levelId, double levelResult) {
    double currentResult = getLevelResult(levelId);
    if (levelResult > currentResult) {
      levelResults.put(levelId, new Double(levelResult));
    }
  }

  public String getDataString() throws MemMazePersistenceException {
    try {
      JSONArray levelResultsArray = new JSONArray();
      for (String levelId : levelResults.keySet()) {
        JSONObject levelResult = new JSONObject();
        levelResult.put(JSON_LEVEL_ID, levelId);
        levelResult.put(JSON_LEVEL_RESULT, levelResults.get(levelId).doubleValue());
        levelResultsArray.put(levelResult);
      }

      JSONObject levelResultsData = new JSONObject();
      levelResultsData.put(JSON_LEVEL_RESULTS, levelResultsArray);
      return levelResultsData.toString();
    } catch (JSONException e) {
      throw new MemMazePersistenceException("Could not write level results to string.", e);
    }
  }

  public boolean resultExistsFor(String levelId) {
    return levelResults.containsKey(levelId);
  }

}
