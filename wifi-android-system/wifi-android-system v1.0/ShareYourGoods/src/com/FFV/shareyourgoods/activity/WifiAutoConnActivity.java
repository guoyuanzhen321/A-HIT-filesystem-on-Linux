package com.FFV.shareyourgoods.activity;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Date;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.util.MsgConfig;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class WifiAutoConnActivity extends BaseActivity implements
		SensorEventListener {
	private final static String TAG = "WifiSoftAP";

	SensorManager snsrMng;
	private WifiManager wifiMng;
	private WifiReceiver wifiRcv;
	private float degree;
	private static boolean isHotPoint;
	private static ProgressDialog ProDlg = null;
	
	private boolean dgrGotten = false;
	private boolean firstCon = true;
	private boolean firstRcv = true;
	private long time = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcom);
		snsrMng = (SensorManager) getSystemService(SENSOR_SERVICE);
		wifiMng = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		Date date = new Date();
		time = date.getTime();
		degree = 0;
		isHotPoint = false;
		ProDlg = new ProgressDialog(this);
		ProDlg.setMessage("正在努力连接wifi中，请适当调整您手机的角度。如果长时间连接不上，请您重新启动本软件。。。");
		ProDlg.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 注册监听器 
		if (wifiRcv != null)
			registerReceiver(wifiRcv, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		snsrMng.registerListener(this,
				snsrMng.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		snsrMng.unregisterListener(this);
		if (wifiRcv != null)
			unregisterReceiver(wifiRcv);
		super.onPause();
	}

	@Override
	protected void onStop() {
		snsrMng.unregisterListener(this);
		super.onStop();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int snsrType = event.sensor.getType();
		if (snsrType == Sensor.TYPE_ORIENTATION) {
			if (firstCon) {
				degree = event.values[0];
				if (degree >= 180) {
					boolean flag = setWifiApEnabled(true);
					if (flag)
						getNetConnection();
					isHotPoint = true;
				} else {
					connectAP();
					isHotPoint = false;
				}
				firstCon = false;
			} else {
				// 如果过了timecounter2加到了300（大概是2*3 = 6秒）
				Date date = new Date();
				long curTime = date.getTime();

				if (curTime - time >= 1000) {
					time = curTime;
					float curDegree = event.values[0];
					if ((degree < 180 && curDegree >= 180)
							|| (degree >= 180 && curDegree < 180)) {
						degree = curDegree;
						if (isHotPoint)
							connectAP();
						else {
							boolean flag = setWifiApEnabled(true);
							if (flag)
								getNetConnection();
						}
						isHotPoint = !isHotPoint;
					}

					if (netConnHelper.getWorkState()) {
						if (connector == null) {
							if (!isHotPoint)
								netConnHelper.broadcastOnLine();
						} else {
								netConnHelper.sendDirection(connector, degree);
						}
					}
				}
			}
		}
	}

	private boolean setWifiApEnabled(boolean enabled) {
		// TODO 自动生成的方法存根
		if (enabled)
			wifiMng.setWifiEnabled(false);
		try {
			WifiConfiguration wifiConfig = new WifiConfiguration();
			wifiConfig.SSID = "GossipDog";
			wifiConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

			Method method = wifiMng.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) method.invoke(wifiMng, wifiConfig, enabled);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Cannot set WiFi Hot Point state", e);
			return false;
		}
	}

	private int getWifiApState() {
		// TODO 自动生成的方法存根
		try {
			Method method = wifiMng.getClass().getMethod("getWifiApState");
			return (Integer) method.invoke(wifiMng);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Cannot get WiFi Hot Point state", e);
			return 4;
		}
	}

	private void connectAP() {
		// TODO 自动生成的方法存根
		int wifiState = getWifiApState();
		if (wifiState != 1) {
			setWifiApEnabled(false);
		}
		if (wifiMng.getWifiState() == 1)
			wifiMng.setWifiEnabled(true);
		wifiRcv = new WifiReceiver();
		registerReceiver(wifiRcv, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiMng.startScan();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO 自动生成的方法存根
	}

	@Override
	public void processMsg(Message msg) {
		// TODO 自动生成的方法存根

		switch (msg.what) {
		case MsgConfig.MSG_USR_ONLINE:
			InetAddress conn = (InetAddress) msg.obj;
			if (conn != null)
				connector = conn;
			if(connector != null)
				netConnHelper.sendDirection(connector, degree);
			break;

		case MsgConfig.MSG_LOC_DIRECTION:
			if(dgrGotten) {
				netConnHelper.sendDirectionRsp(connector);
				break;
			}
			float degre = (Float) msg.obj;
			float tmp = Math.abs(Math.abs(degre - degree) - 180);
			if (tmp <= 120) {
				dgrGotten = true;				
				netConnHelper.sendDirectionRsp(connector);
			}
			break;
			
		case MsgConfig.MSG_LOC_RESPONSE:
			netConnHelper.sendDirection(connector, degree);
			if(dgrGotten) {
				netConnHelper.sendDirectionRsp(connector);
				ProDlg.dismiss();
				Toast.makeText(this, "成功建立连接", Toast.LENGTH_SHORT + 1).show();
				
				Intent intent = new Intent();
				intent.setClass(this, ImgActivity.class);
				startActivity(intent);
				this.finish();
			}
			break;

		default:
			break;

		}
	}

	private void getNetConnection() {
//		if(netConnHelper.getWorkState())
//			netConnHelper.disconnnectSocket();
		connector = null;
		netConnHelper.connectSocket();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * add()方法的四个参数，依次是： 1、组别，如果不分组的话就写Menu.NONE,
		 * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单 3、顺序，那个菜单现在在前面由这个参数的大小决定
		 * 4、文本，菜单的显示文本
		 */
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "关于").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(
				android.R.drawable.ic_lock_power_off);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		// 关于
		if (item.getItemId() == Menu.FIRST + 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setIcon(R.drawable.dialog_information).setTitle("关于")
					.setMessage(R.string.about)// string.xml中定义的about
					.setPositiveButton("确定", null).show();
		} else if (item.getItemId() == Menu.FIRST + 2) {// 退出
			this.onClickExit();
		}
		return true;
	}

	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 自动生成的方法存根
			if (firstRcv) {

				WifiConfiguration wcg = new WifiConfiguration();
				wcg.SSID = "\"GossipDog\"";
				wcg.wepKeys[0] = "";
				wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				wcg.wepTxKeyIndex = 0;
				wcg.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.OPEN);
				wcg.networkId = wifiMng.addNetwork(wcg);
				boolean connected = wifiMng.enableNetwork(wcg.networkId, true);

				if (connected == true) {
					firstRcv = false;
					getNetConnection();
				}
			}
		}
	}

	@Override
	public void sendFile() {
		// TODO 自动生成的方法存根
		
	}

}
