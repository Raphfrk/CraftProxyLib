package main.java.com.raphfrk.protocol.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import main.java.com.raphfrk.protocol.fields.FieldFixedLength;

public class FieldBoolean extends FieldFixedLength {
	
	public FieldBoolean() {
		super(1);
	}

	public static boolean readBoolean(InputStream in) throws IOException {
		int r = in.read();

		if (r == -1) {
			throw new EOFException("EOF reached");
		}
		
		return r != 0;
	}

	@Override
	public Boolean read(InputStream in) throws IOException {
		return readBoolean(in);
	}
	
	public static int writeBoolean(byte[] buf, int pos, boolean i) {
		pos += 1;
		if (pos <= buf.length) {
			buf[pos - 1] = i ? (byte)1 : (byte)0;
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeBoolean(buf, pos, (Boolean)i);
	}

}
