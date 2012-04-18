package com.raphfrk.craftproxylib.packet.standard;

import java.io.IOException;

import com.raphfrk.craftproxylib.fields.elements.FieldByte;
import com.raphfrk.craftproxylib.fields.elements.FieldInteger;
import com.raphfrk.craftproxylib.fields.elements.FieldShort;
import com.raphfrk.craftproxylib.fields.elements.FieldString;
import com.raphfrk.craftproxylib.login.LoginInfo;
import com.raphfrk.craftproxylib.packet.Packet;

public class RespawnPacket extends Packet {
	
	private FieldString fString = new FieldString();
	private FieldInteger fInt = new FieldInteger();
	private FieldByte fByte = new FieldByte(false);
	private FieldShort fShort = new FieldShort();
	
	public RespawnPacket(LoginInfo info, boolean invertDimension) throws IOException {
		this(convert(info.getDimension(), invertDimension), info.getDifficulty(), (byte)0, (short)256, info.getLevelType());
	}
	
	private static int convert(int dimension, boolean invertDimension) {
		if (invertDimension) {
			return dimension == 0 ? 1 : 0;
		} else 
			return dimension;
	}

	public RespawnPacket(int dimension, byte difficulty, byte creative, short height, String levelType) throws IOException {
		writeField(fByte, (Byte)(byte)0x09);
		writeField(fInt, (Integer)dimension);
		writeField(fByte, (Byte)(byte)difficulty);
		writeField(fByte, (Byte)(byte)creative);
		writeField(fShort, (Short)height);
		writeField(fString, (String)levelType);
		setDone();
	}
	
}
