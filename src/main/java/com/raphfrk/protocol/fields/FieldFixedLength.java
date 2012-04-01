package main.java.com.raphfrk.protocol.fields;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a fixed length field
 */
public class FieldFixedLength extends Field {
	
	public FieldFixedLength(int length) {
		super(length);
	}

	@Override
	public int getFixedLength() {
		return buffer.length;
	}

	@Override
	public int skip(InputStream in) throws IOException {
		return (int)in.skip(buffer.length);
	}

	@Override
	public Object read(InputStream in) throws IOException {
		return null;
	}

}
