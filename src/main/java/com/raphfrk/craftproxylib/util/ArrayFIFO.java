package com.raphfrk.craftproxylib.util;


public class ArrayFIFO {
	
	private int write = 0;
	private final int mask;
	private int[] buf;
	
	public ArrayFIFO(int capacity) {
		int mask = capacity;
		mask |= mask >> 1;
		mask |= mask >> 2;
		mask |= mask >> 4;
		mask |= mask >> 8;
		mask |= mask >> 16;
		this.mask = mask;
		buf = new int[mask + 1];
	}
	
	public void write(byte b) {
		buf[(write++) & mask] = b & 0xFF;
	}
	
	public int[] read() {
		int len = Math.min(write, buf.length);
		int[] read = new int[len];
		for (int i = 0; i < len; i++){
			read[i] = buf[(write - len + i) & mask];
		}
		return read;
	}
	
}
