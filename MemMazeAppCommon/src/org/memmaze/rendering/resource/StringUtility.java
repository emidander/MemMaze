package org.memmaze.rendering.resource;

import org.memmaze.R;

import android.content.res.Resources;

public class StringUtility {

	private final Resources resources;

	public StringUtility(Resources resources) {
		this.resources = resources;
	}
	
	public String getLevelCompletionText(double coverage) {
			return String.format("%.0f%%", coverage * 100);
	}
	
	public String getLevelScoreText(double coverage, boolean highscore) {
		if (highscore) {
			return String.format(resources.getString(R.string.highscore), coverage * 100);
		} else {
			return String.format(resources.getString(R.string.score), coverage * 100);
		}
	}

	public String getLevelName(int levelNumber) {
		return String.format(resources.getString(R.string.level_name), levelNumber);
	}

	public String getReplayLevelText(int levelNumber) {
		return String.format(resources.getString(R.string.replay_level_text), levelNumber);
	}

}
