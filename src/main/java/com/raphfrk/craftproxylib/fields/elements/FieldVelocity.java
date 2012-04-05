package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.fields.values.Velocity;

public class FieldVelocity extends Field {

	@Override
	public int skip(InputStream in) throws IOException {
		int eid = FieldInteger.readInt(in);
		
		if (eid != 0) {
			in.skip(6);
			return 10;
		} else {
			return 4;
		}
	}

	@Override
	public Velocity read(InputStream in) throws IOException {
		int eid = FieldInteger.readInt(in);

		if (eid != 0) {
			short vx = FieldShort.readShort(in);
			short vy = FieldShort.readShort(in);
			short vz = FieldShort.readShort(in);
			return new Velocity(eid, vx, vy, vz);
		} else {
			return new Velocity(eid);
		}
	}
	
}
