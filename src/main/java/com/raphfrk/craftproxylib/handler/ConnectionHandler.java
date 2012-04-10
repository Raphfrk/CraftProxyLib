package com.raphfrk.craftproxylib.handler;

import java.io.IOException;
import java.net.Socket;

import com.raphfrk.craftproxylib.MCServerListener;
import com.raphfrk.craftproxylib.MCSocket;

public abstract class ConnectionHandler extends Thread {

	/**
	 * The socket connecting to the client
	 */
	protected final Socket client;
	
	/**
	 * The MCSocket connecting to the client
	 */
	protected MCSocket mClient = null;
	
	/**
	 * The MCSocket connecting to the client
	 */
	protected MCSocket mServer = null;
	
	/**
	 * The server socket listener
	 */
	protected final MCServerListener listener;
	
	private final PacketHandlerRegistry registry;
	
	protected ConnectionHandler() {
		this.client = null;
		this.registry = null;
		this.listener = null;
	}
	
	public ConnectionHandler(Socket client, PacketHandlerRegistry registry, MCServerListener listener) throws IOException {
		this.client = client;
		this.registry = registry;
		this.listener = listener;
		setName(client.getInetAddress().toString());
	}
	
	public final void run() {
		if (client == null && registry == null && listener == null) {
			throw new IllegalStateException("Connection handler started with default constructor should not be run");
		}
		
		if (listener != null) {
			listener.addConnection(this);
		}
		try {
			handleConnection();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			endInternal();
		}
	}
	
	
	private final void endInternal() {
		try {
			end();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (listener != null) {
				listener.removeConnection(this);
			}
		}
	}

	/**
	 * Handles the connection.  
	 */
	public abstract void handleConnection() throws IOException;

	
	/**
	 * Safely ends the connection.
	 */
	public void end() throws IOException {
		if (mClient != null) {
			mClient.close("Connection shut down");
		}
		if (mServer != null) {
			mServer.close("Connection shut down");
		}
	}
	
	/**
	 * Creates a new instance of a connection handler
	 * 
	 * @param client
	 * @param registry
	 * @param listener
	 * @param config
	 * @return
	 * @throws IOException
	 */
	public abstract ConnectionHandler newInstance(Socket client, PacketHandlerRegistry registry, MCServerListener listener, ConnectionConfig config) throws IOException;
	
}
