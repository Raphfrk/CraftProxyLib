package com.raphfrk.craftproxylib.packet.standard;

import java.io.IOException;

import com.raphfrk.craftproxylib.fields.elements.FieldByte;
import com.raphfrk.craftproxylib.fields.elements.FieldInteger;
import com.raphfrk.craftproxylib.fields.elements.FieldString;
import com.raphfrk.craftproxylib.packet.Packet;

public class LoginPacket extends Packet {
	
	private FieldString fString = new FieldString();
	private FieldInteger fInt = new FieldInteger();
	private FieldByte fByte = new FieldByte(false);

	public LoginPacket(int protocolVersion, String username) throws IOException {
		this(protocolVersion, username, "", 0, 0, (byte)0, (byte)0, (byte)0);
	}
	
	public LoginPacket(int entityId, String username, String levelType, int serverMode, int dimension, byte difficulty, byte unused, byte maxPlayers) throws IOException {
		writeField(fByte, (Byte)(byte)0x01);
		writeField(fInt, (Integer)entityId);
		writeField(fString, (String)username);
		writeField(fString, (String)levelType);
		writeField(fInt, (Integer)serverMode);
		writeField(fInt, (Integer)dimension);
		writeField(fByte, (Byte)difficulty);
		writeField(fByte, (Byte)unused);
		writeField(fByte, (Byte)maxPlayers);		
		setDone();
	}
	
}
