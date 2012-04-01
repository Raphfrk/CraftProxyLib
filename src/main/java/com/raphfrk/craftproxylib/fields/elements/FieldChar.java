package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldChar extends FieldFixedLength {

	public FieldChar() {
		super(2);
	}

	public static char readCharacter(InputStream in, byte[] buffer) throws IOException {
		return (char)FieldShort.readShort(in, buffer);
	}

	@Override
	public Character read(InputStream in) throws IOException {
		return readCharacter(in, buffer);
	}
	
	public static int writeCharacter(byte[] buf, int pos, char i) {
		pos += 2;
		if (pos <= buf.length) {
			buf[pos - 2] = (byte)(i >> 8);
			buf[pos - 1] = (byte)(i >> 0);
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeCharacter(buf, pos, (Character)i);
	}

}
