package org.memmaze;

import org.memmaze.maze.LevelNotFoundException;
import org.memmaze.maze.LevelTree;
import org.memmaze.rendering.canvas.MemMazeGameView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MemMazeActivity extends Activity {

	public static final String INTENT_EXTRA_LEVELNUMBER = "org.memmaze.levelnumber";
	public static final String INTENT_EXTRA_SUCCESS = "org.memmaze.success";

	public static final int GAME_THREAD_MSG_LEVEL_DONE = 1;
	private static final String LOG_TAG_MEMMAZE_ACTIVITY = "MemMazeActivity";
	private String levelId;
	
	private MemMazeGameView memMazeGameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MemMazeApplication memMazeApplication = (MemMazeApplication)this.getApplication();
		LevelTree levelTree = memMazeApplication.getLevelTree();

		if (getIntent().getExtras() == null) {
			levelId = levelTree.getFirstLevelId();
		} else {
			int levelNumber = getIntent().getExtras().getInt(INTENT_EXTRA_LEVELNUMBER);
			try {
				levelId = levelTree.getLevel(levelNumber).getLevelId();
			} catch (LevelNotFoundException e) {
				Log.e(LOG_TAG_MEMMAZE_ACTIVITY, String.format("Failed to load level number %d.", levelNumber), e);
				levelId = levelTree.getFirstLevelId();
			}
		}

    	try {
			memMazeGameView = new MemMazeGameView(this.getBaseContext(), memMazeApplication, levelId);
		} catch (LevelNotFoundException e) {
			Log.e(LOG_TAG_MEMMAZE_ACTIVITY, "Failed to load level.", e);
            Intent intent = new Intent(getBaseContext(), SelectLevelActivity.class);
            startActivity(intent);
		}
		setContentView(memMazeGameView);
	}

    @Override
    protected void onPause() {
        super.onPause();
        if (memMazeGameView != null) {
        	memMazeGameView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (memMazeGameView != null) {
        	memMazeGameView.onResume();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		memMazeGameView.stopGameThread();
		memMazeGameView = null;
	}

}
