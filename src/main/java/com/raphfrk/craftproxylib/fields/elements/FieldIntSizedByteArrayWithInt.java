package com.raphfrk.craftproxylib.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.fields.values.ByteArrayWithInt;

public class FieldIntSizedByteArrayWithInt extends Field {

	@Override
	public FieldIntSizedByteArrayWithInt newInstance() {
		return new FieldIntSizedByteArrayWithInt();
	}
	
	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldInteger.readInt(in);
		in.skip(4);
		
		return 8 + (int)in.skip(length);
	}

	@Override
	public ByteArrayWithInt read(InputStream in) throws IOException {
		int length = FieldInteger.readInt(in);

		int d = FieldInteger.readInt(in);
		
		byte[] buf = new byte[length];
		int i = 0;
		while (i < length) {
			int b = in.read(buf, i, length - i);
			if (b == -1) {
				throw new EOFException("EOF Reached");
			}
			i += b;
		}
		return new ByteArrayWithInt(d, buf);
	}


}
