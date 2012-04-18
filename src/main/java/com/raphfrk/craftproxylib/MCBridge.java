package com.raphfrk.craftproxylib;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import com.raphfrk.craftproxylib.handler.ConnectionHandler;
import com.raphfrk.craftproxylib.handler.PacketHandler;
import com.raphfrk.craftproxylib.handler.PacketHandlerRegistry;
import com.raphfrk.craftproxylib.packet.Packet;

/**
 * Represents a bridge for two MCSockets
 */
public class MCBridge {
	
	private final MCPassthroughThread serverToClientThread;
	private final MCPassthroughThread clientToServerThread;
	private final PacketHandler[] upstream;
	private final PacketHandler[] downstream;
	private final ConnectionHandler connectionHandler;
	
	public MCBridge(Socket server, Socket client) throws IOException {
		this(new MCSocket(server, true), new MCSocket(client, false));
	}
	
	public MCBridge(Socket server, Socket client, PacketHandlerRegistry registry) throws IOException {
		this(new MCSocket(server, true), new MCSocket(client, false), registry);
	}
	
	public MCBridge(MCSocket server, MCSocket client) {
		this(server, client, PacketHandlerRegistry.nullRegistry);
	}
	
	public MCBridge(MCSocket server, MCSocket client, PacketHandlerRegistry registry) {
		this(server, client, registry, null);
	}
	
	public MCBridge(MCSocket server, MCSocket client, PacketHandlerRegistry registry, ConnectionHandler connectionHandler) {
		this(server, client, registry.getUpstreamHandlers(), registry.getDownstreamHandlers(), connectionHandler);
	}
	
	public MCBridge(MCSocket server, MCSocket client, MCBridge bridge) {
		this(server, client, bridge, null);
	}
	
	public MCBridge(MCSocket server, MCSocket client, MCBridge bridge, ConnectionHandler connectionHandler) {
		this(server, client, bridge.getUpstreamHandlers(), bridge.getDownstreamHandlers(), connectionHandler);
	}
	
	public MCBridge(MCSocket server, MCSocket client,  PacketHandler[] upstream, PacketHandler[] downstream, ConnectionHandler connectionHandler) {
		AtomicBoolean closeSync = new AtomicBoolean(false);
		
		this.upstream = createNewInstances(upstream);
		this.downstream = createNewInstances(downstream);
		
		this.serverToClientThread = new MCPassthroughThread(server, client, closeSync, this.downstream);
		this.clientToServerThread = new MCPassthroughThread(client, server, closeSync, this.upstream);
		
		this.serverToClientThread.setReturnThread(clientToServerThread);
		this.clientToServerThread.setReturnThread(serverToClientThread);
		
		this.connectionHandler = connectionHandler;
	}
	
	/**
	 * Starts the threads for the bridge
	 */
	public void start() {
		serverToClientThread.start();
		clientToServerThread.start();
	}
	
	/**
	 * Joins against the threads for the bridge.  Both threads are killed if either socket closes.
	 * 
	 * @throws InterruptedException 
	 */
	public void join() throws InterruptedException {
		join(0);
	}
	
	/**
	 * Interrupts the bridge
	 */
	public void interrupt() {
		serverToClientThread.interrupt();
		clientToServerThread.interrupt();
	}
	
