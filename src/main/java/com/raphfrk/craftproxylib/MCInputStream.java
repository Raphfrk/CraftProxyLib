package com.raphfrk.craftproxylib;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.fields.elements.FieldInteger;
import com.raphfrk.craftproxylib.packet.Packet;
import com.raphfrk.craftproxylib.util.ArrayFIFO;

/**
 * Represents a class that can receive Packets from an InputStream.<br>
 * <br>
 * NOTE: While this class supports the SocketTimeoutException, many InputStream sub-classes don't.  To support reading after a timeout, the InputStream passed to the class should be the InputStream directly from a Socket.
 */
public class MCInputStream extends FilterInputStream {

	/**
	 * This stores a Packet that is half read due to a SocketTimeoutException occurring.
	 */
	private Packet packet;
	
	private final int packetSize;
	
	private final ArrayFIFO packetIds = new ArrayFIFO(50);
	
	private final AtomicReference<byte[]> clientEntityId = new AtomicReference<byte[]>(null);
	private final AtomicReference<byte[]> serverEntityId = new AtomicReference<byte[]>(null);
	private final AtomicBoolean entityIdMapping = new AtomicBoolean();
	
	private int dataReceived = 0;
	
	protected MCInputStream(InputStream in) {
		this(in, 500);
	}

	protected MCInputStream(InputStream in, int packetSize) {
		super(in);
		this.packet = null;
		this.packetSize = packetSize;
	}
	
	/**
	 * Reads a Packet from the stream.
	 * 
	 * @param packet the packet
	 * @return the Packet read from the stream
	 * @throws SocketTimeoutException thrown if the stream times out when reading
	 * @throws IOException
	 */
	public Packet readPacket() throws IOException {
		return readPacket(null);
	}
	
	/**
	 * Reads a Packet from the stream.  If a Packet is provided, it may be used in preference to creating a new Packet object.
	 * 
	 * @param packet the packet
	 * @return the Packet read from the stream
	 * @throws SocketTimeoutException thrown if the stream times out when reading
	 * @throws IOException
	 */
	public Packet readPacket(Packet p) throws SocketTimeoutException, IOException {
		if (packet == null) {
			if (p == null) {
				packet = new Packet();
			} else {
				packet = p;
				p.mark(packetSize);
			}
			packet.setInputStream(this);
		} else {
			packet.reset();
			packet.setInputStream(this);
		}
		
		int id = packet.read();
		
		if (id < 0) {
			throw new EOFException("EOF reached");
		}
		packetIds.write((byte)id);
		
		Field[] fields = Field.getCompressedFields(id);
		
		if (fields == null) {
			printRecentPacketIds();
			throw new IOException("Unknown packet id 0x" + Integer.toHexString(id));
		}
		
		for (int f = 0; f < fields.length; f++) {
			try {
				fields[f].skip(packet);
			} catch (IllegalArgumentException iae) {
				throw iae;
			}
		}
		
		if (id >= 0 && this.entityIdMapping.get()) {
			packet.swapEntityId(clientEntityId.get(), serverEntityId.get());
		}
		
		Packet r = packet;
		
		r.setDone();
		
		this.dataReceived += r.getLength();
		
		packet = null;
		
		return r;
	}
	
	public void close() throws IOException {
		throw new IllegalStateException("Use MCSocket.close() to close streams");
	}
	
	public void printRecentPacketIds() {
		int[] oldIds = packetIds.read();
		CraftProxyLib.log("Packet ids: " + Arrays.toString(oldIds));
	}
	
	public void setServerEntityId(int entityId) {
		byte[] newEntityId = new byte[4];
		FieldInteger.writeInt(newEntityId, 0, entityId);
		this.serverEntityId.set(newEntityId);
	}
	
	public void setClientEntityId(int entityId) {
		byte[] newEntityId = new byte[4];
		FieldInteger.writeInt(newEntityId, 0, entityId);
		this.clientEntityId.set(newEntityId);
	}
	
	public int getDataReceived() {
		return this.dataReceived;
	}

}
