package org.memmaze.rendering;

import java.util.HashMap;
import java.util.Map;

import org.memmaze.maze.Level;
import org.memmaze.maze.Tile;
import org.memmaze.rendering.util.TileSetUtility;
import org.memmaze.resource.ResourceUtility;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;

public class TileSet {

	private static final String BITMAP_ID_START_N = "start_n";
	private static final String BITMAP_ID_START_E = "start_e";
	private static final String BITMAP_ID_START_S = "start_s";
	private static final String BITMAP_ID_START_W = "start_w";
	private static final String BITMAP_ID_GOAL = "goal";
	private static final String BITMAP_ID_BLOCKS = "blocks";
	
	private static final String TILESET_RESOURCE_FORMAT = "tileset_%s_%d_%s";
	
	private Map<String, Bitmap> bitmapCache;
	private final String tileSetName;
	private final static String LOG_TAG_TILESET = "TileSet";
	private final int actualTileSize;
	private final int resourceTileSize;
	private final Resources resources;

	public TileSet(Resources resources, String tileSetName, int actualTileSize, int resourceTileSize) {
		this.resources = resources;
		this.tileSetName = tileSetName;
		this.actualTileSize = actualTileSize;
		this.resourceTileSize = resourceTileSize;
		bitmapCache = new HashMap<String, Bitmap>();
	}

	private Bitmap loadBitmap(String tileType, boolean scaleToActualTileSize) {
		
		String resourceName = String.format(TILESET_RESOURCE_FORMAT, tileSetName, resourceTileSize, tileType);

		if (bitmapCache.containsKey(tileType)) {
			return bitmapCache.get(tileType);			
		}

		Bitmap bitmap;
		try {
			bitmap = ResourceUtility.loadBitmap(resources, resourceName);
		}
		catch (NotFoundException e) {
			throw new TileTypeNotFoundException(tileType, tileSetName, resourceName);
		}

		if (scaleToActualTileSize) {
			bitmap = scaleBitmapToActualTileSize(bitmap);
		}
		
		bitmapCache.put(tileType, bitmap);
		
		return bitmap;
	}

	public Bitmap getBitmapForTile(Tile tile) {
		if (tile.isBlock) {
			return getBitmapForBlock(tile.tileType);
		} else if (tile.isStart) {
			String bitmapId = BITMAP_ID_START_N;
			if (tile.tileType.equals(Level.TILETYPE_START_UP)) {
				bitmapId = BITMAP_ID_START_N;
			} else if (tile.tileType.equals(Level.TILETYPE_START_RIGHT)) {
				bitmapId = BITMAP_ID_START_E;
			} else if (tile.tileType.equals(Level.TILETYPE_START_DOWN)) {
				bitmapId = BITMAP_ID_START_S;
			} else if (tile.tileType.equals(Level.TILETYPE_START_LEFT)) {
				bitmapId = BITMAP_ID_START_W;
			}
			return loadBitmap(bitmapId, true);
		} else if (tile.isGoal) {
			return loadBitmap(BITMAP_ID_GOAL, true);
		}
		return null;
	}

	private Bitmap getBitmapForBlock(String tileType) {
		
		if (bitmapCache.containsKey(tileType)) {
			return bitmapCache.get(tileType);
		}
		
		Bitmap blocksBitmap = loadBitmap(BITMAP_ID_BLOCKS, false);
		Bitmap bitmapOriginal = Bitmap.createBitmap(blocksBitmap, resourceTileSize * TileSetUtility.getTileTypeIndex(tileType), 0, resourceTileSize, resourceTileSize);
		Bitmap bitmapScaled = scaleBitmapToActualTileSize(bitmapOriginal);
		
		bitmapCache.put(tileType, bitmapScaled);

		return bitmapScaled;
	}

	private Bitmap scaleBitmapToActualTileSize(Bitmap bitmapOriginal) {
		return Bitmap.createScaledBitmap(bitmapOriginal, actualTileSize, actualTileSize, true);
	}

}
