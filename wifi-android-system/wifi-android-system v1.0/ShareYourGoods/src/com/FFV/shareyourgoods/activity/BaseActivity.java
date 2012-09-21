package com.FFV.shareyourgoods.activity;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

import org.apache.http.conn.util.InetAddressUtils;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.net.NetConnHelpThread;
import com.FFV.shareyourgoods.net.NetTcpFileRcvThread;
import com.FFV.shareyourgoods.service.MusicService;
import com.FFV.shareyourgoods.util.MsgConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {
	private final static String TAG = "BaseActivity";

	protected static InetAddress connector = null;
	protected static LinkedList<BaseActivity> queue = new LinkedList<BaseActivity>();
	protected static NetConnHelpThread netConnHelper;

	private static ProgressDialog mProDlg = null;
	private int mWidth = 0;
	private int mHeight = 0;
	private float mOldX;
	private float mOldY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.GINGERBREAD)
			showNotSupportDialog(getApplicationInfo().targetSdkVersion);

		mWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		mHeight = this.getWindowManager().getDefaultDisplay().getHeight();

		netConnHelper = NetConnHelpThread.newInstance();

		if (!queue.contains(this))
			queue.add(this);
		
		File fileDir = new File("/mnt/sdcard/FileTransRcv/");
		if (!fileDir.exists())
			fileDir.mkdir();
		fileDir = new File("/mnt/sdcard/FileTransRcv/Picture/");
		if (!fileDir.exists())
			fileDir.mkdir();
		fileDir = new File("/mnt/sdcard/FileTransRcv/Music/");
		if (!fileDir.exists())
			fileDir.mkdir();
	}

	public static BaseActivity getActivity(int index) {
		if (index < 0 || index >= queue.size())
			throw new IllegalArgumentException("out of queue");
		return queue.get(index);
	}

	public static BaseActivity getCurActivity() {
		return queue.getLast();
	}

	public abstract void sendFile();

	public abstract void processMsg(Message msg);

	@Override
	public void finish() {
		super.finish();
		queue.removeLast();
	}

	public static void sendMessage(int cmd, String text) {
		Message msg = new Message();
		msg.obj = text;
		msg.what = cmd;
		sendMessage(msg);
	}

	public static void sendMessage(Message msg) {
		if (!queue.isEmpty())
			queue.getLast().handler.sendMessage(msg);
	}

	public static void sendEmptyMessage(int what) {
		if (!queue.isEmpty())
			queue.getLast().handler.sendEmptyMessage(what);
	}

	public void makeTextShort(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	public void makeTextLong(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MsgConfig.MSG_FILE_SND_IRQ:
				mProDlg = new ProgressDialog(BaseActivity.this);
				mProDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProDlg.setMax(100);
				mProDlg.setProgress(0);
				mProDlg.setMessage("开始发送文件。。。");
				mProDlg.show();
				break;

			case MsgConfig.MSG_FILE_SND_PER:
				int[] sendedPer = (int[]) msg.obj;
				mProDlg.setMessage("第" + (sendedPer[0] + 1) + "文件发送中。。。");
				mProDlg.setProgress(sendedPer[1]);
				break;

			case MsgConfig.MSG_FILE_SND_SCS:
				int[] scsNums = (int[]) msg.obj;
				mProDlg.setMessage("第" + scsNums[0] + "文件发送成功！");

				if (scsNums[0] == scsNums[1]) {
					mProDlg.setMessage("所有文件发送成功");
					mProDlg.dismiss();
					queue.getLast().makeTextShort("文件发送成功");
				}
				break;

			case MsgConfig.MSG_FILE_RCV_IRQ:
				String[] extraMsg = (String[]) msg.obj;
				byte[] bt = { 0x07 };
				String[] fileList = extraMsg[2].split(new String(bt));
				Log.d(TAG, fileList.length + "个接受文件请求，文件信息为：" + extraMsg[2]);

				Thread fileRcvThread = new Thread(new NetTcpFileRcvThread(
						extraMsg[0], extraMsg[1], fileList));
				fileRcvThread.start();

				mProDlg = new ProgressDialog(BaseActivity.this);
				mProDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProDlg.setMax(100);
				mProDlg.setProgress(0);
				mProDlg.setMessage("开始接收文件。。。");
				mProDlg.show();

				break;
			case MsgConfig.MSG_FILE_RCV_PER:
				int[] rcvedPer = (int[]) msg.obj;

				mProDlg.setMessage("第" + (rcvedPer[0] + 1) + "文件接收中。。。");
				mProDlg.setProgress(rcvedPer[1]);

				break;
			case MsgConfig.MSG_FILE_RCV_SCS:
				int[] scsNum = (int[]) msg.obj;
				mProDlg.setMessage("第" + scsNum[0] + "文件接收成功！");

				if (scsNum[0] == scsNum[1]) {
					mProDlg.setMessage("所有文件接收成功");
					mProDlg.dismiss();
					queue.getLast().makeTextShort("文件接收成功");
				}
				break;
			default:
				if (queue.size() > 0)
					queue.getLast().processMsg(msg);
			}
		}
	};

	public void exit() {
		while (queue.size() > 0)
			queue.getLast().finish();
	}

	// 定义当点击退出时弹出的对话框
	public void onClickExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setIcon(R.drawable.dialog_information).setTitle("提示")
				.setMessage("确定要退出吗?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						stopService(new Intent(BaseActivity.this,
								MusicService.class));
						netConnHelper.disconnnectSocket();
						closeWifi();
						System.exit(0);
					}
				}).setNegativeButton("取消", null).show();
	}

	// 定义当主菜单返回时，弹出菜单
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		// 返回按键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.onClickExit();
		}

		return super.onKeyDown(keyCode, event);
	}

	// 设置菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * add()方法的四个参数，依次是： 1、组别，如果不分组的话就写Menu.NONE,
		 * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单 3、顺序，那个菜单现在在前面由这个参数的大小决定
		 * 4、文本，菜单的显示文本
		 */
		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "Image").setIcon(
				R.drawable.image);
		menu.add(Menu.NONE, Menu.FIRST + 4, 4, "Music").setIcon(
				R.drawable.music);
		menu.add(Menu.NONE, Menu.FIRST + 5, 5, "File").setIcon(R.drawable.file);
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "关于").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(
				android.R.drawable.ic_lock_power_off);
		return true;
	}

	private void showNotSupportDialog(int version) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"TargetSDKVersion is " + version
						+ "!\nThe program is not supported by your phone!")
				.setNeutralButton("Exit",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mOldX = event.getX();
			mOldY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			float changedX = Math.abs(mOldX - event.getX());
			float changedY = mOldY - event.getY();

			if (changedX <= mWidth / 10 && changedY >= mHeight / 4) {
				sendFile();
			}
			break;
		default:
			break;

		}

		return super.onTouchEvent(event);
	}

	// 得到本机IP地址
	public static String getLocalIpAddress() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
				while (enumIpAddr.hasMoreElements()) {
					InetAddress mInetAddress = enumIpAddr.nextElement();
					if (!mInetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(mInetAddress
									.getHostAddress())) {
						return mInetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	private void closeWifi() {
		WifiManager wifiMngr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		if(wifiMngr.isWifiEnabled())
			wifiMngr.setWifiEnabled(false);
		else
			setWifiApDisable(wifiMngr);
	}
	
	private void setWifiApDisable(WifiManager wifiMngr) {
		try {
			WifiConfiguration wifiConfig = new WifiConfiguration();
			wifiConfig.SSID = "GossipDog";
			wifiConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

			Method method = wifiMngr.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, Boolean.TYPE);
			method.invoke(wifiMngr, wifiConfig, false);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Cannot set WiFi Hot Point state", e);
		}
	}
}
