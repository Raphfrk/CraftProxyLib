package com.raphfrk.craftproxylib.handler.reconnect;

import java.io.IOException;

import com.raphfrk.craftproxylib.MCOutputStream;
import com.raphfrk.craftproxylib.handler.PacketHandler;
import com.raphfrk.craftproxylib.packet.Packet;

public class ReconnectPacketHandler extends PacketHandler {
	
	private String redirectTarget = null;
	
	@Override
	public Packet handle(Packet packet, MCOutputStream out, MCOutputStream ret) throws IOException {
		if (packet.getId() != 0xFF) {
			throw new IOException("Wrong packet sent to ReconnectPacketHandler");
		}
		
		String message = (String)packet.readField(0);
		
		redirectTarget = redirectDetected(message);
		
		if (redirectTarget != null) {
			return PacketHandler.INTERRUPT;
		} else {
			return packet;
		}
	}
	
	@Override
	public ReconnectPacketHandler newInstance() {
		return new ReconnectPacketHandler();
	}
	
	public String getRedirectTarget() {
		return redirectTarget;
	}
	
	private String redirectDetected(String reason) {

		String hostName = null;
		int portNum = -1;

		log("Kick packet detected: " + reason);

		if( reason.indexOf("[Serverport]") != 0 && reason.indexOf("[Redirect]") != 0) {
			return null;
		}

		String[] split = reason.split( ":" );
		if( split.length == 3 ) {
			hostName = split[1].trim();
			try { 
				portNum = Integer.parseInt( split[2].trim() );
			} catch (Exception e) { portNum = -1; };
		} else  if( split.length == 2 ) {
			hostName = split[1].trim();
			try {
				portNum = 25565;
			} catch (Exception e) { portNum = -1; };
		}

		int commaPos = reason.indexOf(",");
		if(commaPos>=0) {
			return reason.substring(reason.indexOf(":") + 1).trim();
		}

		if( portNum != -1 ) {
			return hostName + ":" + portNum;
		} else {
			return null;

		}
	}

}
