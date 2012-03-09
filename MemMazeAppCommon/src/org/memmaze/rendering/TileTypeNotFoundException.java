package org.memmaze.rendering;

public class TileTypeNotFoundException extends RuntimeException {

	public TileTypeNotFoundException(String tileType, String tileSet, String resourceName) {
		super("TileType '" + tileType + "' not found in tileset '" + tileSet + "' (" + resourceName + ")");
	}

	private static final long serialVersionUID = 1L;

}
