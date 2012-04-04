package org.memmaze;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.memmaze.maze.LevelResults;
import org.memmaze.maze.LevelTree;
import org.memmaze.maze.MemMazePersistenceException;
import org.memmaze.resource.ResourceUtility;
import org.memmaze.util.MemMazeDebugUtil;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MemMazeApplication extends Application {

  private static final String FILE_LEVEL_RESULTS = "level-results";
  private static final String LOG_TAG_MEMMAZE_APPLICATION = "MemMazeApplication";
  private LevelTree levelTree = null;
  private LevelResults levelResults = null;

  public LevelTree getLevelTree() {
    if (levelTree == null) {
      levelTree = ResourceUtility.getLevelTree(this, MemMazeDebugUtil.appIsInDebugMode(this), isPremiumVersion());
    }
    return levelTree;
  }

  public LevelResults getLevelResults() {
    if (levelResults == null) {
      FileInputStream fis;
      try {
        fis = openFileInput(FILE_LEVEL_RESULTS);
        levelResults = new LevelResults(fis);
      } catch (FileNotFoundException e) {
        // No file is ok, it will be created when a highscore is registread.
        levelResults = new LevelResults();
      } catch (MemMazePersistenceException e) {
        Log.e(LOG_TAG_MEMMAZE_APPLICATION, "Could not read from level results file", e);
        levelResults = new LevelResults();
      }
    }
    return levelResults;
  }

  public void setLevelResults(LevelResults newLevelResults) {
    this.levelResults = newLevelResults;

    FileOutputStream fos = null;
    try {
      fos = openFileOutput(FILE_LEVEL_RESULTS, Context.MODE_PRIVATE);
      fos.write(levelResults.getDataString().getBytes());
    } catch (IOException e) {
      Log.e(LOG_TAG_MEMMAZE_APPLICATION, "Could not write to level results file.", e);
      return;
    } catch (MemMazePersistenceException e) {
      Log.e(LOG_TAG_MEMMAZE_APPLICATION, "Could not persist level results.", e);
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        ;
      }
    }
  }

  public void clearLevelResults() {
    setLevelResults(new LevelResults());
  }

  public boolean isPremiumVersion() {
    return this.getPackageName().equals("org.memmaze.premium");
  }
}
