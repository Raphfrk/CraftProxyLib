package main.java.com.raphfrk.protocol;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import main.java.com.raphfrk.protocol.packet.Packet;
import main.java.com.raphfrk.protocol.packet.standard.KickPacket;

/**
 * Represents a class that can send Packets from to an OutputStream.<br>
 * <br>
 * This methods in this class are synchronised.
 */
public class MCOutputStream extends FilterOutputStream {

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
}
