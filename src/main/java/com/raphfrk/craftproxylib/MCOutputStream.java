package com.raphfrk.craftproxylib;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.raphfrk.craftproxylib.fields.elements.FieldInteger;
import com.raphfrk.craftproxylib.packet.Packet;
import com.raphfrk.craftproxylib.packet.standard.KickPacket;

/**
 * Represents a class that can send Packets from to an OutputStream.<br>
 * <br>
 * This methods in this class are synchronised.
 */
public class MCOutputStream extends FilterOutputStream {
	
	private final AtomicReference<byte[]> clientEntityId = new AtomicReference<byte[]>(null);
	private final AtomicReference<byte[]> serverEntityId = new AtomicReference<byte[]>(null);
	private final AtomicBoolean entityIdMapping = new AtomicBoolean();

	public MCOutputStream(OutputStream out) {
		super(out);
	}

	/**
	 * Writes the Packet to the OutputStream
	 * 
	 * @param packet the Packet
	 * @throws IOException
	 */
	public synchronized void writePacket(Packet packet) throws IOException {
		packet.reset();

		byte[] buf = packet.getBuffer();
		int off = packet.getOff();
		int length = packet.getLength();
		
		int id = packet.getId();
		if (id >= 0 && this.entityIdMapping.get()) {
			packet.swapEntityId(clientEntityId.get(), serverEntityId.get());
		}
		
		out.write(buf, off, length - off);
	}
	
	public synchronized void close(String message) throws IOException {
		writePacket(new KickPacket(message));
		super.flush();
		super.close();
	}
	
	public synchronized void flush() throws IOException {
		super.flush();
	}
	
	public synchronized void write(int b) throws IOException {
		super.write(b);
	}
	
	public synchronized void write(byte[] b) throws IOException {
		super.write(b);
	}
	
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
	}
	
	public void setServerEntityId(int entityId) {
		byte[] newEntityId = new byte[4];
		FieldInteger.writeInt(newEntityId, 0, entityId);
		this.serverEntityId.set(newEntityId);
		this.entityIdMapping.set(true);
	}
	
	public void setClientEntityId(int entityId) {
		byte[] newEntityId = new byte[4];
		FieldInteger.writeInt(newEntityId, 0, entityId);
		this.clientEntityId.set(newEntityId);
		this.entityIdMapping.set(true);
	}
}
