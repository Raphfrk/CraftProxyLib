package main.java.com.raphfrk.protocol.packet.standard;

import java.io.IOException;

import main.java.com.raphfrk.protocol.fields.elements.FieldByte;
import main.java.com.raphfrk.protocol.fields.elements.FieldString;
import main.java.com.raphfrk.protocol.packet.Packet;

public class KickPacket extends Packet {
	
	private FieldString fString = new FieldString();
	private FieldByte fByte = new FieldByte(false);

	public KickPacket(String message) throws IOException {
		writeField(fByte, (Byte)(byte)0xFF);
		writeField(fString, message);
		setDone();
	}
	
}
