package com.raphfrk.craftproxylib;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.raphfrk.craftproxylib.fields.Field;
import com.raphfrk.craftproxylib.packet.Packet;
import com.raphfrk.craftproxylib.packet.standard.KickPacket;


/**
 * Represents a Socket that implements the protocol.<br>
 * <br>
 * If the socket is in server mode, it will send ping packets to the client at every timeout/2 seconds and verify the response.<br>
 * <br>
 * When in client mode, it will respond to all ping packets received.
 */
public class MCSocket {

	protected final boolean server;
	protected final Socket socket;
	protected final MCInputStream in;
	protected final MCOutputStream out;
	protected final String message;
	
	private static final int DEFAULT_TIMEOUT = 60000;
	
	/**
	 * Creates a MCSocket
	 * 
	 * @param socket the underlying Socket
	 * @param server true if MCSocket is to act like a server
	 * @throws IOException 
	 */
	public MCSocket(Socket socket, boolean server) throws IOException {
		this(socket, server, DEFAULT_TIMEOUT);
	}
	
	/**
	 * Creates a MCSocket
	 * 
	 * @param socket the underlying Socket
	 * @param server true if MCSocket is to act like a server
	 * @param message the message sent when closing the stream
	 * @throws IOException 
	 */
	public MCSocket(Socket socket, boolean server, String message) throws IOException {
		this(socket, server, DEFAULT_TIMEOUT, message);
	}
	
	/**
	 * Creates a MCSocket
	 * 
	 * @param socket the underlying Socket
	 * @param server true if MCSocket is to act like a server
	 * @param timeout the connection timeout in seconds
	 * @throws IOException 
	 */
	public MCSocket(Socket socket, boolean server, int timeout) throws IOException {
		this(socket, server, timeout, null);
	}
	
	/**
	 * Creates a MCSocket
	 * 
	 * @param socket the underlying Socket
	 * @param server true if MCSocket is to act like a server
	 * @param timeout the connection timeout in milliseconds
	 * @param message the message sent when closing the stream
	 * @throws IOException 
	 */
	public MCSocket(Socket socket, boolean server, int timeout, String message) throws IOException {
		try {
			socket.setSoTimeout(timeout);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = socket.getInputStream();
		} catch (IOException ioe) {
			socket.close();
			throw new IOException("Unable to open input stream", ioe);
		}
		try {
			out = socket.getOutputStream();
		} catch (IOException ioe) {
			in.close();
			socket.close();
			throw new IOException("Unable to open output stream", ioe);
		}
		this.in = new MCInputStream(in);
		this.out = new MCOutputStream(out);
		this.socket = socket;
		this.server = server;
		if (message == null) {
			this.message = "MCSocket Stream Closed";
		} else {
			this.message = message;
		}
	}
	
	/**
	 * Reads a Packet from the Socket
	 * 
	 * @return the Packet
	 * @throws IOException
	 */
	public Packet readPacket() throws IOException {
		Packet p = readPacket(null);
		return p;
	}
	
	/**
	 * Read a Packet from the Socket.  If a Packet is provided, it may be used in preference to creating a new Packet object.
	 * @param p a provided Packet.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Packet readPacket(Packet p) throws IOException {
		if (in == null) {
			throw new IOException("Input stream was not correctly opened");
		}

		p = in.readPacket(p);
			
		if (p.getId() == 0) {
			if (!server) {
				Integer i = (Integer)Field.read(p, 0);
				System.out.println("Client sent ping: " + i);
			} else {
				Integer i = (Integer)Field.read(p, 0);
				System.out.println("Server sent ping: " + i);
			}
		}
		return p;
	}
	
	/**
	 * Writes a Packet to the socket,
	 * 
	 * @param p the packet
	 * @throws IOException
	 */
	public void writePacket(Packet p) throws IOException {
		out.writePacket(p);
	}
	
	/**
	 * Gets the output stream.
	 * 
	 * @return
	 */
	public MCOutputStream getOutputStream() {
		return out;
	}
	
	/**
	 * Gets the input stream.
	 * 
	 * @return
	 */
	public MCInputStream getInputStream() {
		return in;
	}
	
	public void close() throws IOException {
		close(message);
	}
	
	/**
	 * Closes the socket.
	 */
	public void close(String message) throws IOException {
		IOException e = null;
		try {
			if (out != null) {
				out.writePacket(new KickPacket(message));
			}
		} catch (IOException ioe) {
			e = ioe;
		}
		try {
			socket.shutdownOutput();
			while (true) {
				in.readPacket();
			}
		} catch (EOFException eof) {
		} catch (IOException ioe) {
			ioe.printStackTrace();
			if (e == null) {
				e = ioe;
			}
		} finally {
			socket.close();
		}
		if (e != null) {
			throw new IOException("Unable to close MCSocket", e);
		}
	}
	
	public void printRecentPacketIds() {
		in.printRecentPacketIds();
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
	
	public SocketAddress getRemoteSocketAddress() {
		return socket.getRemoteSocketAddress();
	}
	
	public SocketAddress getLocalSocketAddress() {
		return socket.getLocalSocketAddress();
	}
	
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}
	
}
