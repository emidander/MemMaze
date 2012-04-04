package org.memmaze;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.memmaze.maze.Level;
import org.memmaze.maze.LevelTree;

public class ValidateCurrentLevelTree extends TestCase {

  public void testLoadAndParseCurrentLevelTree() throws Exception {

    new LevelTree(new FileInputStream("/Users/erikmidander/Dropbox/projekt/MemoryMaze/MemMazeAppCommon/res/raw/level_data.json"), true, true);
    assertTrue(true);
  }

  public void testNavigateToFinishOnAllLevels() throws Exception {
    LevelTree levelTree = new LevelTree(new FileInputStream("/Users/erikmidander/Dropbox/projekt/MemoryMaze/MemMazeAppCommon/res/raw/level_data.json"), true, true);
    for (Level level : levelTree.getAllLevels()) {
      boolean[][] tilesVisited = new boolean[level.getRowCount()][level.getColumnCount()];
      assertTrue(String.format("Could not navigate to finish on level with id '%s'", level.getLevelId()), navigateLevel(level, level.getStartTileRow(), level.getStartTileCol(), tilesVisited));
    }
  }

  private boolean navigateLevel(Level level, int row, int col, boolean[][] tilesVisited) {
    if (level.getTile(row, col).isGoal) {
      return true;
    }

    tilesVisited[row][col] = true;

    for (int nextRow = row - 1; nextRow <= row + 1; nextRow++) {
      for (int nextCol = col - 1; nextCol <= col + 1; nextCol++) {
        if (nextRow >= 0 && nextRow < level.getRowCount() && nextCol >= 0 && nextCol < level.getColumnCount()) {
          if (!tilesVisited[nextRow][nextCol] && !level.getTile(nextRow, nextCol).isBlock) {
            boolean goalFound = navigateLevel(level, nextRow, nextCol, tilesVisited);
            if (goalFound) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

}
