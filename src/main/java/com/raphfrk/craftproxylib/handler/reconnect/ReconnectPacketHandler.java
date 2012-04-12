package com.raphfrk.craftproxylib.handler.reconnect;

import java.io.IOException;

import com.raphfrk.craftproxylib.MCOutputStream;
import com.raphfrk.craftproxylib.handler.PacketHandler;
import com.raphfrk.craftproxylib.packet.Packet;

public class ReconnectPacketHandler extends PacketHandler {
	
	@Override
	public Packet handle(Packet packet, MCOutputStream out, MCOutputStream ret) throws IOException {
		if (packet.getId() != 0xFF) {
			throw new IOException("Wrong packet sent to ReconnectPacketHandler");
		}
		
		String message = (String)packet.readField(0);
		
		if (message.contains("[Redirect]")) {
			return null;
		} else {
			return packet;
		}
	}
	
	@Override
	public ReconnectPacketHandler newInstance() {
		return new ReconnectPacketHandler();
	}

}
