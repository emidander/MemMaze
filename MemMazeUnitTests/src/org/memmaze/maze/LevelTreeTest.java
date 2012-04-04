package org.memmaze.maze;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class LevelTreeTest extends TestCase {

  public void testLevelTreeShouldLoadAllLevelsOnInit() throws Exception {

    String jsonLevel1 = LevelTest.getTestLevelJSON("test-1");
    String jsonLevel2 = LevelTest.getTestLevelJSON("test-2");

    JSONArray levelsData = new JSONArray();
    levelsData.put(new JSONObject(jsonLevel1));
    levelsData.put(new JSONObject(jsonLevel2));

    JSONObject levelData = new JSONObject();
    levelData.put("levels", levelsData);

    LevelTree levelTree = new LevelTree(new ByteArrayInputStream(levelData.toString().getBytes()), false, false);

    assertEquals(2, levelTree.getLevelCount());
  }

  public void testLevelTreeShouldOnlyLoadDebugLevelsIfDebugIsEnabled() throws Exception {

    LevelTree levelTreeDebug = new LevelTree(this.getClass().getResourceAsStream("sample_level_tree_1.json"), true, false);
    boolean debugLevelFound = false;
    for (Level level : levelTreeDebug.getAllLevels()) {
      if (level.isDebugOnly()) {
        debugLevelFound = true;
      }
    }
    if (!debugLevelFound)
      throw new Exception("Debug level not found.");

    LevelTree levelTreeNoDebug = new LevelTree(this.getClass().getResourceAsStream("sample_level_tree_1.json"), false, false);
    for (Level level : levelTreeNoDebug.getAllLevels()) {
      if (level.isDebugOnly()) {
        throw new Exception("Debug level found.");
      }
    }

    assertTrue(levelTreeNoDebug.getLevelCount() < levelTreeDebug.getLevelCount());
  }

  public void testLevelTreeShouldOnlyLoadPremiumLevelsIfPremiumIsEnabled() throws Exception {

    LevelTree levelTreePremium = new LevelTree(this.getClass().getResourceAsStream("sample_level_tree_1.json"), false, true);
    boolean premiumLevelFound = false;
    for (Level level : levelTreePremium.getAllLevels()) {
      if (level.isPremiumOnly()) {
        premiumLevelFound = true;
      }
    }
    if (!premiumLevelFound)
      throw new Exception("Premium level not found.");

    LevelTree levelTreeNoPremium = new LevelTree(this.getClass().getResourceAsStream("sample_level_tree_1.json"), false, false);
    for (Level level : levelTreeNoPremium.getAllLevels()) {
      if (level.isPremiumOnly()) {
        throw new Exception("Premium level found.");
      }
    }

    assertTrue(levelTreeNoPremium.getLevelCount() < levelTreePremium.getLevelCount());
  }

  public void testLevelTreeShouldSetSequentialLevelNumbers() throws Exception {

    LevelTree levelTree = new LevelTree(this.getClass().getResourceAsStream("sample_level_tree_1.json"), false, true);
    assertEquals(levelTree.getLevel(1), levelTree.getLevel("normal"));
    assertEquals(levelTree.getLevel(2), levelTree.getLevel("premium-only"));
  }
}
