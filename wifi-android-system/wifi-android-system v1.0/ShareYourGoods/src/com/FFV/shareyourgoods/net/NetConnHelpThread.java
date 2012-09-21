package com.FFV.shareyourgoods.net;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.FFV.shareyourgoods.activity.BaseActivity;
import com.FFV.shareyourgoods.util.IpMsgProtocol;
import com.FFV.shareyourgoods.util.MsgConfig;

import android.os.Message;
import android.util.Log;

public class NetConnHelpThread implements Runnable {
	private final static String TAG = "NetConnHelpThread";
	private final static int BUFFER_SIZE = 1024;
	private static NetConnHelpThread instance = null;
	private boolean onWork = false;

	private static String hostName;
	private Thread udpThread = null;
	private DatagramSocket udpSocket = null;
	private DatagramPacket udpSndPkt = null;
	private DatagramPacket udpRcvPkt = null;
	private byte[] rcvBuffer = new byte[BUFFER_SIZE];
	private byte[] sndBuffer = null;

	private NetConnHelpThread() {
		hostName = BaseActivity.getLocalIpAddress();
	}

	public static NetConnHelpThread newInstance() {
		if (instance == null)
			instance = new NetConnHelpThread();
		return instance;
	}

	public boolean getWorkState() {
		return onWork;
	}
	
	public boolean connectSocket() {
		boolean rst = false;

		try {
			if (udpSocket == null)
				udpSocket = new DatagramSocket(MsgConfig.PORT);
			if (udpRcvPkt == null)
				udpRcvPkt = new DatagramPacket(rcvBuffer, BUFFER_SIZE);
			onWork = true;
			startThread();
			rst = true;
			Log.i(TAG, "connectionSocket()...绑定UDP端口" + MsgConfig.PORT + "成功");
		} catch (SocketException e) {
			e.printStackTrace();
			Log.e(TAG, "connectionSocket()...绑定UDP端口" + MsgConfig.PORT + "失败");
		}
		return rst;
	}

	public void disconnnectSocket() {
		// TODO 自动生成的方法存根
		onWork = false;
		stopThread();
		if(udpSocket != null) {
			udpSocket.close();
		}
			
	}

	private void startThread() {
		if (udpThread == null) {
			udpThread = new Thread(this);
			udpThread.start();
			Log.i(TAG, "正在监听UDP数据");
		}
	}

	private void stopThread() {
		if (udpThread != null) {
			udpThread.interrupt();
			Log.i(TAG, "停止监听UDP数据");
		}
	}

