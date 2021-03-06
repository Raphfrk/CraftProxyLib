package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;
import com.raphfrk.craftproxylib.packet.Packet;

public class FieldShort extends FieldFixedLength {
	
	@Override
	public FieldShort newInstance() {
		return new FieldShort();
	}

	public FieldShort() {
		super(2);
	}

	public static short readShort(InputStream in) throws IOException {
		return (short)((getByte(in) << 8) | getByte(in));
	}

	@Override
	public Short read(InputStream in) throws IOException {
		return readShort(in);
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
