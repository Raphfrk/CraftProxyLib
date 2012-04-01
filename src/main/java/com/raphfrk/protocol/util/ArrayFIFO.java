package main.java.com.raphfrk.protocol.util;


public class ArrayFIFO {
	
	private int write = 0;
	private final int mask;
	private byte[] buf;
	
	public ArrayFIFO(int capacity) {
		int mask = capacity;
		mask |= mask >> 1;
		mask |= mask >> 2;
		mask |= mask >> 4;
		mask |= mask >> 8;
		mask |= mask >> 16;
		this.mask = mask;
		buf = new byte[mask + 1];
	}
	
	public void write(byte b) {
		buf[(write++) & mask] = b;
	}
	
	public byte[] read() {
		int len = Math.min(write, buf.length);
		byte[] read = new byte[len];
		for (int i = 0; i < len; i++){
			read[i] = buf[(write - len + i) & mask];
		}
		return read;
	}
	
}
