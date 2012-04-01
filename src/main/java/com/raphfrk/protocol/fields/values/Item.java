package main.java.com.raphfrk.protocol.fields.values;

public class Item implements FieldValue {

	private short id;
	private byte count;
	private short damage;
	private byte[] enchant;
	
	public Item(short id) {
		this(id, (byte)0, (short)0);
	}
	
	public Item(short id, byte count, short damage) {
		this(id, count, damage, null);
	}
	
	public Item(short id, byte count, short damage, byte[] enchant) {
		this.id = id;
		this.count = count;
		this.damage = damage;
		this.enchant = enchant;
	}
	
	public short getId() {
		return id;
	}
	
	public void setId(short id) {
		this.id = id;
	}
	
	public short getCount() {
		return count;
	}
	
	public void setCount(byte count) {
		this.count = count;
	}
	
	public short getDamage() {
		return damage;
	}
	
	public void setDamage(short damage) {
		this.damage = damage;
	}
}
