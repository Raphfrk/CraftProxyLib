package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldFloat extends FieldFixedLength {

	public FieldFloat() {
		super(4);
	}

	public static float readFloat(InputStream in) throws IOException {
		
		int intValue = FieldInteger.readInt(in);

		return Float.intBitsToFloat(intValue);
	}

	@Override
	public Float read(InputStream in) throws IOException {
		return readFloat(in);
	}
	
	public static int writeFloat(byte[] buf, int pos, float i) {
		return FieldInteger.writeInt(buf, pos, Float.floatToRawIntBits(i));
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return writeFloat(buf, pos, (Float)i);
	}

}
