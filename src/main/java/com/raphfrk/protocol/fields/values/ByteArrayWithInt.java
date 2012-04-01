package main.java.com.raphfrk.protocol.fields.values;

public class ByteArrayWithInt {
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