	/**
	 * Joins against the threads for the bridge.  Both threads are killed if either socket closes.
	 * 
	 * @param millis the time to wait in milliseconds, or 0 for infinite time.
	 * @throws InterruptedException 
	 */
	public void join(long millis) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		serverToClientThread.join(millis);
		if (millis != 0) {
			millis -= System.currentTimeMillis() - startTime;
			if (millis == 0) {
				return;
			}
		}
		clientToServerThread.join(millis);
	}
	
	/**
	 * Gets the upstream handler instance array.
	 * 
	 * @return the handlers
	 */
	public PacketHandler[] getUpstreamHandlers() {
		return upstream;
	}
	
	/**
	 * Gets the upstream handler instance array.
	 * 
	 * @return the handlers
	 */
	public PacketHandler[] getDownstreamHandlers() {
		return downstream;
	}
	
	public void log(String message) {
		if (connectionHandler != null) {
			connectionHandler.log(message);
		} else {
			CraftProxyLib.log(message);
		}
	}
	
	private PacketHandler[] createNewInstances(PacketHandler[] handlers) {
		PacketHandler[] newHandlers = new PacketHandler[256];
		if (handlers != null) {
			for (int i = 0; i < 256; i++) {
				if (handlers[i] != null) {
					newHandlers[i] = handlers[i].newInstance(this);
				}
			}
		}
		return newHandlers;
	}

	private static class MCPassthroughThread extends Thread {
		
		private static final PacketHandler[] nullList = new PacketHandler[256];
		
		private final PacketHandler[] handlers;
		private final AtomicBoolean closeSync;
		
		private MCPassthroughThread counterFlowThread = null;
		private final MCSocket from;
		private final MCSocket to;
		private volatile boolean running = true;
		private volatile boolean readError = false;
		private volatile boolean writeError = false;
		private volatile boolean safeDeath = false;

		
		public MCPassthroughThread(MCSocket from, MCSocket to, AtomicBoolean closeSync, PacketHandler[] handlers) {
			this.from = from;
			this.to = to;
			if (handlers == null) {
				this.handlers = nullList;
			} else {
				this.handlers = handlers;
			}
			this.closeSync = closeSync;
		}
		
		public void setReturnThread(MCPassthroughThread t) {
			this.counterFlowThread = t;
		}
		
		public boolean isReadError() {
			if (isAlive()) {
				throw new UnsupportedOperationException("This method should not be called while the thread is running");
			}
			return !safeDeath || readError;
		}
		
		public boolean isWriteError() {
			if (isAlive()) {
				throw new UnsupportedOperationException("This method should not be called while the thread is running");
			}
			return !safeDeath || writeError;
		}
		
		public void interrupt() {
			running = false;
			super.interrupt();
		}
		
		public void run() {
			Packet p = null;
			try {
				while (running) {
					try {
						p = from.readPacket(p);
					} catch (EOFException eof) {
						running = false;
						readError = true;
						continue;
					} catch (IOException ioe) {
						running = false;
						readError = true;
						ioe.printStackTrace();
						continue;
					} catch (Exception e) {
						running = false;
						readError = true;
						e.printStackTrace();
						continue;
					}
					int id = p.getId();
					if (id == -1) {
						readError = true;
						running = false;
						throw new IllegalStateException("Incomplete packet read");
					}
					PacketHandler handler = handlers[id];
					if (handler != null) {
						try {
							p = handler.handle(p, to.getOutputStream(), from.getOutputStream());
						} catch (IOException ioe) {
							readError = true;
							writeError = true;
							running = false;
							throw new RuntimeException("Handler " + handler.getClass().getSimpleName() + " threw an IOException", ioe);
						} catch (Exception e) {
							readError = true;
							writeError = true;
							running = false;
							throw new RuntimeException("Handler " + handler.getClass().getSimpleName() + " threw an exception", e);
						}
					}
					if (p == PacketHandler.INTERRUPT) {
						running = false;
					} else if (p != null) {
						try {
							to.writePacket(p);
						} catch (IOException ioe) {
							ioe.printStackTrace();
							running = false;
							writeError = true;
						} catch (Exception e) {
							e.printStackTrace();
							running = false;
							writeError = true;
						}
					}

				}
				safeDeath = true;
			} finally {
				if (counterFlowThread == null) {
					if (isReadError()) {
						try {
							from.close();
						} catch (IOException ioe) {
						}
					}
					if (isWriteError()) {
						try {
							to.close();
						} catch (IOException ioe) {
						}
					}
				} else {
					counterFlowThread.interrupt();
					boolean counterFlowThreadDead = !closeSync.compareAndSet(false, true);

					if (counterFlowThreadDead) {
						try {
							counterFlowThread.join();
						} catch (InterruptedException e) {
						}
						if (readError || counterFlowThread.isWriteError()) {
							try {
								from.close();
							} catch (IOException ioe) {
							}
						}
						if (writeError || counterFlowThread.isReadError()) {
							try {
								to.close();
							} catch (IOException ioe) {
							}
						}
					}
				}
			}
		}
	}
}
