package org.memmaze.maze;

public class LevelNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	private final String levelId;
	private final int levelNumber;

	public LevelNotFoundException(String levelId) {
		super(String.format("Level with id %s not found.", levelId));
		this.levelId = levelId;
		this.levelNumber = -1;
	}

	public LevelNotFoundException(int levelNumber) {
		super(String.format("Level with level number %d not found.", levelNumber));
		this.levelId = "";
		this.levelNumber = levelNumber;
	}

	public String getLevelNumber() {
		return levelId;
	}
	
}
