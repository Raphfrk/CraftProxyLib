package com.raphfrk.craftproxylib.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;

public class FieldShortSizedByteArray extends Field {
	
	@Override
	public FieldShortSizedByteArray newInstance() {
		return new FieldShortSizedByteArray();
	}
	
	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldShort.readShort(in);
		
		return 2 + (int)in.skip(length);
	}

	@Override
	public byte[] read(InputStream in) throws IOException {
		int length = FieldShort.readShort(in);
		
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
