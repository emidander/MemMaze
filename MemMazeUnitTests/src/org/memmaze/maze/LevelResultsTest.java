package org.memmaze.maze;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class LevelResultsTest extends TestCase {

	public void testShouldPersistHighscores() {
		LevelResults levelResults = new LevelResults();
		levelResults.setLevelResult("1", 39.0);
		levelResults.setLevelResult("2", 44.0);
		assertEquals(39.0, levelResults.getLevelResult("1"));
		assertEquals(44.0, levelResults.getLevelResult("2"));
	}

	public void testShouldReturnZeroAsResultForLevelsWithoutHighscore() throws Exception {
		LevelResults levelResults = new LevelResults();
		assertEquals(0.0, levelResults.getLevelResult("1"));
		assertEquals(0.0, levelResults.getLevelResult("2"));
	}

	public void testShouldPersistToAndFromJSON() throws Exception {
		
		LevelResults levelResults = new LevelResults();
		levelResults.setLevelResult("1", 39);
		levelResults.setLevelResult("5", 44);

		String levelResultsData = levelResults.getDataString();
		LevelResults levelResultsDeserialized = new LevelResults(new ByteArrayInputStream(levelResultsData.getBytes()));
		assertEquals(39.0, levelResultsDeserialized.getLevelResult("1"));
		assertEquals(44.0, levelResultsDeserialized.getLevelResult("5"));
	}

	public void testNewResultShouldOnlyBeSetIfItIsHigherThanTheExisting() {
		LevelResults levelResults = new LevelResults();
		
		levelResults.setLevelResult("1", 45);
		assertEquals(45.0, levelResults.getLevelResult("1"));
		
		levelResults.setLevelResult("1", 12);
		assertEquals(45.0, levelResults.getLevelResult("1"));
		
		levelResults.setLevelResult("1", 87);
		assertEquals(87.0, levelResults.getLevelResult("1"));
	}
}
