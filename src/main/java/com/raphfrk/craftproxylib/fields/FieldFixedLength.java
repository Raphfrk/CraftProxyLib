package com.raphfrk.craftproxylib.fields;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a fixed length field
 */
public class FieldFixedLength extends Field {

	private final int length;
	
	public FieldFixedLength(int length) {
		this.length = length;
	}

	@Override
	public int getFixedLength() {
		return length;
	}

	@Override
	public int skip(InputStream in) throws IOException {
		return (int)in.skip(length);
	}

	@Override
	public Object read(InputStream in) throws IOException {
		return null;
	}

}
