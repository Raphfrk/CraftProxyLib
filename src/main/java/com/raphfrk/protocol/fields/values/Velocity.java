package main.java.com.raphfrk.protocol.fields.values;

public class Velocity implements FieldValue {

	private int eid;
	private short vx;
	private short vy;
	private short vz;
	
	public Velocity(int eid) {
		this(eid, (short)0, (short)0, (short)0);	
	}
	
	public Velocity(int eid, short vx, short vy, short vz) {
		this.eid = eid;
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
	}
	
	public int getEID() {
		return eid;
	}
	
	public short getVX() {
		return vx;
	}
	
	public short getVY() {
		return vy;
	}
	
	public short getVZ() {
		return vz;
	}
	
	public void set(int eid, short vx, short vy, short vz) {
		this.eid = eid;
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
	}
	
}
