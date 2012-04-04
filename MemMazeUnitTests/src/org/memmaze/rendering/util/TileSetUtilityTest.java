package org.memmaze.rendering.util;

import org.memmaze.rendering.util.TileSetUtility;

import junit.framework.TestCase;

public class TileSetUtilityTest extends TestCase {

  public void testShouldConvertTileTypeStringToTileIndex() {

    assertEquals(0, TileSetUtility.getTileTypeIndex("00000000"));
    assertEquals(0, TileSetUtility.getTileTypeIndex("00000001"));

    assertEquals(1, TileSetUtility.getTileTypeIndex("00000010"));
    assertEquals(1, TileSetUtility.getTileTypeIndex("00000011"));

    assertEquals(0, TileSetUtility.getTileTypeIndex("00000100"));

    assertEquals(46, TileSetUtility.getTileTypeIndex("11111111"));

  }

}
