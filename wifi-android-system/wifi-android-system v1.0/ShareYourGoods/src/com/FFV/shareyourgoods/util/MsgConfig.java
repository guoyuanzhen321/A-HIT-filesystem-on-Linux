package com.FFV.shareyourgoods.util;

/**
 * 常量配置
 * 
 * @author Trey
 * 
 *         2012/9/1
 */
public class MsgConfig {
	public final static int VERSION = 0x001; // 版本号
	public static final int PORT = 0x0979; // 端口号，默认为2425

	// 消息模式
	public static final int MSG_FILE_SND_SCS = 0xFF00; // 文件发送成功
	public static final int MSG_FILE_SND_IRQ = 0xFE00; // 文件发送请求
	public static final int MSG_FILE_SND_PER = 0xFD00;// 文件发送进度
	public static final int MSG_FILE_RCV_SCS = 0xFC00; // 文件接收成功
	public static final int MSG_FILE_RCV_IRQ = 0xFB00; // 文件接收请求
	public static final int MSG_FILE_RCV_PER = 0xFA00;// 文件接收进度
	public static final int MSG_FILE_INFOS = 0xF900; // 文件信息
	public static final int MSG_FILE_IMG_RCV = 0xF800;// 收到图片文件
	public static final int MSG_FILE_MP3_RCV = 0xF700;// 收到音乐文件
	public static final int MSG_FILE_DIY_RCV = 0xF600;// 收到自定义文件

	public static final int MSG_USR_ONLINE = 0xEF00; // 用户上线
	public static final int MSG_USR_OFFLINE = 0xEE00; // 用户下线

	public static final int MSG_LOC_DIRECTION = 0xDF00; // 方向定位参数
	public static final int MSG_LOC_RESPONSE = 0xDE00; // 方位定位应答

	// 命令
	public static final int IPMSG_NO_OPERATION = 0x00000000; // 不进行任何操作

	// WIFI连接
	public static final int IPMSG_USR_ONLINE = 0x00000001; // 用户上线
	public static final int IPMSG_USR_OFFLINE = 0x00000002; // 用户下线
	public static final int IPMSG_USR_RESPONSE = 0x00000003; // 上线应答

	// 定位参数
	public static final int IPMSG_LOC_DIRECTION = 0x00000011; // 方向定位参数
	public static final int IPMSG_LOC_RESPONSE = 0x00000012; // 方向定位应答
	public static final int IPMSG_LOC_COORDINATES = 0x00000021; // 坐标定位参数

	// 文件传输
	public static final int IPMSG_FILE_TRANS = 0x00001000; // 文件传输
	public static final int IPMSG_FILE_INFOS = 0x00002000; // 文件信息
	public static final int IPMSG_FILETYPE_CRD = 0x00000001; // 名片
	public static final int IPMSG_FILETYPE_IMG = 0x00000002; // 图片
	public static final int IPMSG_FILETYPE_MP3 = 0x00000003; // 音乐
	public static final int IPMSG_FILETYPE_DIY = 0x00000004; // 其他

}
