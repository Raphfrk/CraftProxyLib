package com.raphfrk.craftproxylib.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldByte extends FieldFixedLength {
	
	private boolean unsigned;
	
	public FieldByte(boolean unsigned) {
		super(1);
		this.unsigned = unsigned;
	}
	
	@Override
	public FieldByte newInstance() {
		return new FieldByte(unsigned);
	}
	
	public int readByte(InputStream in) throws IOException {
		int r = in.read();

		if (r == -1) {
			throw new EOFException("EOF reached");
		}
		
		if (unsigned) {
			return r & 0xFF;
		} else {
			return (int)((byte)r);
		}
	}

	@Override
	public Byte read(InputStream in) throws IOException {
		return (byte)readByte(in);
	}
	
	public static int writeByte(byte[] buf, int pos, byte i) {
		pos += 1;
		if (pos <= buf.length) {
			buf[pos - 1] = (byte)(i >> 0);
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeByte(buf, pos, (Byte)i);
	}

}
