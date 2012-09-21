package com.FFV.shareyourgoods.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

import com.FFV.shareyourgoods.activity.BaseActivity;
import com.FFV.shareyourgoods.util.IpMsgProtocol;
import com.FFV.shareyourgoods.util.MsgConfig;

/**
 * TCP协议接收文件线程
 * 
 * @author Trey
 * 
 */

public class NetTcpFileRcvThread implements Runnable {
	private final static String TAG = "NetTcpFileRcvThread";
	private final static int BUFFER_LENGTH = 1024;

	private String[] fileInfos; // 文件信息字符数组
	private String senderIp; // 发送者IP
	private long packetNo;
	private String savePath; // 文件保存路径

	private Socket socket;
	private BufferedInputStream bfdInStrm;
	private BufferedOutputStream bfdOutStrm;
	private BufferedOutputStream fBfdOutStrm;
	private byte[] readBuffer = new byte[BUFFER_LENGTH];

	public NetTcpFileRcvThread(String senderIp, String packetNo,
			String[] fileInfos) {
		this.fileInfos = fileInfos;
		this.packetNo = Long.valueOf(packetNo);
		this.senderIp = senderIp;

		savePath = "/mnt/sdcard/FileTransRcv/";
	}

	@Override
	public void run() {
		// TODO 自动生成的方法存根

		for (int i = 0; i < fileInfos.length; i++) {
			String[] fileInfo = fileInfos[i].split(":");

			IpMsgProtocol ipMsgPro = new IpMsgProtocol();
			ipMsgPro.setVersion(String.valueOf(MsgConfig.VERSION));
			ipMsgPro.setCommandNO(MsgConfig.IPMSG_FILE_TRANS);
			ipMsgPro.setSenderHost(BaseActivity.getLocalIpAddress());
			String additionalStr = Long.toHexString(packetNo) + ":" + i + ":0:";
			ipMsgPro.setAdditionalSection(additionalStr);

			try {
				socket = new Socket(senderIp, MsgConfig.PORT);
				Log.d(TAG, "已连接上发送端");
				bfdOutStrm = new BufferedOutputStream(socket.getOutputStream());

				byte[] sndBuffer = ipMsgPro.getProtocolString().getBytes("gbk");
				bfdOutStrm.write(sndBuffer, 0, sndBuffer.length);
				bfdOutStrm.flush();
				Log.d(TAG,
						"通过TCP发送接收指定文件命令。命令内容是：" + ipMsgPro.getProtocolString());

				if (fileInfo[0].endsWith(".jpg")
						|| fileInfo[0].endsWith(".gif")
						|| fileInfo[0].endsWith(".jpeg"))
					savePath += "Picture/";
				else if (fileInfo[0].endsWith(".mp3")
						|| fileInfo[0].endsWith(".wma")
						|| fileInfo[0].endsWith(".ape"))
					savePath += "Music/";

				File receiveFile = new File(savePath + fileInfo[0]);
				if (receiveFile.exists())
					receiveFile.delete();
				fBfdOutStrm = new BufferedOutputStream(new FileOutputStream(
						receiveFile));
				Log.d(TAG, "准备开始接收文件....");
				bfdInStrm = new BufferedInputStream(socket.getInputStream());
				int length = 0;
				long sended = 0;
				long total = Long.parseLong(fileInfo[1], 16);
				int tmp = 0;

				while ((length = bfdInStrm.read(readBuffer)) != -1) {
					fBfdOutStrm.write(readBuffer, 0, length);
					fBfdOutStrm.flush();

					sended += length;
					int sendedPer = (int) (sended * 100 / total);

					if (tmp != sendedPer) {
						int[] msgObj = { i, sendedPer };
						Message msg = new Message();
						msg.what = MsgConfig.MSG_FILE_RCV_PER;
						msg.obj = msgObj;
						BaseActivity.sendMessage(msg);
						tmp = sendedPer;
					}

				}
				Log.i(TAG, "第" + (i + 1) + "个文件接收成功，文件名为" + fileInfo[0]);

				int[] success = { i + 1, fileInfos.length };
				Message msgScs = new Message();
				msgScs.what = MsgConfig.MSG_FILE_RCV_SCS;
				msgScs.obj = success;
				BaseActivity.sendMessage(msgScs);

				if (savePath.endsWith("Picture/")) {
					new SingleMediaScanner(BaseActivity.getCurActivity(),
							savePath + fileInfo[0], true);
				} else if (savePath.endsWith("Music/")) {
					new SingleMediaScanner(BaseActivity.getCurActivity(),
							savePath + fileInfo[0], false);
				} else {
					BaseActivity.sendEmptyMessage(MsgConfig.MSG_FILE_DIY_RCV);
				}

				savePath = "/mnt/sdcard/FileTransRcv/";

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "....系统不支持GBK编码");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "远程IP地址错误");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "文件创建失败");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "发生IO错误");
			} finally { // 处理

				if (bfdOutStrm != null) {
					try {
						bfdOutStrm.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					bfdOutStrm = null;
				}

				if (fBfdOutStrm != null) {
					try {
						fBfdOutStrm.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fBfdOutStrm = null;
				}

				if (bfdInStrm != null) {
					try {
						bfdInStrm.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					bfdInStrm = null;
				}

				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					socket = null;
				}

			}
		}
	}

	public class SingleMediaScanner implements MediaScannerConnectionClient {

		private MediaScannerConnection mMs;
		private String mPath;
		private boolean mFlag;

		public SingleMediaScanner(Context context, String path, boolean flag) {
			mPath = path;
			mFlag = flag;
			mMs = new MediaScannerConnection(context, this);
			mMs.connect();
		}

		@Override
		public void onMediaScannerConnected() {
			mMs.scanFile(mPath, null);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			mMs.disconnect();
			if (mFlag) {
				Message msgs = new Message();
				msgs.what = MsgConfig.MSG_FILE_IMG_RCV;
				msgs.obj = mPath;
				BaseActivity.sendMessage(msgs);
			} else {
				Message msgs = new Message();
				msgs.what = MsgConfig.MSG_FILE_MP3_RCV;
				msgs.obj = mPath;
				BaseActivity.sendMessage(msgs);
			}
		}

	}

}
