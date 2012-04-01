package main.java.com.raphfrk.protocol;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import main.java.com.raphfrk.protocol.fields.Field;
import main.java.com.raphfrk.protocol.packet.Packet;
import main.java.com.raphfrk.protocol.util.ArrayFIFO;

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
			byte[] oldIds = packetIds.read();
			System.out.println("Packet ids: " + Arrays.toString(oldIds));
			throw new IOException("Unknown packet id 0x" + Integer.toHexString(id));
		}
		
		for (int f = 0; f < fields.length; f++) {
			fields[f].skip(packet);
		}
		
		Packet r = packet;
		
		r.setDone();
		
		packet = null;
		
		return r;
	}

}
