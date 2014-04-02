package com.autoupdate.framework;


public interface IDownload {
	void  onPreDownload(String... str);
	void  onUpdateDownload(Integer... progress);// currentProgress, max
	void  onPostDownload(boolean result);
}
