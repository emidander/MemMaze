package org.memmaze.rendering.util;

public class TileSizeCalculator {

  private final int largestAllowedTileSize;
  private final int MARGIN = 1;

  public TileSizeCalculator(int largestAllowedTileSize) {
    this.largestAllowedTileSize = largestAllowedTileSize;
  }

  public int calculateTileSize(int width, int height, int tileColumns, int tileRows) {
    int largestPossibleTileSizeWidth = width / (tileColumns + MARGIN);
    int largestPossibleTileSizeHeight = height / (tileRows + MARGIN);

    int largestPossibleTileSize = Math.min(largestPossibleTileSizeWidth, largestPossibleTileSizeHeight);

    return Math.min(largestPossibleTileSize, largestAllowedTileSize);
  }

}
