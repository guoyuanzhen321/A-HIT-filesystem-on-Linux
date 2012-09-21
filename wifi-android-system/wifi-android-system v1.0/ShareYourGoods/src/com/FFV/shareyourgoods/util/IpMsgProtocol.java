package com.FFV.shareyourgoods.util;

import java.util.Date;

/**
 * 
 * IpMsgProtocol文件传输协议<br>
 * <br>
 * 协议格式：<br>
 * Version:PacketNO:SenderHost:CommandNO:AdditionalSection<br>
 * 版本号（默认为1）:数据包编号:发送主机名:命令编号:附加数据<br>
 * 
 * 其中:<br>
 * 数据包编号:一般取毫秒数，利用这个数据可以唯一区别每一个数据包<br>
 * 发送主机：发送数据包的主机名<br>
 * 命令编号：协议定义的命令，定位到相应的操作<br>
 * 附加数据：指的是对应不同的具体命令，需要提供的数据。当为上线报文时，附加信息内容是用户名和分组名，中间用"\0"分隔
 * 
 * @author Trey
 * 
 *         2012/9/1
 */

public class IpMsgProtocol {
	private String version; // 版本号
	private String packetNO; // 数据包编号
	private String senderHost; // 发送主机名
	private int commandNO; // 命令编号
	private String additionalSection; // 附加数据

	public IpMsgProtocol() {
		this.setPacketNO(getSeconds());
	}

	// 根据协议字符串初始化
	public IpMsgProtocol(String protocolString) {

		String[] args = strOp(protocolString).split(":");
		version = args[0];
		packetNO = args[1];
		senderHost = args[2];
		commandNO = Integer.parseInt(args[3]);
		additionalSection = "";
		for (int i = 4; i < args.length; i++)
			additionalSection += i == 4 ? args[i] : ":" + args[i];
	}

	// 根据参数初始化
	public IpMsgProtocol(String senderHost, int commandNO,
			String additionalSection) {
		this.version = "1";
		this.packetNO = getSeconds();
		this.senderHost = senderHost;
		this.commandNO = commandNO;
		this.additionalSection = additionalSection;
	}

	public String getProtocolString() {
		StringBuffer sb = new StringBuffer();
		sb.append(version);
		sb.append(":");
		sb.append(packetNO);
		sb.append(":");
		sb.append(senderHost);
		sb.append(":");
		sb.append(commandNO);
		sb.append(":");
		sb.append(additionalSection);
		return sb.toString();
	}

	private String getSeconds() {
		Date nowDate = new Date();
		return Long.toString(nowDate.getTime());
	}

	private String strOp(String str) {
		while (str.endsWith("\0"))
			str = str.substring(0, str.length() - 1);
		return str;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPacketNO() {
		return packetNO;
	}

	public void setPacketNO(String packetNO) {
		this.packetNO = packetNO;
	}

	public String getSenderHost() {
		return senderHost;
	}

	public void setSenderHost(String senderHost) {
		this.senderHost = senderHost;
	}

	public int getCommandNO() {
		return commandNO;
	}

	public void setCommandNO(int commandNO) {
		this.commandNO = commandNO;
	}

	public String getAdditionalSection() {
		return additionalSection;
	}

	public void setAdditionalSection(String additionalSection) {
		this.additionalSection = additionalSection;
	}

}
