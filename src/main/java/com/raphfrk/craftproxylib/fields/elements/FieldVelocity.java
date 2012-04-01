package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.fields.values.Velocity;

public class FieldVelocity extends Field {

	byte[] shortBuffer = new byte[2];
	
	public FieldVelocity() {
		super(4);
	}

	@Override
	public int skip(InputStream in) throws IOException {
		int eid = FieldInteger.readInt(in, buffer);
		
		if (eid != 0) {
			in.skip(6);
			return 10;
		} else {
			return 4;
		}
	}

	@Override
	public Velocity read(InputStream in) throws IOException {
		int eid = FieldInteger.readInt(in, buffer);

		if (eid != 0) {
			short vx = FieldShort.readShort(in, shortBuffer);
			short vy = FieldShort.readShort(in, shortBuffer);
			short vz = FieldShort.readShort(in, shortBuffer);
			return new Velocity(eid, vx, vy, vz);
		} else {
			return new Velocity(eid);
		}
	}
	
}