	public synchronized void sndUdpData(String sndStr, InetAddress sndAdd,
			int sndPort) {
		try {
			sndBuffer = sndStr.getBytes("gbk");

			udpSndPkt = new DatagramPacket(sndBuffer, sndBuffer.length, sndAdd,
					sndPort);
			udpSocket.send(udpSndPkt);
			udpSndPkt = null;
			Log.i(TAG, "成功向IP为" + sndAdd.getHostAddress() + "发送UDP数据：" + sndStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(TAG, "sendUdpData(...)...系统不支持GBK编码");
		} catch (IOException e) {
			e.printStackTrace();
			udpSndPkt = null;
			Log.e(TAG, "sendUdpData(...)...发送UDP数据包失败");
		}
	}

	public void broadcastOnLine() {
		IpMsgProtocol ipMsgSnd = new IpMsgProtocol();
		ipMsgSnd.setVersion(String.valueOf(MsgConfig.VERSION));
		ipMsgSnd.setSenderHost(hostName);
		ipMsgSnd.setCommandNO(MsgConfig.IPMSG_USR_ONLINE);
		ipMsgSnd.setAdditionalSection(hostName);
		try {
			InetAddress broadcastAdd = InetAddress.getByName("255.255.255.255");
			sndUdpData(ipMsgSnd.getProtocolString(), broadcastAdd,
					MsgConfig.PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.e(TAG, "broadcast()...广播地址有误");
		}
	}

	public void broadcastOffLine() {
		IpMsgProtocol ipMsgSnd = new IpMsgProtocol();
		ipMsgSnd.setVersion(String.valueOf(MsgConfig.VERSION));
		ipMsgSnd.setSenderHost(hostName);
		ipMsgSnd.setCommandNO(MsgConfig.IPMSG_USR_OFFLINE);
		ipMsgSnd.setAdditionalSection(hostName);

		try {
			InetAddress broadcastAdd = InetAddress.getByName("255.255.255.255");
			sndUdpData(ipMsgSnd.getProtocolString(), broadcastAdd,
					MsgConfig.PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.e(TAG, "broadcast()...广播地址有误");
		}
	}

	public void sendDirection(InetAddress conn, float angle) {
		IpMsgProtocol ipMsgSnd = new IpMsgProtocol();
		ipMsgSnd.setVersion(String.valueOf(MsgConfig.VERSION));
		ipMsgSnd.setSenderHost(hostName);
		ipMsgSnd.setCommandNO(MsgConfig.IPMSG_LOC_DIRECTION);
		ipMsgSnd.setAdditionalSection(String.valueOf(angle));

		try {
			InetAddress broadcastAdd = conn;
			sndUdpData(ipMsgSnd.getProtocolString(), broadcastAdd,
					MsgConfig.PORT);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "sendDirection(...)...方向定位参数发送失败");
		}
	}
	
	public void sendDirectionRsp(InetAddress conn) {
		IpMsgProtocol ipMsgSnd = new IpMsgProtocol();
		ipMsgSnd.setVersion(String.valueOf(MsgConfig.VERSION));
		ipMsgSnd.setSenderHost(hostName);
		ipMsgSnd.setCommandNO(MsgConfig.IPMSG_LOC_RESPONSE);
		ipMsgSnd.setAdditionalSection(hostName);

		try {
			InetAddress connAdd = conn;
			sndUdpData(ipMsgSnd.getProtocolString(), connAdd,
					MsgConfig.PORT);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "sendDirectionRsp(...)...方向定位应答发送失败");
		}
	}

	public void sendFileTransIRQ(InetAddress conn, String[] filePathArray) {
		byte[] bt = { 0x07 };
		String splitStr = new String(bt);
		IpMsgProtocol ipMsgPro = new IpMsgProtocol();
		ipMsgPro.setVersion("" + MsgConfig.VERSION);
		ipMsgPro.setSenderHost(hostName);
		ipMsgPro.setCommandNO(MsgConfig.IPMSG_FILE_INFOS);

		StringBuffer addiStrBf = new StringBuffer();
		for (String path : filePathArray) {
			File file = new File(path);
			/*
			 * boolean a = file.exists(); File[] files = file.listFiles();
			 */
			addiStrBf.append(file.getName() + ":");
			addiStrBf.append(Long.toHexString(file.length()) + ":");
			addiStrBf.append(splitStr);
		}

		ipMsgPro.setAdditionalSection(addiStrBf.toString());

		InetAddress sendto = conn;

		if (sendto != null) {
			sndUdpData(ipMsgPro.getProtocolString(), sendto, MsgConfig.PORT);
			BaseActivity.sendEmptyMessage(MsgConfig.MSG_FILE_SND_IRQ);
			Thread netTcpSndThread = new Thread(new NetTcpFileSendThread(
					filePathArray));
			netTcpSndThread.start();
		}

	}

	@Override
	public void run() {
		// TODO 自动生成的方法存根
		while (onWork) {
			try {
				udpSocket.receive(udpRcvPkt);
			} catch (IOException e) {
				e.printStackTrace();
				onWork = false;

				if (udpRcvPkt != null)
					udpRcvPkt = null;
				if (udpSocket != null)
					udpSocket = null;

				udpThread = null;
				Log.e(TAG, "UDP数据包接受失败，线程停止");
				break;
			}
			
			if (udpRcvPkt.getLength() == 0) {
				Log.i(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
				continue;
			}
			String rcvStr = "";
			try {
				rcvStr = new String(rcvBuffer, 0, udpRcvPkt.getLength(), "gbk");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e(TAG, "接收数据时，系统不支持GBK编码");
			}
			Log.i(TAG, "接收到IP为"+ udpRcvPkt.getAddress().toString() +"的UDP数据内容为:" + rcvStr);

			IpMsgProtocol ipMsgPro = new IpMsgProtocol(rcvStr);
			int cmdNo = ipMsgPro.getCommandNO();
			
			switch (cmdNo) {
			
			case MsgConfig.IPMSG_USR_ONLINE:
				hostName = BaseActivity.getLocalIpAddress();
				if (udpRcvPkt.getAddress().toString().equals("/" + hostName))
					break;

				Message msg1 = new Message();
				msg1.what = MsgConfig.MSG_USR_ONLINE;
				msg1.obj = udpRcvPkt.getAddress();
				BaseActivity.sendMessage(msg1);

				IpMsgProtocol ipMsgSnd = new IpMsgProtocol();
				ipMsgSnd.setVersion(String.valueOf(MsgConfig.VERSION));
				ipMsgSnd.setSenderHost(hostName);
				ipMsgSnd.setCommandNO(MsgConfig.IPMSG_USR_RESPONSE);
				ipMsgSnd.setAdditionalSection(hostName);

				sndUdpData(ipMsgSnd.getProtocolString() + "\0",
						udpRcvPkt.getAddress(), udpRcvPkt.getPort());
				break;

			case MsgConfig.IPMSG_USR_OFFLINE:
				BaseActivity.sendEmptyMessage(MsgConfig.MSG_USR_OFFLINE);
				break;

			case MsgConfig.IPMSG_USR_RESPONSE:
				Message msg2 = new Message();
				msg2.what = MsgConfig.MSG_USR_ONLINE;
				msg2.obj = udpRcvPkt.getAddress();
				BaseActivity.sendMessage(msg2);
				break;

			case MsgConfig.IPMSG_LOC_DIRECTION:
				Message msg3 = new Message();
				msg3.what = MsgConfig.MSG_LOC_DIRECTION;
				msg3.obj = Float.parseFloat(ipMsgPro.getAdditionalSection());
				BaseActivity.sendMessage(msg3);
				break;

			case MsgConfig.IPMSG_LOC_RESPONSE:
				BaseActivity.sendEmptyMessage(MsgConfig.MSG_LOC_RESPONSE);
				break;
				
			case MsgConfig.IPMSG_FILE_INFOS:
				String[] extraMsg = { udpRcvPkt.getAddress().getHostAddress(),
						ipMsgPro.getPacketNO(), ipMsgPro.getAdditionalSection() };
				Message msg5 = new Message();
				msg5.what = MsgConfig.MSG_FILE_RCV_IRQ;
				msg5.obj = extraMsg;
				BaseActivity.sendMessage(msg5);
				break;

			}
		}
	}

}
