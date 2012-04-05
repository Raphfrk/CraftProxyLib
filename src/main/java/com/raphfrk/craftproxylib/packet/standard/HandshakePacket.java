package com.raphfrk.craftproxylib.packet.standard;

import java.io.IOException;

import com.raphfrk.craftproxylib.fields.elements.FieldByte;
import com.raphfrk.craftproxylib.fields.elements.FieldInteger;
import com.raphfrk.craftproxylib.fields.elements.FieldString;
import com.raphfrk.craftproxylib.packet.Packet;

public class HandshakePacket extends Packet {
	
	private FieldString fString = new FieldString();
	private FieldByte fByte = new FieldByte(false);

	public HandshakePacket(String message) throws IOException {
		writeField(fByte, (Byte)(byte)0x02);
		writeField(fString, message);
		setDone();
	}
	
}
