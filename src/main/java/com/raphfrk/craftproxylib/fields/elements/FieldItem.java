package com.raphfrk.craftproxylib.fields.elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.fields.values.Item;

public class FieldItem extends Field {
	
	public static boolean[] enchanted;

	static {
		
		HashSet<Short> enchantedItemsIds = new HashSet<Short>();
		
		enchantedItemsIds.add((short)0x100);
		enchantedItemsIds.add((short)0x101);
		enchantedItemsIds.add((short)0x102);
		enchantedItemsIds.add((short)0x103);
		enchantedItemsIds.add((short)0x105);
		enchantedItemsIds.add((short)0x10B);
		enchantedItemsIds.add((short)0x10C);
		enchantedItemsIds.add((short)0x10D);
		enchantedItemsIds.add((short)0x10E);
		enchantedItemsIds.add((short)0x10F);
		enchantedItemsIds.add((short)0x110);
		enchantedItemsIds.add((short)0x111);
		enchantedItemsIds.add((short)0x112);
		enchantedItemsIds.add((short)0x113);
		enchantedItemsIds.add((short)0x114);
		enchantedItemsIds.add((short)0x115);
		enchantedItemsIds.add((short)0x116);
		enchantedItemsIds.add((short)0x117);
		enchantedItemsIds.add((short)0x11B);
		enchantedItemsIds.add((short)0x11C);
		enchantedItemsIds.add((short)0x11D);
		enchantedItemsIds.add((short)0x11E);
		enchantedItemsIds.add((short)0x122);
		enchantedItemsIds.add((short)0x123);
		enchantedItemsIds.add((short)0x124);
		enchantedItemsIds.add((short)0x125);
		enchantedItemsIds.add((short)0x126);
		enchantedItemsIds.add((short)0x12A);
		enchantedItemsIds.add((short)0x12B);
		enchantedItemsIds.add((short)0x12C);
		enchantedItemsIds.add((short)0x12D);
		enchantedItemsIds.add((short)0x12E);
		enchantedItemsIds.add((short)0x12F);
		enchantedItemsIds.add((short)0x130);
		enchantedItemsIds.add((short)0x131);
		enchantedItemsIds.add((short)0x132);
		enchantedItemsIds.add((short)0x133);
		enchantedItemsIds.add((short)0x134);
		enchantedItemsIds.add((short)0x135);
		enchantedItemsIds.add((short)0x136);
		enchantedItemsIds.add((short)0x137);
		enchantedItemsIds.add((short)0x138);
		enchantedItemsIds.add((short)0x139);
		enchantedItemsIds.add((short)0x13A);
		enchantedItemsIds.add((short)0x13B);
		enchantedItemsIds.add((short)0x13C);
		enchantedItemsIds.add((short)0x13D);
		enchantedItemsIds.add((short)0x15A);
		enchantedItemsIds.add((short)0x167);
		
		enchanted = new boolean[0x10000];
		for (int i = 0; i < enchanted.length; i++) {
			enchanted[i] = enchantedItemsIds.contains(i);
		}
	}

	@Override
	public int skip(InputStream in) throws IOException {
		int id = FieldShort.readShort(in);
		
		if (id >= 0) {
			int count = in.read();
			if (count == -1) {
				throw new EOFException("EOF reached");
			}
			
			FieldShort.readShort(in);
			
			if (enchanted[id]) {
				int length = FieldShort.readShort(in);
				in.skip(length);
				return length + 7;
			} else {
				return 5;
			}
		} else {
			return 2;
		}
	}

	@Override
	public Item read(InputStream in) throws IOException {
		short id = FieldShort.readShort(in);
		
		if (id >= 0) {
			int count = in.read();
			if (count == -1) {
				throw new EOFException("EOF reached");
			}
			
			short damage = FieldShort.readShort(in);
			
			if (enchanted[id]) {
				int length = FieldShort.readShort(in);
				byte[] enchant = new byte[length];
				int i = 0;
				while (i < length) {
					int b = in.read(enchant, i, length - i);
					if (b == -1) {
						throw new EOFException("EOF reached");
					}
					i += b;
				}
				return new Item(id, (byte)count, damage, enchant);
			} else {
				return new Item(id, (byte)count, damage);
			}
		} else {
			return new Item(id);
		}	
	}

}
