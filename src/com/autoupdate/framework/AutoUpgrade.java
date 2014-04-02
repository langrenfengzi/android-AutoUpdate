package com.autoupdate.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.autoupdate.R;
import com.autoupdate.util.HttpUtils;
import com.autoupdate.util.PackageUtils;

public final class AutoUpgrade{
	private static final String DEFAULT_SAVE_PATH = Environment.getExternalStorageDirectory() + "/tmp/newfile.apk";
	private static final int DOWNLOAD_NOT = 0;
	private static final int DOWNLOAD_ING = 1;
	private static final int DOWNLOAD_OVER = 2;
	private static  int downloadingStatus = DOWNLOAD_NOT; 
	private static final String TAG;
	private Context context;
	private String jsonPath;
	private IUpdateObject  uo;
	private IDownload download;


	private String savePath = DEFAULT_SAVE_PATH;
	private boolean autoInstall = false;
	public boolean isAutoInstall() {
		return autoInstall;
	}

	public void setAutoInstall(boolean autoInstall) {
		this.autoInstall = autoInstall;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getJsonPath() {
		return jsonPath;
	}

	public void setJsonPath(String jsonPath) {
		this.jsonPath = jsonPath;
	}

	public IUpdateObject getUo() {
		return uo;
	}

	public void setUo(IUpdateObject uo) {
		this.uo = uo;
	}

	
	//获取类名作为TAG名
	static {
		TAG=new Object(){
			public String getClassName(){
				String inClassName = this.getClass().getName();
				return inClassName.substring(0, inClassName.lastIndexOf('$'));
			}
		}.getClassName();
	}
	public IDownload getDownload() {
		return download;
	}
	
	public void setDownload(IDownload download) {
		this.download = download;
	}
	
	public AutoUpgrade(Context context){
		this.context = context;
		try{
			this.jsonPath = context.getResources().getString(R.string.default_url);
			Log.d(TAG, "Get from string resources, jsonPath: " +jsonPath);
		}catch(NotFoundException e){
			Log.e(TAG, "Can not find resource id of default_url in the res/values/strings.xml");
		}
	}
	

	public AutoUpgrade(Context context, String jsonPath){
		this.context = context;
		this.jsonPath = jsonPath;
	}
	

	
	/**
	 * 检查是否需要更新以及用户确认
	 */
	public void start(){
		//每次从新获取服务器端版本号，异步地
		new QueryUpdateTask().execute();
	}
	
	/**
	 * 真正启动下载apk任务
	 */
	public void startTask(){
		if (downloadingStatus != DOWNLOAD_ING) {
			new DownloadTask().execute(uo.getApkUrl(), savePath);
		}
	}
	
	
	/**
	 * 
	 * @author Lang
	 * 查询服务器apk版本任务
	 */
	private class QueryUpdateTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected Void doInBackground(Void... params) {
			JSONObject jsonObject = HttpUtils.getJSONObj(AutoUpgrade.this.jsonPath);
			uo.parseJSONObject(jsonObject);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			int currVersion = PackageUtils.getCurrVersionCode(context);
			if(uo!=null && uo.isInitial() && currVersion < uo.getVersionCode()){
				download.onPreDownload(uo.getFeatures());
			}else{
				Toast.makeText(context, "当前已是最新版本，无需更新", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	
	/**
	 * 
	 * @author Lang
	 * 下载APK任务
	 */
	private class DownloadTask extends AsyncTask<String, Integer, Boolean>{
		@Override
		protected void onPreExecute() {
			downloadingStatus = DOWNLOAD_ING;
		}
		@Override
		protected Boolean doInBackground(String... params) {
			String apk_url = params[0];
			String apk_save = params[1];
			InputStream is = null;
			FileOutputStream  fos = null;
			boolean downloadSucceed = false;
			try {
				File tmpFile = new File(apk_save);
				if(!tmpFile.exists()){
					tmpFile.getParentFile().mkdirs();
				}
				fos = new FileOutputStream(tmpFile);
				URL url = new URL(apk_url);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
					//服务端存在问题
					Log.w(TAG, "Access " + apk_url +" failed. Code:"+conn.getResponseCode());
					return downloadSucceed;
				}else{
					is = conn.getInputStream();
					byte[] buffer = new byte[1024];
					int len = 0;
					int loaded = 0;
					int i = 0;
					int length = uo.getApkSize();
					while((len=is.read(buffer)) != -1){
						fos.write(buffer, 0, len);
						loaded += len;
						//更新频率
						int progress = loaded * 100 / length;
						if(progress >= 5*i){
							++i;
							publishProgress(loaded, length);//进度、总大小
						}
					}
					fos.flush();
					downloadSucceed = true;
					Log.d(TAG, "Succeed to save to " + apk_save);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
					try {
						if(is!=null){
							is.close();
						}
						if(fos!=null){
							fos.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			return downloadSucceed;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(download!=null){
				download.onUpdateDownload(values);
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			downloadingStatus = DOWNLOAD_OVER;
			Log.d(TAG, "Download over.");
			if(download!=null){
				download.onPostDownload(result);
			}
			// Auto Reinstall
			if (autoInstall) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(new File(savePath));
				intent.setDataAndType(uri,"application/vnd.android.package-archive");
				context.startActivity(intent);
			}
		}
		
	}

}
