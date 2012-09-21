package com.FFV.shareyourgoods.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Message;
import android.util.Log;

import com.FFV.shareyourgoods.activity.BaseActivity;
import com.FFV.shareyourgoods.util.IpMsgProtocol;
import com.FFV.shareyourgoods.util.MsgConfig;

/**
 * TCP协议发送文件线程
 * 
 * @author Trey
 * 
 *         2012/9/1
 */

public class NetTcpFileSendThread implements Runnable {
	private final static String TAG = "NetTcpFileSendThread";
	private final static int BUFFER_LENGTH = 1024; // 缓冲区大小
	private String[] filePathArray; // 保存发送文就按路径的数组

	private static ServerSocket server;
	private Socket socket;
	private byte[] readBuffer = new byte[BUFFER_LENGTH]; // 文件读入缓冲区

	public NetTcpFileSendThread(String[] filePathArray) {
		this.filePathArray = filePathArray;
		try {
			server = new ServerSocket(MsgConfig.PORT);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.e(TAG, "监听TCP端口失败");
		}
	}

	@Override
	public void run() {
		// TODO 自动生成的方法存根
		for (int i = 0; i < filePathArray.length; i++) {
			try {
				socket = server.accept();

				Log.i(TAG, "与IP为" + socket.getInetAddress().getHostAddress()
						+ "的用户建立TCP连接");

				BufferedOutputStream bfdOutStrm = new BufferedOutputStream(
						socket.getOutputStream());
				BufferedInputStream bfdInStrm = new BufferedInputStream(
						socket.getInputStream());

				int mLength = bfdInStrm.read(readBuffer);
				String ipMsgStr = new String(readBuffer, 0, mLength, "gbk");

				Log.d(TAG, "收到的TCP数据为： " + ipMsgStr);

				IpMsgProtocol ipMsgPro = new IpMsgProtocol(ipMsgStr);
				String fileNoStr = ipMsgPro.getAdditionalSection();
				String[] fileNoArray = fileNoStr.split(":");
				int sendFileNo = Integer.valueOf(fileNoArray[1]);

				Log.d(TAG, "本次发送的文件具体路径为： " + filePathArray[sendFileNo]);
				File sendFile = new File(filePathArray[sendFileNo]);
				BufferedInputStream fBfdInStrm = new BufferedInputStream(
						new FileInputStream(sendFile));

				int rLength = 0;
				long sended = 0;
				long total = sendFile.length();
				int tmp = 0;
				while ((rLength = fBfdInStrm.read(readBuffer)) != -1) {
					bfdOutStrm.write(readBuffer, 0, rLength);
					bfdOutStrm.flush();
					
					sended += rLength;
					int sendedPer = (int) (sended * 100 / total);
					
					if(tmp != sendedPer) {
						int[] msgObj = {i, sendedPer};
						Message msg = new Message();
						msg.what = MsgConfig.MSG_FILE_SND_PER;
						msg.obj = msgObj;
						BaseActivity.sendMessage(msg);
						tmp = sendedPer;
					}
					
//					if (sended == total)
//					break;
				}
				Log.i(TAG, "文件发送成功");
				
				int [] success = {i + 1, filePathArray.length};
				Message msgScs = new Message();
				msgScs.what = MsgConfig.MSG_FILE_SND_SCS;
				msgScs.obj = success;
				BaseActivity.sendMessage(msgScs);

				if (bfdInStrm != null) {
					bfdInStrm.close();
					bfdInStrm = null;
				}

				if (fBfdInStrm != null) {
					fBfdInStrm.close();
					fBfdInStrm = null;
				}

				if (bfdOutStrm != null) {
					bfdOutStrm.close();
					bfdOutStrm = null;
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e(TAG, "接收数据不支持和GBK编码");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				Log.e(TAG, ioe.toString());
				break;
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					socket = null;
				}
			}
		}
		
		

		if (server != null) {
			try {
				server.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
			server = null;
		}

	}
}
