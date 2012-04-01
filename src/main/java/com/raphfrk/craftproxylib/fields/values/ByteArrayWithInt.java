package com.raphfrk.craftproxylib.fields.values;

public class ByteArrayWithInt implements FieldValue {
	private int i;
	private byte[] buf;
	
	public ByteArrayWithInt(int i, byte[] buf) {
		this.i = i;
		this.buf = buf;
	}
	
	public void set(int i, byte[] buf) {
		this.i = i;
		this.buf = buf;
	}
	
	public int getInt() {
		return i;
	}
	
	public byte[] getBuf() {
		return buf;
	}
}
