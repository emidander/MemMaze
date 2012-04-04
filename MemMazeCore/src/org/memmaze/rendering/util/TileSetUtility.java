package org.memmaze.rendering.util;

public class TileSetUtility {

  private static final int[] tileSetActualRef = new int[256];
  private static final int[] tileSetIndexRef = new int[256];

  static {

    boolean indexesSeen[] = new boolean[256];

    int ref_index = -1;
    for (int neighbour_N = 0; neighbour_N <= 1; neighbour_N++) {
      for (int neighbour_NE = 0; neighbour_NE <= 1; neighbour_NE++) {
        for (int neighbour_E = 0; neighbour_E <= 1; neighbour_E++) {
          for (int neighbour_SE = 0; neighbour_SE <= 1; neighbour_SE++) {
            for (int neighbour_S = 0; neighbour_S <= 1; neighbour_S++) {
              for (int neighbour_SW = 0; neighbour_SW <= 1; neighbour_SW++) {
                for (int neighbour_W = 0; neighbour_W <= 1; neighbour_W++) {
                  for (int neighbour_NW = 0; neighbour_NW <= 1; neighbour_NW++) {

                    boolean accept_N = (neighbour_N == 1);
                    boolean accept_NE = (neighbour_NE == 1 && neighbour_N == 1 && neighbour_E == 1);
                    boolean accept_E = (neighbour_E == 1);
                    boolean accept_SE = (neighbour_SE == 1 && neighbour_S == 1 && neighbour_E == 1);
                    boolean accept_S = (neighbour_S == 1);
                    boolean accept_SW = (neighbour_SW == 1 && neighbour_S == 1 && neighbour_W == 1);
                    boolean accept_W = (neighbour_W == 1);
                    boolean accept_NW = (neighbour_NW == 1 && neighbour_N == 1 && neighbour_W == 1);

                    int tile_index = neighbour_N * 128 + neighbour_NE * 64 + neighbour_E * 32 + neighbour_SE * 16 + neighbour_S * 8 + neighbour_SW * 4 + neighbour_W * 2 + neighbour_NW;
                    int actual_index = (accept_N ? 128 : 0) + (accept_NE ? 64 : 0) + (accept_E ? 32 : 0) + (accept_SE ? 16 : 0) + (accept_S ? 8 : 0) + (accept_SW ? 4 : 0) + (accept_W ? 2 : 0) + (accept_NW ? 1 : 0);
                    tileSetActualRef[tile_index] = actual_index;

                    if (indexesSeen[actual_index]) {
                      tileSetIndexRef[tile_index] = tileSetIndexRef[actual_index];
                    } else {
                      ref_index++;
                      tileSetIndexRef[tile_index] = ref_index;
                      indexesSeen[actual_index] = true;
                    }

                  }
                }
              }
            }
          }
        }
      }
    }

  }

  public static int getTileTypeIndex(String tileType) {
    return tileSetIndexRef[Integer.parseInt(tileType, 2)];
  }

  public static int getTileTypeActualIndex(String tileType) {
    return tileSetActualRef[Integer.parseInt(tileType, 2)];
  }

}
