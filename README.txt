android-AutoUpdate
==================

Android自动升级模块

主要模块在com.autoupdate.framework和com.autoupdate.util中，
可以直接使用上述部分。
自己覆写Activity类，实现IDownload和IUpdateObject接口，传递给AutoUpgrade使用。

接口IUpdateObject描述了一个json属性，包含版本号、更新描述、apk下载地址及大小，
因为是从JSON数据解析得到的，所以需要实现一个parseJSONObject方法。
详见实现该接口的UpdateObject类。

接口IDownload描述了下载前、下载中间及下载后的操作。
MainActivity类中实现该接口分别为在下载前生成AlertDialog提示用户选择，
下载中间生成通知实时更新下载进度以及下载完成后询问用户是否需要立刻安装。

资源strings.xml中，default_url定义了服务器上存放json的地址。
version_code，features,apk_url, apk_size分别定义了json中的key，必须存在。
可以和json协商更改key名，或另外扩展。
json示例：
{
"versioncode":2,
"features":"1.新增了自动更新特性\n2.修复了小小bug\n3.求更新啊",
"url":"http://xxx.com/AutoUpdate.apk",
"length":281686
}
布局notify_upgrade.xml定义了通知栏的布局，主要是一个水平进度条。


AutoUpgrade类主要使用说明：
实例化的时候，可以传进一个远程json地址；不传递的时候，默认查找strings.xml中的default_url资源。
setDownload( IDownload实现)，对应下载前中后的操作，可以实现这个接口，静默下载。
    如果有实现，则在onPreDownload()中根据用户的选择调用下载任务AutoUpgrade的startTask()。
    之后可以调用AutoUpgrade的getUo()获得解析JSON后得到的内容。
setUo(IUpdateObject实现)必须实现，不然无法解析json数据。
setSavePath(String)可以设置下载的apk保存路径。
setAutoInstall(boolean)设置是否下载完毕自动安装。
start() 更新的最初一步。首先获取远程json，判断是否需要更新。如果需要更新，才会调用IDownload的onPreDownload实现。

