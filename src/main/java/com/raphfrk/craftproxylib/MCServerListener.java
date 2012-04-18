package com.raphfrk.craftproxylib;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import com.raphfrk.craftproxylib.handler.ConnectionHandler;
import com.raphfrk.craftproxylib.handler.PacketHandlerRegistry;
import com.raphfrk.craftproxylib.util.ConcurrentTimedOutHashMap;

public class MCServerListener extends Thread {
	
	private final static long FLOOD_DEFAULT = 5000;
	private final static int FLOOD_MEMORY_TIMEOUT = 60000;
	private final static int FLOOD_LENGTH = 2;
	private final static long JOIN_TIMEOUT = 90000;

	private final ConcurrentTimedOutHashMap<InetAddress, Long> floodShield = new ConcurrentTimedOutHashMap<InetAddress, Long>(FLOOD_MEMORY_TIMEOUT, FLOOD_LENGTH);
	
	private final ConcurrentHashMap<ConnectionHandler, Boolean> connections = new ConcurrentHashMap<ConnectionHandler, Boolean>();
	
	private final PacketHandlerRegistry registry;
	private final ConnectionHandler connectionHandler;
	
	private final ServerSocket serverSocket;
	private final InetAddress address;
	private final int port;
	private final long floodTimeout;
	private final boolean rotateIP;
	
	private final AtomicInteger index = new AtomicInteger(1);
	
	public MCServerListener(int port, PacketHandlerRegistry registry, ConnectionHandler connectionHandler, boolean rotateIP) throws IOException {
		this(null, port, FLOOD_DEFAULT, registry, connectionHandler, rotateIP);
	}
	
	public MCServerListener(InetAddress address, int port, long floodTimeout, PacketHandlerRegistry registry, ConnectionHandler connectionHandler, boolean rotateIP) throws IOException {
		this.floodTimeout = floodTimeout;
		this.registry = registry;
		this.connectionHandler = connectionHandler;
		this.address = address;
		this.port = port;
		this.rotateIP = rotateIP;
		serverSocket = new ServerSocket(port);
		this.setName("Server Listener Thread-" + index.getAndIncrement());
	}
	
	public void run() {
		try {
			while (!interrupted()) {
				Socket clientSocket = null;

				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					safeCloseSocket(clientSocket);
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					safeCloseSocket(clientSocket);
					continue;
				}

				if (!floodShield(clientSocket)) {
					CraftProxyLib.log("Flood shield activated");
					continue;
				}
				
				ConnectionHandler handler;
				try {
					handler = connectionHandler.newInstance(clientSocket, registry, this, null);
				} catch (IOException ioe) {
					if (!clientSocket.isClosed()) {
						try {
							clientSocket.close();
						} catch (IOException e2) {
						}
					}
					continue;
				}
				
				handler.start();

			}
		} finally {
			for (ConnectionHandler h : this.connections.keySet()) {
				h.interrupt();
			}
			long startTime = System.currentTimeMillis();
			
			Iterator<ConnectionHandler> itr = this.connections.keySet().iterator();
			while (itr.hasNext()) {
				ConnectionHandler handler = itr.next();
				long timeRemaining = JOIN_TIMEOUT - (System.currentTimeMillis() - startTime);
				if (timeRemaining <= 0) {
					continue;
				}
				try {
					handler.join(timeRemaining);
				} catch (InterruptedException e) {
					continue;
				}
				if (!handler.isAlive()) {
					itr.remove();
				}
			}
			
			itr = this.connections.keySet().iterator();
			while (itr.hasNext()) {
				ConnectionHandler handler = itr.next();
				CraftProxyLib.getLogger().log(Level.SEVERE, "WARNING: Connection " + handler + " did not shut down correctly");
				CraftProxyLib.getLogger().log(Level.SEVERE, "WARNING: Shutting down thread forcefully");
				handler.stop();
			}
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		if (this.isAlive()) {
			try {
				InetAddress addr;
				if (address == null) {
					addr = InetAddress.getLocalHost();
				} else {
					addr = address;
				}
				Socket s = new Socket(addr, port);
				s.close();
			} catch (IOException ioe) {
			}
		}
	}
	
	public void removeConnection(ConnectionHandler handler) {
		connections.remove(handler);
	}
	
	public void addConnection(ConnectionHandler handler) {
		connections.put(handler, Boolean.TRUE);
	}
	
	public int getActiveConnections() {
		return connections.size();
	}
	
	private void safeCloseSocket(Socket s) {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e2) {
			}
		}
	}
	
	private boolean floodShield(Socket socket) {
		InetAddress a = socket.getInetAddress();

		long currentTime = System.currentTimeMillis();

		if (floodTimeout > 0 ) {
			Long lastConnect = floodShield.get(a);
			floodShield.put(a, currentTime);
			
			if (lastConnect != null) {
				long reconnectDelay = currentTime - lastConnect;
				if (reconnectDelay < floodTimeout) {
					try {
						MCSocket mClient = new MCSocket(socket, false);
						mClient.close("Last connection attempt only " + (reconnectDelay / 1000.0) + " seconds ago");
					} catch (IOException ioe) {
						if (!socket.isClosed()) {
							try {
								socket.close();
							} catch (IOException e2) {
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (!socket.isClosed()) {
							try {
								socket.close();
							} catch (IOException e2) {
							}
						}
					}
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean getRotateIP() {
		return rotateIP;
	}

}
