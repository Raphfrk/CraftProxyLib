package com.raphfrk.craftproxylib.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;

public class FieldIntSizedByteArray extends Field {
	
	@Override
	public FieldIntSizedByteArray newInstance() {
		return new FieldIntSizedByteArray();
	}

	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldInteger.readInt(in);
		
		return 4 + (int)in.skip(length);
	}

	@Override
	public byte[] read(InputStream in) throws IOException {
		int length = FieldInteger.readInt(in);
		
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
