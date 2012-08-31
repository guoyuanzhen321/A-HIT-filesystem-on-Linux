//wifi-android-system v0.1

// "THE BEER-WARE LICENSE" (Revision 43):
// Steve Guo wrote this file. As long as you retain this notice you can do whatever you want with this stuff. 
// If we meet some day, and you think this stuff is worth it, you can buy me a beer in return.


package myownwifi.com;

import myownwifi.com.R;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import myownwifi.com.*;
import android.R.string;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiInfo;     
import android.net.wifi.WifiManager.WifiLock; 


public class MyownwifiActivity extends Activity implements SensorEventListener{
	
	SensorManager mSensorManager; //管理器
	
    private final String TAG = "WifiSoftAP";
    public static final String WIFI_AP_STATE_CHANGED_ACTION =
        "android.net.wifi.WIFI_AP_STATE_CHANGED";

    public static final int WIFI_AP_STATE_DISABLING = 0;
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLING = 2;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    public static final int WIFI_AP_STATE_FAILED = 4;  
    //TextView result;
    public static WifiManager wifiManager;
    public static WifiReceiver receiverWifi;
    public static List<ScanResult> wifiList;
	//网络连接列表  
    public static List<WifiConfiguration> wifiConfiguration;
	StringBuilder resultList = new StringBuilder();
	
    public float degree;//当前方向传感器的角度值（0-360），刷新速度非常快
    
    public float passoutdegree = -1;//大概每六秒一次，将degree的值赋给passoutdegree
    								//用于判断当前手机朝向（0-180？180-360？）是否发生改变
    public int index = 1;//由于wifireceiver一直在收到wifi的广播，所以一直会重复连接相同wifi热点的动作
    					 //index用于限制wifireceiver使其只在第一次收到广播时连接特定wifi热点
    public int diyici = 1;//同index作用相同
    public int timecounter1 = 1;//由于对android编程不够熟悉，android的定时以及延时操作没有掌握
    public int timecounter2 = 0;//所以用timecounter1和timecounter2来代替定时功能，
    							//每次循环加一，到达几百时进行相关操作并把timecounter清零，大概100次循环能够延时2秒左右
    public int APorconnect = 0;//记录当前得到的角度对应的手机应有的状态（180-360：建立wifi热点；0-180：连接wifi热点）
    public int formerAPorconnect = 0;//记录上一次（大概6秒前）手机应有的状态
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //获取管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        formerAPorconnect = APorconnect;//将当前状态赋值给上一次状态
 
