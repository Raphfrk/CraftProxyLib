package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;

public class FieldString extends Field {
	
	public FieldString() {
		super(2);
	}

	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldShort.readShort(in, buffer);
		int bytes = length << 1;
		
		return 2 + (int)in.skip(bytes);
	}

	@Override
	public String read(InputStream in) throws IOException {
		int length = FieldShort.readShort(in, buffer);
		
		StringBuilder sb = new StringBuilder(length);

		for(int cnt=0; cnt<length;cnt++) {
			sb.append((char)FieldShort.readShort(in, buffer));
		}
		return sb.toString();
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
