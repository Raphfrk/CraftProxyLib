package com.raphfrk.craftproxylib.packet.standard;

import java.io.IOException;

import com.raphfrk.craftproxylib.fields.elements.FieldByte;
import com.raphfrk.craftproxylib.fields.elements.FieldString;
import com.raphfrk.craftproxylib.packet.Packet;

public class KickPacket extends Packet {
	
	private FieldString fString = new FieldString();
	private FieldByte fByte = new FieldByte(false);

	public KickPacket(String message) throws IOException {
		writeField(fByte, (Byte)(byte)0xFF);
		writeField(fString, message);
		setDone();
	}
	
}
