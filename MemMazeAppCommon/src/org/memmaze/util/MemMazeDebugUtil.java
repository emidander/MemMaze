package org.memmaze.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class MemMazeDebugUtil {

	public static boolean appIsInDebugMode(Context context) {
		boolean debuggableFlagSet = false;
	    PackageManager pm = context.getPackageManager();
	    try
	    {
	        ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
	        debuggableFlagSet = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
	    }
	    catch(NameNotFoundException e)
	    {
	    	debuggableFlagSet = false;
	    }
		return debuggableFlagSet;
	}

}
