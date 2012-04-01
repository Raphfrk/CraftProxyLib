package com.raphfrk.craftproxylib.packet;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.raphfrk.craftproxylib.fields.Field;

/**
 * A packet associated with the protocol
 */
public class Packet extends InputStream {
	
	/**
	 * Byte buffer for the packet
	 */
	private byte[] buf;
	
	/**
	 * The number of valid bytes in the byte array
	 */
	private int count;
	
	/**
	 * The current read position in the byte array
	 */
	private int pos;
	
	/**
	 * Indicates that the Packet is a full packet
	 */
	private boolean done;
	
	private InputStream in;
	
	public Packet() {
		this(500);
	}
	
	/**
	 * Creates a packet with a buffer of size.  This does not prevent the buffer from being expanded as required.  However, the reset method reduces the size of the array to this size if it is larger.
	 * 
	 * @param size the buffer size
	 * @param maxSize
	 */
	public Packet(int size) {
		buf = new byte[size];
		count = 0;
		pos = 0;
		done = false;
	}
	
	/**
	 * Writes the Packet to an output stream
	 * 
	 * @param out the output stream
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		out.write(buf, 0, count);
	}
	
	/**
	 * Sets the input stream associated with this packet
	 * 
	 * @param in the InputStream
	 */
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	/**
	 * Reads a byte from the Packet.  The Packet can be safely rewound after the underlying stream throws a SocketTimeoutException.
	 */
	@Override
	public int read() throws IOException {
		if (pos == count) {
			if (count >= buf.length) {
				expandBuffer();
			}
			int b = in.read();

			if (b >= 0) {
				buf[count++] = (byte)b;
				pos++;
			}
			return b;
		} else {
			return buf[pos++];
		}
	}
	
	/**
	 * Reads a length byte from the Packet.  The Packet can be safely rewound after the underlying stream throws a SocketTimeoutException.
	 */
	@Override
	public int read(byte[] target, int off, int length) throws IOException {
		
		if (length < 0) {
			throw  new IllegalArgumentException("Length must be positive");
		}
		
		int end = pos + length;
		
		readEnoughFor(length);
		
		while (pos < end) {
			target[off++] = buf[pos++];
		}
		
		return length;
		
	}
	
	/**
	 * Skips skip bytes
	 */
	@Override
	public long skip(long skip) throws IOException {
		
		if (skip < 0) {
			throw  new IllegalArgumentException("Skip must be positive");
		}
		
		readEnoughFor((int)skip);
		
		pos += skip;
		
		return skip;
	}
	
	private void readEnoughFor(int length) throws IOException {
		
		int end = pos + length;
		
		expandBuffer(pos + length);
		
		while (count < end) {
			int b = in.read(buf, count, end - count);
			if (b == -1) {
				throw new EOFException("End of file");
			} else {
				count += b;
			}
		}
	}
	
	@Override
	public void reset() {
		pos = 0;
	}
	
	@Override
	public void mark(int readAheadLimit) {
		pos = 0;
		count = 0;
		if (buf.length > readAheadLimit) {
			buf = new byte[readAheadLimit];
		}
	}
	
	/**
	 * Gets the id for the Packet
	 * 
	 * @return the id or -1 if the first byte has not been read
	 */
	public int getId() {
		return (count == 0) ? (-1) : (buf[0] & 0xFF);
	}
	
	public void setDone() {
		this.done = true;
	}
	
	private void expandBuffer() {
		expandBuffer(count + 1);
	}
	
	private void expandBuffer(int target) {
		if (target > buf.length) {
			int newSize = Math.max(target, buf.length + (buf.length >> 1));
			byte[] newBuf = new byte[newSize];
			for (int i = 0; i < count; i++) {
				newBuf[i] = buf[i];
			}
			buf = newBuf;
		}
	}
	
	public byte[] getBuffer() {
		return buf;
	}
	
	public int getOff() {
		return pos;
	}
	
	public int getLength() {
		return count;
	}

	@Override
	public String toString() {
		int oldPos = pos;
		int oldCount = count;
		reset();
		StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "{");
		if (!done) {
			sb.append("Incomplete}");
			return sb.toString();
		}
		int id = getId();
		sb.append("id = " + id);
		
		pos = 1;

		if (id != -1) {
			Field[] fields = Field.getCompressedFields(id);
			for (Field f : fields) {
				sb.append(", ");
				try {
					sb.append(f.read(this));
				} catch (IOException e) {
					sb.append("error");
				}
			}
		}
		
		sb.append("}");
		pos = oldPos;
		if (count != oldCount) {
			throw new IllegalStateException("Buffer size changed when converting packet to String");
		}
		return sb.toString();
	}
	
	public void writeField(Field field, Object value) throws IOException {
		if (pos != count) {
			throw new IllegalStateException("Writing a field to a packet is only allowed if the position counter is at the end of the packet");
		}
		
		boolean success = false;
		while (!success) {
			int newPos = field.write(buf, pos, value);
			if (newPos <= buf.length) {
				pos = newPos;
				count = newPos;
				success = true;
			} else {
				expandBuffer(newPos);
			}
		}
	}
}
