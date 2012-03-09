package org.memmaze.maze;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LevelTest extends TestCase {

	public static String getTestLevelJSON(String levelId) throws Exception {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("level-id", levelId);

		jsonObject.put("columns", 5);
		jsonObject.put("rows", 6);

		jsonObject.put("start-column", 2);
		jsonObject.put("start-row", 4);

		jsonObject.put("goal-column", 2);
		jsonObject.put("goal-row", 0);

		jsonObject.put("level-text", "testtext");
		jsonObject.put("tileset", "testset");
		jsonObject.put("memorize-time", 2000);

		JSONArray griddata = new JSONArray();
		griddata.put("..g..");
		griddata.put(".....");
		griddata.put("XX...");
		griddata.put(".....");
		griddata.put("..s..");
		griddata.put(".....");
		
		jsonObject.put("griddata", griddata);

		return jsonObject.toString();
	}
	
	public void testShouldInitializeFromJSON() throws Exception {
		Level level = new Level(getTestLevelJSON("test-1"));
		assertEquals(5, level.getColumnCount());
		assertEquals(6, level.getRowCount());
		
		assertTrue(level.getTile(2, 0).isBlock);
		assertTrue(level.getTile(2, 1).isBlock);
		
		assertTrue(level.getTile(4, 2).isStart);
		assertTrue(level.getTile(0, 2).isGoal);

		assertEquals("test-1", level.getLevelId());
		assertEquals("testtext", level.getLevelText());
		assertEquals("testset", level.getTileSet());
		assertEquals(2000, level.getMemorizeTime());
	}

	public static final String JSON_WITH_ALL_TILE_TYPES = "{'level-id': 'test-1', 'rows':9,'columns':9," + 
						"'griddata':[" + 
						"'........g'," + 
						"'....X....'," + 
						"'..XXXX...'," + 
						"'.XXXXX...'," + 
						"'..XXXXXX.'," + 
						"'..XXXX...'," + 
						"'...X.....'," + 
						"'...X.X...'," + 
						"'........s'," + 
						"]}".replaceAll("'", "\"");
	//   012345678
	//  ___________
	//0 |         |
	//1 |    X    |
	//2 |  XXXX   |
	//3 | XXXXX � |
	//4 |  XXXXXX |
	//5 |  XXXX   |
	//6 |   X     |
	//7 |   X X   |
	//8 |         |
	//  -----------
	
	public void testShouldGenerateTileTypeForTiles() throws Exception {
		Level level = new Level(JSON_WITH_ALL_TILE_TYPES);

		assertEquals("00011100", level.getTile(1, 4).tileType);

		assertEquals("00111100", level.getTile(2, 2).tileType);
		assertEquals("01111110", level.getTile(2, 3).tileType);
		assertEquals("10111110", level.getTile(2, 4).tileType);
		assertEquals("00001111", level.getTile(2, 5).tileType);

		assertEquals("01110000", level.getTile(3, 1).tileType);
		assertEquals("11111010", level.getTile(3, 2).tileType);
		assertEquals("11111111", level.getTile(3, 3).tileType);
		assertEquals("11111111", level.getTile(3, 4).tileType);
		assertEquals("10011111", level.getTile(3, 5).tileType);

		assertEquals("11111001", level.getTile(4, 2).tileType);
		assertEquals("11111111", level.getTile(4, 3).tileType);
		assertEquals("11111111", level.getTile(4, 4).tileType);
		assertEquals("10101111", level.getTile(4, 5).tileType);
		assertEquals("00100111", level.getTile(4, 6).tileType);
		assertEquals("00000010", level.getTile(4, 7).tileType);

		assertEquals("11110000", level.getTile(5, 2).tileType);
		assertEquals("11101011", level.getTile(5, 3).tileType);
		assertEquals("11100111", level.getTile(5, 4).tileType);
		assertEquals("11000011", level.getTile(5, 5).tileType);

		assertEquals("11001001", level.getTile(6, 3).tileType);

		assertEquals("10000000", level.getTile(7, 3).tileType);
		assertEquals("00000000", level.getTile(7, 5).tileType);

	}

	private static final String JSON_WITH_EDGE_TILES = "{'level-id': 'test-1', 'rows':3,'columns':3," + 
							"'griddata':[" + 
							"'.Xg'," + 
							"'XXX'," + 
							"'.Xs'" + 
							"]}".replaceAll("'", "\"");

	//   012
	//  _____
	//0 | Xg|
	//1 |XXX|
	//2 | Xs|
	//  -----

	public void testShouldGenerateProperTileTypeForEdgeTiles() throws Exception {
		Level level = new Level(JSON_WITH_EDGE_TILES);

		assertEquals("11011101", level.getTile(0, 1).tileType);

		assertEquals("01110111", level.getTile(1, 0).tileType);
		assertEquals("10101010", level.getTile(1, 1).tileType);
		assertEquals("01110111", level.getTile(1, 2).tileType);

		assertEquals("11011101", level.getTile(2, 1).tileType);

	}	


	public void testShouldGenerateTileTypeForTilesOutsideGrid() throws Exception {
		Level level = new Level(JSON_WITH_EDGE_TILES);

		assertEquals("11111111", level.getTile(-2, -2).tileType);
		assertEquals("11101111", level.getTile(-1, -1).tileType);

		assertEquals("11011111", level.getTile(0, -1).tileType);
		assertEquals("10101111", level.getTile(1, -1).tileType);

		assertEquals("11110111", level.getTile(-1, 0).tileType);
		assertEquals("11101011", level.getTile(-1, 1).tileType);

		assertEquals("11111110", level.getTile(3, 3).tileType);
		assertEquals("11111111", level.getTile(4, 4).tileType);
	}	

	public void testShouldGenerateEmtpyTileTypeForNonBlockTilesInsideGrid() throws Exception {
		Level level = new Level(JSON_WITH_EDGE_TILES);

		assertEquals("", level.getTile(0, 0).tileType);
		assertEquals("", level.getTile(2, 0).tileType); 
	}	

	private static final String JSON_WITH_START_UP = "{'level-id': 'test-1', 'rows':1,'columns':3," + 
		"'griddata':[" + 
		"'^.g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITH_START_RIGHT = "{'level-id': 'test-2', 'rows':1,'columns':3," + 
		"'griddata':[" + 
		"'>.g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITH_START_DOWN = "{'level-id': 'test-3', 'rows':1,'columns':3," + 
		"'griddata':[" + 
		"'v.g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITH_START_LEFT = "{'level-id': 'test-4', 'rows':1,'columns':3," + 
		"'griddata':[" + 
		"'<.g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITH_UNSPECIFIED_START_DIRECTION = "{'level-id': 'test-5', 'rows':1,'columns':3," + 
		"'griddata':[" + 
		"'s.g'," + 
		"]}".replaceAll("'", "\"");

	public void testShouldHandleStartInDifferentDirections() throws Exception {
		Level levelUp = new Level(JSON_WITH_START_UP);
		Level levelRight = new Level(JSON_WITH_START_RIGHT);
		Level levelDown = new Level(JSON_WITH_START_DOWN);
		Level levelLeft = new Level(JSON_WITH_START_LEFT);
		Level levelUnspecified = new Level(JSON_WITH_UNSPECIFIED_START_DIRECTION);
		
		assertTrue(levelUp.getTile(0, 0).isStart);
		assertTrue(levelRight.getTile(0, 0).isStart);
		assertTrue(levelDown.getTile(0, 0).isStart);
		assertTrue(levelLeft.getTile(0, 0).isStart);
		assertTrue(levelUnspecified.getTile(0, 0).isStart);

		assertEquals(Level.TILETYPE_START_UP, levelUp.getTile(0, 0).tileType);
		assertEquals(Level.TILETYPE_START_RIGHT, levelRight.getTile(0, 0).tileType);
		assertEquals(Level.TILETYPE_START_DOWN, levelDown.getTile(0, 0).tileType);
		assertEquals(Level.TILETYPE_START_LEFT, levelLeft.getTile(0, 0).tileType);
		assertEquals(Level.TILETYPE_START_UP, levelUnspecified.getTile(0, 0).tileType);
	
	}

	private static final String JSON_WITHOUT_START = "{'level-id': 'test-1', 'rows':3,'columns':3," + 
		"'griddata':[" + 
		"'..g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITHOUT_GOAL = "{'level-id': 'test-1', 'rows':3,'columns':3," + 
		"'griddata':[" + 
		"'^..'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITH_TOO_FEW_ROWS = "{'level-id': 'test-1', 'rows':2,'columns':3," + 
		"'griddata':[" + 
		"'sg.'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_WITH_TOO_FEW_COLUMNS = "{'level-id': 'test-1', 'rows':1,'columns':3," + 
		"'griddata':[" + 
		"'sg'," + 
		"]}".replaceAll("'", "\"");

	public void testLevelMustHaveStartTile() throws JSONException {
		try {
			Level level = new Level(JSON_WITHOUT_START);
			assertTrue(false);
		} catch (MalformedLevelDataException e) {
			assertTrue(true);
		}
	}

	public void testLevelMustHaveGoalTile() throws JSONException {
		try {
			Level level = new Level(JSON_WITHOUT_GOAL);
			assertTrue(false);
		} catch (MalformedLevelDataException e) {
			assertTrue(true);
		}
	}

	public void testLevelMustHaveAsManyColumnsAsSpecified() throws JSONException {
		try {
			Level level = new Level(JSON_WITH_TOO_FEW_COLUMNS);
			assertTrue(false);
		} catch (MalformedLevelDataException e) {
			assertTrue(true);
		}
	}

	public void testLevelMustHaveAsManyRowsAsSpecified() throws JSONException {
		try {
			Level level = new Level(JSON_WITH_TOO_FEW_ROWS);
			assertTrue(false);
		} catch (MalformedLevelDataException e) {
			assertTrue(true);
		}
	}

	private static final String JSON_SIMPLE = "{'level-id': 'test-1', 'rows':2,'columns':2," + 
		"'griddata':[" + 
		"'s.'," + 
		"'.g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_SIMPLE_DEBUG = "{'level-id': 'test-1', 'rows':2,'columns':2," + 
		"'debug-only': true," + 
		"'griddata':[" + 
		"'s.'," + 
		"'.g'," + 
		"]}".replaceAll("'", "\"");
	private static final String JSON_SIMPLE_PREMIUM = "{'level-id': 'test-1', 'rows':2,'columns':2," + 
		"'premium-only': true," + 
		"'griddata':[" + 
		"'s.'," + 
		"'.g'," + 
		"]}".replaceAll("'", "\"");

	public void testDefaultValues() throws Exception {
		Level levelAllDefaults = new Level(JSON_SIMPLE);
		assertFalse(levelAllDefaults.isDebugOnly());
		assertFalse(levelAllDefaults.isPremiumOnly());
		Level levelDebug = new Level(JSON_SIMPLE_DEBUG);
		assertTrue(levelDebug.isDebugOnly());
		Level levelPremium = new Level(JSON_SIMPLE_PREMIUM);
		assertTrue(levelPremium.isPremiumOnly());
	}
}
