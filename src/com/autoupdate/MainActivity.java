package com.autoupdate;

import java.io.File;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.autoupdate.framework.AutoUpgrade;
import com.autoupdate.framework.IDownload;

public class MainActivity extends Activity {

	private static final int NOTIFY_UPGRADE_ID = 1;
	private AutoUpgrade autoUpgrade;
	private NotificationManager notifyMgr;
	private Notification notifi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		notifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		//实例化自动升级服务
		autoUpgrade = new AutoUpgrade(this);
		autoUpgrade.setDownload(new IDownload() {
			
			/**
			 * 只有在真正检查到需要更新时，才会调用这个函数
			 */
			@Override
			public void onPreDownload(String... str) {
				Toast.makeText(MainActivity.this, "Before  Download", Toast.LENGTH_SHORT).show();
				
				new AlertDialog.Builder(MainActivity.this).setTitle("升级 v" + autoUpgrade.getUo().getVersionCode())
				.setMessage(autoUpgrade.getUo().getFeatures())
				.setPositiveButton("升级", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						autoUpgrade.startTask();//启动下载apk任务
						
						//状态通知栏提示
						notifi = new Notification(R.drawable.ic_launcher, "升级", System.currentTimeMillis());
						notifi.contentView = new RemoteViews(getPackageName(), R.layout.notify_upgrade);
						String filePath = autoUpgrade.getUo().getApkUrl().trim();
						String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
						notifi.contentView.setTextViewText(R.id.textView1,  fileName);
						Intent intent = new Intent();
						notifi.contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
						notifi.flags |= Notification.FLAG_AUTO_CANCEL;
						notifyMgr.notify(NOTIFY_UPGRADE_ID , notifi);
					}
				})
				.setNegativeButton("取消", null).create().show();
			}
			

			@Override
			public void onUpdateDownload(Integer... progress) {
				int loaded = progress[0].intValue();
				int length = progress[1].intValue();
				notifi.contentView.setProgressBar(R.id.progressBar1, length, loaded, false);
				String filePath = autoUpgrade.getUo().getApkUrl().trim();
				String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
				notifi.contentView.setTextViewText(R.id.textView1, fileName +" "+loaded * 100 / length + "%");
				notifyMgr.notify(NOTIFY_UPGRADE_ID, notifi);
			}

			@Override
			public void onPostDownload(boolean result) {
				if(result){
					Toast.makeText(MainActivity.this, "Download suceess.", Toast.LENGTH_LONG).show();
					notifi.contentView.setViewVisibility(R.id.progressBar1, View.INVISIBLE);
					String filePath = autoUpgrade.getUo().getApkUrl().trim();
					String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					notifi.contentView.setTextViewText(R.id.textView1, fileName+"下载完成，点击开始安装");
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri uri = Uri.fromFile(new File(autoUpgrade.getSavePath()));
					intent.setDataAndType(uri, "application/vnd.android.package-archive");
					notifi.contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
					notifyMgr.notify(NOTIFY_UPGRADE_ID, notifi);
					if(autoUpgrade.isAutoInstall()){
//						notifyMgr.cancel(NOTIFY_UPGRADE_ID);
//						startActivity(intent);
					}else{
						Toast.makeText(MainActivity.this, "新版本下载完成，请点击通知安装", Toast.LENGTH_LONG).show();
					}
				}else{
					Toast.makeText(MainActivity.this, "Download  failed", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		autoUpgrade.setUo(new UpdateObject(this));
		autoUpgrade.setSavePath(Environment.getExternalStorageDirectory()+"/download/tmp.apk");
		autoUpgrade.setAutoInstall(false);
	}
	
	
	public void startDownloading(View view){
		autoUpgrade.start();
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
