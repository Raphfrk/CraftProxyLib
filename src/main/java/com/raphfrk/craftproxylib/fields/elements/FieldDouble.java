package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldDouble extends FieldFixedLength {

	public FieldDouble() {
		super(8);
	}

	public static double readDouble(InputStream in, byte[] buffer) throws IOException {
		
		long longValue = FieldLong.readLong(in, buffer);

		return Double.longBitsToDouble(longValue);
	}

	@Override
	public Double read(InputStream in) throws IOException {
		return readDouble(in, buffer);
	}

	public static int writeDouble(byte[] buf, int pos, double i) {
		return FieldLong.writeLong(buf, pos, Double.doubleToRawLongBits(i));
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeDouble(buf, pos, (Double)i);
	}
	
}
