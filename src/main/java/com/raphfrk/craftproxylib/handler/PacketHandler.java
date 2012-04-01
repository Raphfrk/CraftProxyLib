package com.raphfrk.craftproxylib.handler;

import java.io.IOException;

import com.raphfrk.craftproxylib.MCOutputStream;
import com.raphfrk.craftproxylib.packet.Packet;

public abstract class PacketHandler {

	/**
	 * Handles a Packet.<br>
	 * <br>
	 * Sending a packet to the out stream is equivalent to returning a packet.<br>
	 * 
	 * @param packet the Packet
	 * @param out the MCOutputStream in the forwarded direction
	 * @param ret the MCOutputStream in the return direction
	 * @return the Packet to forward, or null to send no packet
	 * @throws IOException throwing an exception will close both bridges
	 */
	public Packet handle(Packet packet, MCOutputStream out, MCOutputStream ret) throws IOException {
		return packet;
	}
	
}