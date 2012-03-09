package org.memmaze.resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.json.JSONException;
import org.memmaze.R;
import org.memmaze.maze.LevelTree;
import org.memmaze.maze.MalformedLevelDataException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ResourceUtility {

    private static final String LOG_TAG_RESOURCE_UTILITY = "ResourceUtility";
	private static BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

	public static Bitmap loadBitmap(Resources resources, String resourceName) throws NotFoundException {

		if (Log.isLoggable(LOG_TAG_RESOURCE_UTILITY, Log.DEBUG)) {
			Log.d(LOG_TAG_RESOURCE_UTILITY, String.format("Loading resource with name %s.", resourceName));
		}
		int resourceId = getDrawableResourceIdentifierFromOrgMemmaze(resourceName);
		
        Bitmap bitmap = null;
        InputStream is = resources.openRawResource(resourceId);
        try {
            bitmap = BitmapFactory.decodeStream(is, null, bitmapOptions);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }
        return bitmap;
	}

	private static int getDrawableResourceIdentifierFromOrgMemmaze(String resourceName) {
		final Class resourceType = org.memmaze.R.drawable.class;
		Field field;
		try {
			field = resourceType.getField(resourceName);
			return field.getInt(null);
		} catch (Exception e) {
			return 0;
		}
	}

	public static LevelTree getLevelTree(Context context, boolean loadDebugLevels, boolean loadPremiumLevels) throws ResourceLoadException {
		LevelTree levelTree = null;
		InputStream levelResourceStream = null;
		try {
			levelResourceStream = context.getResources().openRawResource(R.raw.level_data);
			levelTree = new LevelTree(levelResourceStream, loadDebugLevels, loadPremiumLevels);
		} catch (JSONException e) {
			throw new ResourceLoadException("Leveltree", e);
		} catch (IOException e) {
			throw new ResourceLoadException("Leveltree", e);
		} catch (MalformedLevelDataException e) {
			throw new ResourceLoadException("Leveltree", e);
		} finally {
            try {
                levelResourceStream.close();
            } catch (IOException e) {
                // Ignore.
            }
		}
		return levelTree;
	}

}
