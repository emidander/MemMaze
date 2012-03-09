package org.memmaze;

import org.memmaze.maze.LevelResults;
import org.memmaze.maze.LevelTree;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SelectLevelActivity extends ListActivity {

	private Typeface font;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		font = Typeface.createFromAsset(getAssets(), "zeroes-one.ttf");

		ListView listview = getListView();
		listview.setTextFilterEnabled(true);

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getBaseContext(), MemMazeActivity.class);
    	        intent.putExtra(MemMazeActivity.INTENT_EXTRA_LEVELNUMBER, (int)id);
				startActivity(intent);
			}
		});

	}

	@Override
	protected void onResume() {		
		MemMazeApplication memMazeApplication = (MemMazeApplication)this.getApplication();
		LevelTree levelTree = memMazeApplication.getLevelTree();
		LevelResults levelResults = memMazeApplication.getLevelResults();
		
		LevelAdapter levelListAdapter = (LevelAdapter) getListAdapter();
		if (levelListAdapter == null) {
			levelListAdapter = new LevelAdapter(this, R.layout.select_level_item, levelTree, levelResults, font);
			setListAdapter(levelListAdapter);
		} else {
			levelListAdapter.refreshResults(levelResults);
		}
		super.onResume();
	}
	
}
