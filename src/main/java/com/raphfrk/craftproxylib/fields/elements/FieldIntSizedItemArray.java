package com.raphfrk.craftproxylib.fields.elements;

import java.io.IOException;
import java.io.InputStream;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.fields.values.Item;

public class FieldIntSizedItemArray extends Field {
	
	private FieldItem fItem = new FieldItem();

	@Override
	public int skip(InputStream in) throws IOException {
		int length = FieldShort.readShort(in) & 0xFFFF;
		
		int skipped = 4;
		for (int i = 0; i < length; i++) {
			skipped += fItem.skip(in);
		}
		
		return skipped;
	}

	@Override
	public Item[] read(InputStream in) throws IOException {
		int length = FieldShort.readShort(in) & 0xFFFF;

		Item[] itemArray = new Item[length];
		
		for (int i = 0; i < length; i++) {
			itemArray[i] = fItem.read(in);
		}
		
		return itemArray;
	}


}
