package org.memmaze;

import org.memmaze.maze.Level;
import org.memmaze.maze.LevelResults;
import org.memmaze.util.MemMazeDebugUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuActivity extends Activity {

	// Create an anonymous implementation of OnClickListener
    private View.OnClickListener sStartButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getBaseContext(), SelectLevelActivity.class);
            startActivity(intent);
        }
    };

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        Typeface font = Typeface.createFromAsset(getAssets(), "zeroes-one.ttf");  

        TextView txt1 = (TextView) findViewById(R.id.TextLogo1);  
        txt1.setTypeface(font); 
        TextView txt2 = (TextView) findViewById(R.id.TextLogo2);  
        txt2.setTypeface(font);

        MemMazeApplication application = (MemMazeApplication) getApplication();
        TextView txtFree = (TextView) findViewById(R.id.TextFreeVersion); 
        if (application.isPremiumVersion()) {
        	txtFree.setVisibility(View.GONE);
        }

        Button startButton = (Button) findViewById(R.id.ButtonStartGame);
        startButton.setTypeface(font);
        startButton.setOnClickListener(sStartButtonListener);

    }
    
    private static final int DEBUG_ITEM_ID_CLEAR_STATE = 1;
    private static final int DEBUG_ITEM_ID_ENABLE_ALL_LEVELS = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (MemMazeDebugUtil.appIsInDebugMode(this)) {
			menu.addSubMenu(1, DEBUG_ITEM_ID_CLEAR_STATE, 0, "DEBUG: Clear state");
			menu.addSubMenu(1, DEBUG_ITEM_ID_ENABLE_ALL_LEVELS, 0, "DEBUG: Enable all levels");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        MemMazeApplication application = (MemMazeApplication) getApplication();
	    switch (item.getItemId()) {
	    case DEBUG_ITEM_ID_CLEAR_STATE:
	        application.clearLevelResults();
	        return true;
	    case DEBUG_ITEM_ID_ENABLE_ALL_LEVELS:
	        LevelResults levelResults = application.getLevelResults();
	        for (Level level : application.getLevelTree().getAllLevels()) {
		        levelResults.setLevelResult(level.getLevelId(), 1);
	        }
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	
}