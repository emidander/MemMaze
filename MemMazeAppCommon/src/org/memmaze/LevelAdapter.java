package org.memmaze;

import org.memmaze.maze.Level;
import org.memmaze.maze.LevelNotFoundException;
import org.memmaze.maze.LevelResults;
import org.memmaze.maze.LevelTree;
import org.memmaze.rendering.resource.StringUtility;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {

	private static final String LOG_TAG_LEVEL_ADAPTER = "LevelAdapter";
	private static final int COLOR_DISABLED_LEVEL = Color.rgb(64, 64, 64);
	private SelectLevelActivity activity;
	private LevelTree levelTree;
	private Typeface font;
	private LevelResults levelResults;
	
	public LevelAdapter(SelectLevelActivity selectLevelActivity, int selectLevelItem, LevelTree levelTree, LevelResults levelResults, Typeface font) {
		this.activity = selectLevelActivity;
		this.levelTree = levelTree;
		this.levelResults = levelResults;
		this.font = font;
	}

	@Override
	public int getCount() {
		return levelTree.getLevelCount();
	}

	@Override
	public Object getItem(int position) {
		return getLevelForPosition(position);
	}

	@Override
	public long getItemId(int position) {
		return getLevelNumberForPosition(position);
	}

	private Level getLevelForPosition(int position) {
		try {
			return levelTree.getLevel(getLevelNumberForPosition(position));
		} catch (LevelNotFoundException e) {
			Log.e(LOG_TAG_LEVEL_ADAPTER, String.format("Level not found for position %d.", position));
			return null;
		}
	}

	private int getLevelNumberForPosition(int position) {
		return position + 1;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = LayoutInflater.from(activity);
		View levelItemView = inflater.inflate(R.layout.select_level_item, parent, false);

		TextView levelName = (TextView) levelItemView.findViewById(R.id.TextLevelName);
		TextView levelCompletion = (TextView) levelItemView.findViewById(R.id.TextLevelCompletion);
		
		levelName.setTypeface(font);
		levelCompletion.setTypeface(font);
		
		StringUtility stringUtility = new StringUtility(activity.getResources());
		Level level = getLevelForPosition(position);
		levelName.setText(stringUtility.getLevelName(getLevelNumberForPosition(position)));
		if (isEnabled(position)) {
			double levelResult = levelResults.getLevelResult(level.getLevelId());
			if (levelResult > 0) {
				levelCompletion.setText(stringUtility.getLevelCompletionText(levelResult));
			} else {
				levelCompletion.setText("");				
			}
		} else {
			levelName.setTextColor(COLOR_DISABLED_LEVEL);
			levelCompletion.setText("");
		}
		
		return levelItemView;
	}

	@Override
	public boolean isEnabled(int position) {
		if (getLevelNumberForPosition(position) == 1) {
			return true;		// First level is always available
		}
		String currentLevelId = getLevelForPosition(position).getLevelId();
		if (levelResults.resultExistsFor(currentLevelId)) {
			return true;
		}
		String previousLevelId = levelTree.getPreviousLevelId(currentLevelId);
		if (levelResults.resultExistsFor(previousLevelId)) {
			return true;
		}
		return false;
	}

	public void refreshResults(LevelResults newLevelResults) {
		this.levelResults = newLevelResults;
		notifyDataSetChanged();
	}

}
