package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldShort extends FieldFixedLength {

	public FieldShort() {
		super(2);
	}

	public static short readShort(InputStream in, byte[] buffer) throws IOException {
		fillBuffer(buffer, in);
		
		return (short)((buffer[0] << 8) | (buffer[1] & 0xFF));
	}

	@Override
	public Short read(InputStream in) throws IOException {
		return readShort(in, buffer);
	}
	
	public static int writeShort(byte[] buf, int pos, short i) {
		pos += 2;
		if (pos <= buf.length) {
			buf[pos - 2] = (byte)(i >> 8);
			buf[pos - 1] = (byte)(i >> 0);
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeShort(buf, pos, (Short)i);
	}
	


}
