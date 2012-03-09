package org.memmaze.maze;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.memmaze.rendering.util.FileUtil;

public class LevelTree {

	private SortedMap<Integer, Level> levels;
	private Map<String, Integer> levelIdToLevelNumberIndex;

	private LevelTree(JSONObject levelTreeData, boolean loadDebugLevels, boolean loadPremiumLevels)  throws JSONException, IOException, MalformedLevelDataException {
		levels = new TreeMap<Integer, Level>();
		levelIdToLevelNumberIndex = new HashMap<String, Integer>();
		
		JSONArray levelsArray = levelTreeData.getJSONArray("levels");
		Integer levelNumber = 0;
		for (int levelIndex = 0; levelIndex < levelsArray.length(); levelIndex++) {
			JSONObject levelData = levelsArray.getJSONObject(levelIndex);
			Level level = new Level(levelData);
			if ((!level.isDebugOnly() || loadDebugLevels) &&
					(!level.isPremiumOnly() || loadPremiumLevels)) {
				levelNumber++;
				levels.put(levelNumber, level);
				
				if (levelIdToLevelNumberIndex.containsKey(level.getLevelId())) {
					throw new MalformedLevelDataException(String.format("Level with id '%s' found twice.", level.getLevelId()), levelData);
				}
				levelIdToLevelNumberIndex.put(level.getLevelId(), levelNumber);
			}
		}
	}

	public LevelTree(String jsonData, boolean loadDebugLevels, boolean loadPremiumLevels) throws JSONException, IOException, MalformedLevelDataException {
		this(new JSONObject(jsonData), loadDebugLevels, loadPremiumLevels);
	}
	
	public LevelTree(InputStream inputStream, boolean loadDebugLevels, boolean loadPremiumLevels) throws JSONException, IOException, MalformedLevelDataException {
		this(new JSONObject(FileUtil.readStream(inputStream)), loadDebugLevels, loadPremiumLevels);
	}
	
	public Level getLevel(String levelId) throws LevelNotFoundException {
		if (!levelIdToLevelNumberIndex.containsKey(levelId)) throw new LevelNotFoundException(levelId);
		Integer levelNumberKey = levelIdToLevelNumberIndex.get(levelId);
		return levels.get(levelNumberKey);
	}

	public Level getLevel(int levelNumber) throws LevelNotFoundException {
		if (!levels.containsKey(levelNumber)) throw new LevelNotFoundException(levelNumber);
		return levels.get(levelNumber);
	}

	public int getLevelCount() {
		return levels.size();
	}

	public boolean isLastLevel(String levelId) {
		return levelIdToLevelNumberIndex.get(levelId) == levels.size();
	}
	
	public Collection<Level> getAllLevels() {
		return levels.values();
	}

	public String getNextLevelId(String levelId) {
		Integer nextLevelNumber = levelIdToLevelNumberIndex.get(levelId) + 1;
		if (nextLevelNumber > levels.size()) {
			return "";
		}
		return levels.get(nextLevelNumber).getLevelId();
	}

	public String getFirstLevelId() {
		return levels.get(1).getLevelId();
	}

	public int getLevelNumber(String levelId) {
		return levelIdToLevelNumberIndex.get(levelId);
	}

	public String getPreviousLevelId(String levelId) {
		Integer previousLevelNumber = levelIdToLevelNumberIndex.get(levelId) - 1;
		if (previousLevelNumber < 1) {
			return "";
		}
		return levels.get(previousLevelNumber).getLevelId();
	}
}
