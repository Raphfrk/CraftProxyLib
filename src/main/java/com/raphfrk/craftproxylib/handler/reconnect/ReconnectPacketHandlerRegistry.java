package com.raphfrk.craftproxylib.handler.reconnect;

import com.raphfrk.craftproxylib.handler.PacketHandlerRegistry;

public class ReconnectPacketHandlerRegistry extends PacketHandlerRegistry {
	
	public ReconnectPacketHandlerRegistry() {
		registerHandler(new ReconnectPacketHandler(), 0xFF, false);
	}

}
