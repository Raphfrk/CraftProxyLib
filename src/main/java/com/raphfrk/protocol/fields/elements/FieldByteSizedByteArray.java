package main.java.com.raphfrk.protocol.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import main.java.com.raphfrk.protocol.fields.Field;

public class FieldByteSizedByteArray extends Field {
	
	private FieldByte fByte = new FieldByte(true);
	
	@Override
	public int skip(InputStream in) throws IOException {
		int length = fByte.readByte(in);
		
		return 1 + (int)in.skip(length);
	}

	@Override
	public byte[] read(InputStream in) throws IOException {
		int length = fByte.readByte(in);
		
		byte[] buf = new byte[length];
		int i = 0;
		while (i < length) {
			int b = in.read(buf, i, length - i);
			if (b == -1) {
				throw new EOFException("EOF Reached");
			}
			i += b;
		}
		return buf;
	}


}