package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.FieldFixedLength;

public class FieldEntityId extends FieldFixedLength {
	
	public FieldEntityId() {
		super(4);
	}

	@Override
	public Integer read(InputStream in) throws IOException {
		return FieldInteger.readInt(in, buffer);
	}
	
	@Override
	public int write(byte[] buf, int pos, Object i) {
		return FieldInteger.writeInt(buf, pos, (Integer)i);
	}
	
	public static void swapEntityId(byte[] buf, int pos, byte[] id1, byte[] id2) {
		if (
				buf[pos + 0] == id1[0] && 
				buf[pos + 1] == id1[1] && 
				buf[pos + 2] == id1[2] && 
				buf[pos + 3] == id1[3]) {
			for (int i = 0; i < 4; i++) {
				buf[i + pos] = id2[i];
			}
			return;
		}
		if (
				buf[pos + 0] == id2[0] && 
				buf[pos + 1] == id2[1] && 
				buf[pos + 2] == id2[2] && 
				buf[pos + 3] == id2[3]) {
			for (int i = 0; i < 4; i++) {
				buf[i + pos] = id1[i];
			}
			return;
		}
	}

}
