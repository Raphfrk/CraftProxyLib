package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldLong extends FieldFixedLength {
	
	public FieldLong() {
		super(8);
	}

	public static long readLong(InputStream in, byte[] buffer) throws IOException {
		fillBuffer(buffer, in);
		
		return
				((buffer[0])        << 56) |
				((buffer[1] & 0xFF) << 48) |
				((buffer[2] & 0xFF) << 40) |
				((buffer[3] & 0xFF) << 32) |
				((buffer[4] & 0xFF) << 24) |
				((buffer[5] & 0xFF) << 16) |
				((buffer[6] & 0xFF) << 8) |
				((buffer[7] & 0xFF) << 0);
				
	}

	@Override
	public Long read(InputStream in) throws IOException {
		return readLong(in, buffer);
	}
	
	public static int writeLong(byte[] buf, int pos, long i) {
		pos += 8;
		if (pos <= buf.length) {
			buf[pos - 8] = (byte)(i >> 56);
			buf[pos - 7] = (byte)(i >> 48);
			buf[pos - 6] = (byte)(i >> 40);
			buf[pos - 5] = (byte)(i >> 32);
			buf[pos - 4] = (byte)(i >> 24);
			buf[pos - 3] = (byte)(i >> 16);
			buf[pos - 2] = (byte)(i >> 8);
			buf[pos - 1] = (byte)(i >> 0);
		}
		return pos;
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeLong(buf, pos, (Long)i);
	}

}
