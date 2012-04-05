package com.raphfrk.craftproxylib.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.raphfrk.craftproxylib.fields.Field;

public class FieldMetadata extends Field {

	private FieldByte fByte = new FieldByte(false);
	private FieldString fString = new FieldString();
	
	private byte[] shortBuf = new byte[2];
	
	@Override
	public int skip(InputStream in) throws IOException {
		int skipped = 0;
		int b = 0;
		while (b != 127) {
			b = in.read();
			if (b == -1) {
				throw new EOFException("EOF reached");
			} else if (b != 127) {
				skipped++;
				b = (b & 0xFF) >> 5;
				switch(b) {
				case 0: skipped += in.skip(1); break;
				case 1: skipped += in.skip(2); break;
				case 2: 
				case 3: skipped += in.skip(4); break;
				case 4: skipped += fString.skip(in); break;
				case 5: skipped += in.skip(5); break;
				case 6: skipped += in.skip(12); break;
				}
			}
		}
		return skipped;
	}

	@Override
	public Object[] read(InputStream in) throws IOException {
		ArrayList<Object> data = new ArrayList<Object>();
		
		int b = 0;
		while (b != 127) {
			b = in.read();
			if (b == -1) {
				throw new EOFException("EOF reached");
			}
			b = (b & 0xFF) >> 5;
			switch(b) {
			case 0: {
				data.add(fByte.readByte(in));
				break;
			}
			case 1: {
				data.add(FieldShort.readShort(in));
				break;
			}
			case 2: {
				data.add(FieldInteger.readInt(in));
				break;
			}
			case 3: {
				data.add(FieldFloat.readFloat(in));
				break;
			}
			case 4: {
				data.add(FieldString.readString(in));
				break;
			}
			case 5: {
				short aa = FieldShort.readShort(in);
				byte bb = (byte)fByte.readByte(in);
				short cc = FieldShort.readShort(in);
				
				data.add(new Object[] {aa , bb, cc});
				break;
			}
			case 6: {
				int aa = FieldInteger.readInt(in);
				int bb = FieldInteger.readInt(in);
				int cc = FieldInteger.readInt(in);
				
				data.add(new Object[] {aa , bb, cc});
				break;
			}
			default: {
				data.add(null);
			}
			}
		}
		return data.toArray();
	}
	
}
