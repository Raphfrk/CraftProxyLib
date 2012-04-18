package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.packet.Packet;

public class FieldString extends Field {

	@Override
	public FieldString newInstance() {
		return new FieldString();
	}
	
	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldShort.readShort(in);
		int bytes = length << 1;
		
		return 2 + (int)in.skip(bytes);
	}

	public static String readString(InputStream in) throws IOException {
		int length = FieldShort.readShort(in);
		
		StringBuilder sb = new StringBuilder(length);

		for(int cnt=0; cnt<length;cnt++) {
			sb.append((char)FieldShort.readShort(in));
		}
		return sb.toString();
	}
	
	@Override
	public String read(InputStream in) throws IOException {
		return readString(in);
	}
	
	public static int writeString(byte[] buf, int pos, String s) {
		pos = FieldShort.writeShort(buf, pos, (short)s.length());
		char[] chars = s.toCharArray();
		for (char c : chars) {
			pos = FieldChar.writeCharacter(buf, pos, c);
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object value) {
		return writeString(buf, pos, (String)value);
	}

}
