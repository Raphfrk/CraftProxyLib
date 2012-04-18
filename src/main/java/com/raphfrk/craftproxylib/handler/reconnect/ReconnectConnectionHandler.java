package com.raphfrk.craftproxylib.handler.reconnect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import com.raphfrk.craftproxylib.MCBridge;
import com.raphfrk.craftproxylib.MCServerListener;
import com.raphfrk.craftproxylib.MCSocket;
import com.raphfrk.craftproxylib.handler.BasicConnectionHandler;
import com.raphfrk.craftproxylib.handler.ConnectionConfig;
import com.raphfrk.craftproxylib.handler.PacketHandlerRegistry;
import com.raphfrk.craftproxylib.login.LoginInfo;
import com.raphfrk.craftproxylib.login.MCLoginUtils;

public class ReconnectConnectionHandler extends BasicConnectionHandler {
	
	public ReconnectConnectionHandler() {
		super();
	}
	
	public ReconnectConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener, boolean rotateIP) throws IOException {
		super(client, registry, listener, InetAddress.getLocalHost(), 25565, rotateIP);
	}
	
	@Override
	public void handleConnection() throws IOException {
		
		LoginInfo info = this.handleLogin();
		
		if (info == null) {
			return;
		}
		
		String target = "";

		while (target != null && info.getError() == null) {
			MCBridge bridge = bridgeConnections(info);

			ReconnectPacketHandler kickHandler = (ReconnectPacketHandler)bridge.getDownstreamHandlers()[0xFF];

			if (!mServer.isClosed()) {
				mServer.close("Disconnecting");
			}
			
			target = kickHandler.getRedirectTarget();
			
			if (target != null) {
				String hostname = MCSocket.parseHostname(target);
				int port = MCSocket.parsePort(target);
				
				log("Attempting reconnect to " + hostname + ":" + port);

				info = MCLoginUtils.handleLogin(mClient, info, hostname, port, rotateIP);
				
				mServer = info.getServerSocket();
				
				if (info.getError() != null) {
					log(info.getError());
					break;
				} else {
					log("Connection successful");
				}
			}
		}
	}
	
	@Override
	public ReconnectConnectionHandler newInstance(Socket client, PacketHandlerRegistry registry, MCServerListener listener, ConnectionConfig config) throws IOException {
		return new ReconnectConnectionHandler(client, registry, listener, listener.getRotateIP());
	}

}
