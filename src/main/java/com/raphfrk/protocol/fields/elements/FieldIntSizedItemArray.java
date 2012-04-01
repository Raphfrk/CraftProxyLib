package main.java.com.raphfrk.protocol.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import main.java.com.raphfrk.protocol.fields.Field;
import main.java.com.raphfrk.protocol.fields.values.Item;

public class FieldIntSizedItemArray extends Field {
	
	private FieldItem fItem = new FieldItem();
	
	public FieldIntSizedItemArray() {
		super(2);
	}

	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldShort.readShort(in, buffer) & 0xFFFF;
		
		int skipped = 4;
		for (int i = 0; i < length; i++) {
			skipped += fItem.skip(in);
		}
		
		return skipped;
	}

	@Override
	public Item[] read(InputStream in) throws IOException {
		int length = FieldShort.readShort(in, buffer) & 0xFFFF;

		Item[] itemArray = new Item[length];
		
		for (int i = 0; i < length; i++) {
			itemArray[i] = fItem.read(in);
		}
		
		return itemArray;
	}


}