        //程序建立一开始，先进行一次，判断当前角度，决定建立wifi热点或者连接wifi
        if(passoutdegree >= 180){
    		APorconnect = 1;
    	}
    	else{
       		APorconnect = 0;
    	}
		if(APorconnect == 1){
			setWifiApEnabled(true);
		}
		else{			
			connectAP();
		}
    }
    @Override 
    protected void onResume(){
    	super.onResume();
    	//注册监听器
    	if (receiverWifi != null)
			registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }  
    //取消注册
    @Override
    protected void onPause(){
    	mSensorManager.unregisterListener(this);
    	if (receiverWifi != null)
			unregisterReceiver(receiverWifi);
    	super.onPause();
    	
    }
    //注销监听
    @Override
    protected void onStop(){
    	mSensorManager.unregisterListener(this);
    	super.onStop();
    	
    }
    //精度改变
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		//获取触发event的传感器类型		
		int sensorType = event.sensor.getType();
		//监测方向传感器，得到方向角度（0-360；0、90、180、360分别对应于东南西北）
		//如果两个手机相对，必然一个在（0，180）范围，另一个在（180,360）范围
		//那么只要令（180,360）建立wifi热点，另一个（0,180）进行连接特定wifi热点即可实现通讯
		switch(sensorType){
			case Sensor.TYPE_ORIENTATION:
				degree = event.values[0]; //获取z转过的角度
				TextView ddegree = (TextView) findViewById(R.id.Direction);
				ddegree.setText("Direction:" + String.valueOf(degree));//这个值会不停的变化，只要角度发生轻微改变，这个函数就会被调用
				//第一次得到方向角度的时候
				if(diyici == 1){
					passoutdegree = degree;
					TextView passdegree = (TextView) findViewById(R.id.wifistate);
					passdegree.setText("Passoutdegree:" + String.valueOf(passoutdegree));
			        if(passoutdegree >= 180){
			        	setWifiApEnabled(true);
			    	}
			    	else{
			    		connectAP();
			    	}
			        diyici ++;
				}
				//之后得到方向角度的时候
				else{
					//一些费操作
					if(diyici % 2 == 0)
						diyici ++;
					else
						diyici --;
					//如果过了timecounter2加到了300（大概是2*3 = 6秒）
					if(timecounter2 % 300 == 0)
					{
						timecounter2 = 1;//timecounter重新开始计数
						passoutdegree = degree;//得到当前的方向传感器角度值
						TextView passdegree = (TextView) findViewById(R.id.wifistate);
						passdegree.setText("Passoutdegree:" + String.valueOf(passoutdegree));
						//根据角度值判断手机应该建立wifi热点还是连接wifi热点
				        if(passoutdegree >= 180){
				    		APorconnect = 1;
				    	}
				    	else{
				       		APorconnect = 0;
				    	}
				        //判断现在手机应该处在的状态是否和前一次判断相同。不相同则做出相应改变，相同则没有动作
				        if(formerAPorconnect != APorconnect){
				        	if(APorconnect == 1){
								setWifiApEnabled(true);
							}
							else{								
								connectAP();
							}
				        	formerAPorconnect = APorconnect;//将当前状态赋值给上一次状态
				        }				
					}
					
					timecounter2 ++;					
				}				
		}		
	}
    //传感器值改变
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
		
	}
	//其实没有用到startscan
	public void StartScan() {
		//打开wifi
		
		wifiManager.setWifiEnabled(true);

		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		//result.setText("\nScaning...\n");
		TextView scannet = (TextView) findViewById(R.id.State);
		scannet.setText("State: Scaning...");
	}	
	//建立wifi热点的函数，因为连接有密码和保密协议的wifi热点的代码总是写不好，所以选择建立没有密码的公开wifi热点
	public boolean setWifiApEnabled(boolean enabled) {
		if (enabled) { // disable WiFi in any case
			wifiManager.setWifiEnabled(false);
		}
    	
		try {
			WifiConfiguration apConfig = new WifiConfiguration();
			apConfig.SSID = "GossipDog";
			apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			
			Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			Log.e(TAG, "Cannot set WiFi AP state", e);
			return false;
		}
	}	
	//这个其实也没有用到
	public int getWifiApState() {
		try {
			Method method = wifiManager.getClass().getMethod("getWifiApState");
			return (Integer) method.invoke(wifiManager);
		} catch (Exception e) {
			Log.e(TAG, "Cannot get WiFi AP state", e);
			return WIFI_AP_STATE_FAILED;
		}
	}
	//同上
	public boolean isApEnabled() {
        int state = getWifiApState();
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
	}
    //连接GossipDog热点  
    public void connectAP() {
    	int wifistate = getWifiApState();
    	if(wifistate != 1){
        	setWifiApEnabled(false);
    	}  	
    	if(wifiManager.getWifiState() == 1)
    		wifiManager.setWifiEnabled(true);
    	receiverWifi = new WifiReceiver();//主要操作都是在新建这个receiverwifi类的对象和监听receiverwifi时进行的   	
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		return ;
    	
    }   
    //没有用到
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
        	BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return connectedIP;
    }
	
    //wifireceiver类定义
	class WifiReceiver extends BroadcastReceiver {

		public void onReceive(Context c, Intent intent) {
			//同上面传感器的值总是不停改变导致上面的onchang函数总是不停调用一样，这里只想进行一次连接热点的动作（如果成功的话）
			if(index == 1){
				//这个地方的wifilist其实就是为了打印出得到的所有wifi热点SSID等信息，调试用
				resultList = new StringBuilder();
				wifiList = wifiManager.getScanResults();
				for (int i = 0; i < wifiList.size(); i++) {
					resultList.append(new Integer(i + 1).toString() + ".");
					resultList.append((wifiList.get(i)).toString());
					resultList.append("\n\n");
				}
				//真正连接wifi热点的操作
			    WifiConfiguration wcg = new WifiConfiguration();
			    wcg.SSID = "\"GossipDog\"";//这个地方必须写作“gossipdog”，双引号异常重要，纠结了好久
			    wcg.wepKeys[0] = ""; 
	            wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
	            wcg.wepTxKeyIndex = 0;
	            wcg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);   
	        	wcg.networkId = wifiManager.addNetwork(wcg);        	
	        	boolean ifconnected = wifiManager.enableNetwork(wcg.networkId, true);
	        	//如果wifi连接成功，index变为2，不再进行连接操作
	        	if(ifconnected == true){
	        		Toast.makeText(MyownwifiActivity.this, "Connected to wifiAP!", 1).show();
	        		index ++;
	        	}
	        	else
	        		Toast.makeText(MyownwifiActivity.this, "Searching...", 1).show();
			}
			//费操作
			else{
				if(index % 2 == 0){
					index ++;
				}
				else
					index --;
			}
		}
	}

}
	
	 
	
