package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldInteger extends FieldFixedLength {

	@Override
	public FieldInteger newInstance() {
		return new FieldInteger();
	}
	
	public FieldInteger() {
		super(4);
	}

	public static int readInt(InputStream in) throws IOException {

		return
				(getByte(in) << 24) |
				(getByte(in) << 16) |
				(getByte(in) << 8) |
				(getByte(in) << 0);
			
	}

	@Override
	public Integer read(InputStream in) throws IOException {
		return readInt(in);
	}
	
	public static int writeInt(byte[] buf, int pos, int i) {
		pos += 4;
		if (pos <= buf.length) {
			buf[pos - 4] = (byte)(i >> 24);
			buf[pos - 3] = (byte)(i >> 16);
			buf[pos - 2] = (byte)(i >> 8);
			buf[pos - 1] = (byte)(i >> 0);
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeInt(buf, pos, (Integer)i);
	}

}
