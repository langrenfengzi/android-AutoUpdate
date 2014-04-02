package com.autoupdate;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import com.autoupdate.R;
import com.autoupdate.framework.IUpdateObject;

public class UpdateObject implements IUpdateObject{
	private Context context;
	private int versionCode;
	private String features;
	private String apkUrl;
	private int   apkSize;
	private boolean isInit;
	

	public  UpdateObject(Context context){
		this.context = context;
		this.versionCode = -1;
		this.features = "empty";
		this.apkUrl = "none";
		this.apkSize = 0;
		
		this.isInit= false;//尚未初始化
	}
	
	@Override
	public int getVersionCode() {
		return versionCode;
	}
	
	@Override
	public String getFeatures() {
		return features;
	}
	
	@Override
	public String getApkUrl() {
		return apkUrl;
	}
	
	@Override
	public int getApkSize() {
		return apkSize;
	}

	
	@Override
	public boolean isInitial() {
		return this.isInit;
	}

	@Override
	public void parseJSONObject(JSONObject jsonObject) {
		if(jsonObject==null){
			return;
		}
		Resources resources = context.getResources();
		try {
			versionCode = jsonObject.getInt(resources.getString(R.string.version_code));
			features =	jsonObject.getString(resources.getString(R.string.features));
			apkUrl  =	jsonObject.getString(resources.getString(R.string.apk_url));
			apkSize =	jsonObject.getInt( resources.getString(R.string.apk_size));
			this.isInit = true;
		} catch (NotFoundException notFoundEx) {
			notFoundEx.printStackTrace();
		} catch (JSONException jsonEx) {
			jsonEx.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
}
