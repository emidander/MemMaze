package org.memmaze.rendering.util;

import org.memmaze.rendering.util.TileSizeCalculator;

import junit.framework.TestCase;

public class TileSizeCalculatorTest extends TestCase {

	private TileSizeCalculator tileSizeCalculator;
	private final static int TILE_SIZE = 50;
	
	@Override
	protected void setUp() throws Exception {
		tileSizeCalculator = new TileSizeCalculator(TILE_SIZE);
	}

	public void testCalculateSizeBasedOnMarginOfOneRowAndOneColumn() {		
		assertEquals(9, tileSizeCalculator.calculateTileSize(100, 100, 10, 10));	// 100 / (10 + 1) == 9	=> 9
	}

	public void testShouldSelectTheLargestAllowedSizeIfOptimalSizeIsHuge() {
		// Select the largest allowed size if optimal size is huge
		assertEquals(TILE_SIZE, tileSizeCalculator.calculateTileSize(1000, 1000, 10, 10));
	}

	public void testShouldBaseCalculationsOnTheSmallestDimension() {		
		// Base calculations on the smallest of width and height
		assertEquals(5, tileSizeCalculator.calculateTileSize(110, 55, 10, 10));		// 55 / 11 == 5		=> 5
		assertEquals(5, tileSizeCalculator.calculateTileSize(55, 110, 10, 10));		// 55 / 11 == 5		=> 5
	}

}
