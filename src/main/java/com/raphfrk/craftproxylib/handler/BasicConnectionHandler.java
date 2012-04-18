package com.raphfrk.craftproxylib.handler;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import com.raphfrk.craftproxylib.MCBridge;
import com.raphfrk.craftproxylib.MCServerListener;
import com.raphfrk.craftproxylib.login.LoginInfo;
import com.raphfrk.craftproxylib.login.MCLoginUtils;
import com.raphfrk.craftproxylib.packet.standard.KickPacket;

public class BasicConnectionHandler extends ConnectionHandler {

	private final InetAddress defaultAddress;
	private final int defaultPort;
	private final boolean authenticate;
	private final Map<String, InetSocketAddress> connectMap;
	
	public BasicConnectionHandler() {
		super();
		this.defaultAddress = null;
		this.authenticate = false;
		this.connectMap = null;
		this.defaultPort = 0;
	}
	
	public BasicConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener, boolean rotateIP) throws IOException {
		this(client, registry, listener, InetAddress.getLocalHost(), 25565, rotateIP);
	}
	
	public BasicConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener, InetAddress defaultAddress, int defaultPort, boolean rotateIP) throws IOException {
		this(client, registry, listener, defaultAddress, defaultPort, false, null, rotateIP);
	}
	
	public BasicConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener, InetAddress defaultAddress, int defaultPort, boolean authenticate, Map<String, InetSocketAddress> connectMap, boolean rotateIP) throws IOException {
		super(client, registry, listener, rotateIP);
		this.defaultPort = defaultPort;
		this.defaultAddress = defaultAddress;
		this.authenticate = authenticate;
		this.connectMap = connectMap;
	}


	@Override
	public void handleConnection() throws IOException {
		
		LoginInfo info = this.handleLogin();
		
		if (info == null) {
			return;
		}
		
		MCBridge bridge = bridgeConnections(info);
		
	}
	
	/**
	 * Bridges the connection between the client and server given in the LoginInfo.<br>
	 * <br>
	 * The method will return when either stream is interrupted or when the current thread is interrupted.
	 * 
	 * @param info
	 * @return
	 * @throws IOException
	 */
	protected MCBridge bridgeConnections(LoginInfo info) throws IOException {
		MCBridge bridge = new MCBridge(mServer, mClient, registry, this);
		
		bridge.start();

		try {
			bridge.join();
		} catch (InterruptedException e) {
		}
		
		bridge.interrupt();
		
		try {
			bridge.join();
		} catch (InterruptedException e) {
		}
		
		return bridge;
	}
	
	/**
	 * Logs in to a server based on the connectionMap setting
	 * 
	 * @return
	 * @throws IOException
	 */
	protected LoginInfo handleLogin() throws IOException {
		setName("(" + client.getRemoteSocketAddress().toString() + ")");

		log("Connected");
		
		LoginInfo info = null;
		
		try {
			info = MCLoginUtils.handleLogin(client, connectMap, new InetSocketAddress(defaultAddress, defaultPort), false);
		} catch (EOFException e) {
			return null;
		}
		
		if (info.getError() != null) {
			log(info.getError());
			return null;
		} else if (info.isServerPing()) {
			mClient = info.getClientSocket();
			if (mClient != null) {
				mClient.close(KickPacket.generatePing("Proxy name", 1, 50));
			}
			return null;
		}
		
		mClient = info.getClientSocket();
		mServer = info.getServerSocket();
		
		setName(info.getUsername() + " (" + client.getRemoteSocketAddress() + ")");
		
		log("Logged in successfully");
		
		return info;
	}
	
	@Override
	public BasicConnectionHandler newInstance(Socket client, PacketHandlerRegistry registry, MCServerListener listener, ConnectionConfig config) throws IOException {
		return new BasicConnectionHandler(client, registry, listener, listener.getRotateIP());
	}

}
