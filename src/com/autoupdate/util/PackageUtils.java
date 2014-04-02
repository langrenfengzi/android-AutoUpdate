package com.autoupdate.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public final class PackageUtils {
	private PackageUtils(){}
	
	public static int getCurrVersionCode(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo  packInfo = null;
		int version = 0;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			version = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}
}
