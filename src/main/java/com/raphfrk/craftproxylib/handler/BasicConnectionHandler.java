package com.raphfrk.craftproxylib.handler;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.raphfrk.craftproxylib.MCBridge;
import com.raphfrk.craftproxylib.MCServerListener;
import com.raphfrk.craftproxylib.MCSocket;
import com.raphfrk.craftproxylib.login.LoginInfo;
import com.raphfrk.craftproxylib.login.MCLoginUtils;
import com.raphfrk.craftproxylib.packet.standard.KickPacket;

public class BasicConnectionHandler extends ConnectionHandler {

	private final InetAddress defaultAddress;
	private final int defaultPort;
	private final boolean authenticate;
	private final Map<String, InetSocketAddress> connectMap;
	private final AtomicReference<MCBridge> bridgeRef = new AtomicReference<MCBridge>();
	
	public BasicConnectionHandler() {
		super();
		this.defaultAddress = null;
		this.authenticate = false;
		this.connectMap = null;
		this.defaultPort = 0;
	}
	
	public BasicConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener) throws IOException {
		this(client, registry, listener, InetAddress.getLocalHost(), 25565);
	}
	
	public BasicConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener, InetAddress defaultAddress, int defaultPort) throws IOException {
		this(client, registry, listener, defaultAddress, defaultPort, false, null);
	}
	
	public BasicConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener, InetAddress defaultAddress, int defaultPort, boolean authenticate, Map<String, InetSocketAddress> connectMap) throws IOException {
		super(client, registry, listener);
		this.defaultPort = defaultPort;
		this.defaultAddress = defaultAddress;
		this.authenticate = authenticate;
		this.connectMap = connectMap;
	}


	@Override
	public void handleConnection() throws IOException {
		setName(client.getRemoteSocketAddress().toString());

		log("Connected");
		
		LoginInfo info = null;
		
		try {
			info = MCLoginUtils.handleLogin(client, connectMap, new InetSocketAddress(defaultAddress, defaultPort), false);
		} catch (EOFException e) {
			return;
		}
		
		if (info.getError() != null) {
			log(info.getError());
			return;
		} else if (info.isServerPing()) {
			MCSocket mClient = info.getClientSocket();
			if (mClient != null) {
				mClient.close(KickPacket.generatePing("Proxy name", 1, 50));
			}
			return;
		}
		
		setName(client.getRemoteSocketAddress() + "(" + info.getUsername() + ")");
		
		log("Logged in successfully");
		
		MCSocket mClient = info.getClientSocket();
		MCSocket mServer = info.getServerSocket();
		
		MCBridge bridge = new MCBridge(mServer, mClient);
		
		bridgeRef.compareAndSet(null, bridge);
		
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
		
		mServer.printRecentPacketIds();
		mClient.printRecentPacketIds();
		
		if (!mServer.isClosed()) {
			mServer.close();
		}
		
		if (!mClient.isClosed()) {
			mClient.close();
		}
		
	}
	
	private void log(String message) {
		System.out.println(getName() + ": " + message);
	}

	@Override
	public BasicConnectionHandler newInstance(Socket client, PacketHandlerRegistry registry, MCServerListener listener, ConnectionConfig config) throws IOException {
		return new BasicConnectionHandler(client, registry, listener);
	}

}
