package org.memmaze.maze;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Level {

	public static final String TILETYPE_START_UP = "UP";
	public static final String TILETYPE_START_RIGHT = "RIGHT";
	public static final String TILETYPE_START_DOWN = "DOWN";
	public static final String TILETYPE_START_LEFT = "LEFT";
	private static final String DEFAULT_LEVEL_TEXT = "Remember, remember, remember...";
	private static final String DEFAULT_TILESET = "circles_yellow";
	private static final long DEFAULT_MEMORIZE_TIME = 3000;
	private final int rowCount;
	private final int columnCount;
	private Tile[][] tiles;
	private final String levelId;
	private final String levelText;
	private final String tileSet;
	private final long memorizeTime;
	private final boolean debugOnly;
	private final boolean premiumOnly;
	private int startTileRow;
	private int startTileCol;

	public Level(String levelJSON) throws JSONException, MalformedLevelDataException {
		this(new JSONObject(levelJSON));
	}

	public Level(JSONObject levelData) throws JSONException, MalformedLevelDataException {
		
		levelId = levelData.getString("level-id");
		rowCount = levelData.getInt("rows");
		columnCount = levelData.getInt("columns");
		levelText = levelData.optString("level-text", DEFAULT_LEVEL_TEXT);
		tileSet = levelData.optString("tileset", DEFAULT_TILESET);
		memorizeTime = levelData.optLong("memorize-time", DEFAULT_MEMORIZE_TIME);
		debugOnly = levelData.optBoolean("debug-only", false);
		premiumOnly = levelData.optBoolean("premium-only", false);
		
		tiles = new Tile[rowCount][columnCount];
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				tiles[row][column] = new Tile();
			}			
		}
		
		Tile goalTile = null;
		Tile startTile = null;
		JSONArray griddata = levelData.getJSONArray("griddata");

		if (griddata.length() != rowCount) throw new MalformedLevelDataException("Wrong number of rows", levelData);

		for (int row = 0; row < griddata.length(); row++) {
			String rowdata = griddata.getString(row).toLowerCase();	
			if (rowdata.length() != columnCount) throw new MalformedLevelDataException("Wrong length in row " + row, levelData);
			
			for (int col = 0; col < rowdata.length(); col++) {
				char tileChar = rowdata.charAt(col);
				switch (tileChar) {
				case '.':
				case ' ':
					// SPACE
					break;
				case 'g':
					// GOAL
					goalTile = tiles[row][col];
					goalTile.isGoal = true;
					break;
				case 's':
				case '^':
				case '>':
				case '<':
				case 'v':
					// START
					startTile = tiles[row][col];
					startTile.isStart = true;
					startTile.tileType = getTileTypeForStartTile(tileChar);
					startTileRow = row;
					startTileCol = col;
					break;
				case 'x':
					// block
					tiles[row][col].isBlock = true;
				}
			}
		}
		if (goalTile == null) throw new MalformedLevelDataException("No goal tile found in level.", levelData);
		if (startTile == null) throw new MalformedLevelDataException("No start tile found in level.", levelData);
		
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				Tile tile = tiles[row][column];
				if (tile.isBlock) {
					tile.tileType = getTileType(row, column);
				}
			}
		}

	}

	private String getTileTypeForStartTile(char tileChar) {
		switch (tileChar) {
		case '^':
			return TILETYPE_START_UP;
		case '>':
			return TILETYPE_START_RIGHT;
		case 'v':
			return TILETYPE_START_DOWN;
		case '<':
			return TILETYPE_START_LEFT;
		}
		return TILETYPE_START_UP;
	}

	private String getTileType(int row, int column) {
		if (isEmpty(row, column)) return "";
		
		boolean hasNeighbourN = isBlock(row - 1, column);
		boolean hasNeighbourNE = isBlock(row - 1, column + 1);
		boolean hasNeighbourE = isBlock(row, column + 1);
		boolean hasNeighbourSE = isBlock(row + 1, column + 1);
		boolean hasNeighbourS = isBlock(row + 1, column);
		boolean hasNeighbourSW = isBlock(row + 1, column - 1);
		boolean hasNeighbourW = isBlock(row, column - 1);
		boolean hasNeighbourNW = isBlock(row - 1, column - 1);
		
		return getTileTypeString(hasNeighbourN, hasNeighbourNE, hasNeighbourE, hasNeighbourSE, hasNeighbourS, hasNeighbourSW, hasNeighbourW, hasNeighbourNW);
	}

	private boolean outsideGrid(int row, int column) {
		return row < 0 || row >= rowCount || column < 0 || column >= columnCount;
	}

	private boolean isBlock(int row, int column) {
		if (outsideGrid(row, column)) return true;
		return tiles[row][column].isBlock;
	}

	// empty = inside grid and not block, start or goal
	private boolean isEmpty(int row, int column) {
		if (outsideGrid(row, column)) return false;
		return !tiles[row][column].isBlock && !tiles[row][column].isStart && !tiles[row][column].isGoal;
	}

	private static String getTileTypeString(boolean hasNeighbourN, boolean hasNeighbourNE, boolean hasNeighbourE, boolean hasNeighbourSE, boolean hasNeighbourS, boolean hasNeighbourSW, boolean hasNeighbourW, boolean hasNeighbourNW) {
		return (hasNeighbourN ? "1" : "0") + 
								(hasNeighbourNE ? "1" : "0") + 
								(hasNeighbourE ? "1" : "0") + 
								(hasNeighbourSE ? "1" : "0") + 
								(hasNeighbourS ? "1" : "0") + 
								(hasNeighbourSW ? "1" : "0") + 
								(hasNeighbourW ? "1" : "0") + 
								(hasNeighbourNW ? "1" : "0");
	}


	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public Tile getTile(int row, int column) {
		if (outsideGrid(row, column)) {
			Tile tile = new Tile();
			tile.isBlock = true;
			tile.tileType = getTileType(row, column);
			return tile;
		}
		return tiles[row][column];
	}

	public String getLevelText() {
		return levelText;
	}
	
	public String getTileSet() {
		return tileSet;
	}

	public long getMemorizeTime() {
		return memorizeTime;
	}

	public String getLevelId() {
		return levelId;
	}

	public boolean isDebugOnly() {
		return debugOnly;
	}

	@Override
	public String toString() {
		return String.format("Level with id %s", levelId);
	}

	public int countTouchableTiles() {
		int touchables = 0;
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				if (!tiles[row][column].isBlock) {
					touchables++;
				}
			}
		}
		return touchables;
	}

	public boolean isPremiumOnly() {
		return premiumOnly;
	}

	public int getStartTileRow() {
		return startTileRow;
	}

	public int getStartTileCol() {
		return startTileCol;
	}

}
